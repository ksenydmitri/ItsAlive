package com.furiqd.home.data.repository

import android.util.Log
import com.furiqd.home.domain.model.PushNotificationPayload
import com.furiqd.home.domain.repository.HomeRepository
import com.furiqd.local.contract.ImageLocalDataSource
import com.furiqd.local.contract.RoomLocalDataSource
import com.furiqd.local.scheduler.StreakExpirationScheduler
import com.furiqd.network.contract.RemoteChallengeDataSource
import com.furiqd.network.contract.RemoteRoomDataSource
import com.furiqd.shared.data.mapper.RoomMapper
import com.furiqd.shared.domain.model.PairingRoom
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class HomeRepositoryImpl @Inject constructor(
    private val remoteRoomDataSource: RemoteRoomDataSource,
    private val remoteChallengeDataSource: RemoteChallengeDataSource,
    private val localFileDataSource: ImageLocalDataSource,
    private val localRoomDataSource: RoomLocalDataSource,
    private val streakExpirationScheduler: StreakExpirationScheduler,
    private val roomMapper: RoomMapper
) : HomeRepository {

    override fun observeUserRooms(userId: String): Flow<List<PairingRoom>> {
        return localRoomDataSource.observeUserRooms(userId).map { entities ->
            entities.map {
                roomMapper.toDomain(it.room, it.challenge)
            }
        }
    }

    override suspend fun getRoomWithChallenge(roomId: String, userId: String): Result<PairingRoom> =
        runCatching {
            coroutineScope {
                val roomDeferred = localRoomDataSource.getRoomWithChallenge(roomId)
                TODO("Верните объект PairingRoom здесь")
            }
        }
    /*runCatching {
        coroutineScope {
            val roomDeferred = async { remoteRoomDataSource.getRoomDetails(roomId) }
            val roomDto = roomDeferred.await() ?: throw Exception("Room not found")

            val challengeId = roomDto.challengeId

            val challengeDto = if (!challengeId.isNullOrEmpty()) {
                remoteChallengeDataSource.getChallengeById(challengeId)
            } else {
                val newChallengeDto = DailyChallengeDto(
                    isActiveHour = true,
                    expiresAt = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1),
                    task = remoteChallengeDataSource.getRandomTask()
                )

                val createdChallenge = remoteChallengeDataSource.createChallenge(newChallengeDto)
                launch {
                    remoteRoomDataSource.updateRoomChallengeId(roomId, createdChallenge.id)
                }
                createdChallenge
            }
            roomMapper.toDomain(roomDto, challengeDto)
        }
    }*/

    override suspend fun deleteRoom(roomId: String) {
        remoteRoomDataSource.deleteRoom(roomId)
    }

    override suspend fun submitChallengePhoto(
        roomId: String,
        photoPath: String,
        userId: String
    ): Result<Unit> = runCatching {
        val room = remoteRoomDataSource.getRoomDetails(roomId) ?: throw Exception("Room not found")
        val challengeId = room.challengeId ?: throw Exception("Challenge not found")
        val isHost = room.hostId == userId

        val (bytes, mimeType) = localFileDataSource.readFileAsBytes(photoPath)
            ?: throw IllegalStateException("File not found")

        Log.d("HomeRepositoryImpl", "bytes read: ${bytes.size}, mime: $mimeType")

        val uploadedPhotoPath = remoteChallengeDataSource.uploadUserPhoto(
            challengeId = challengeId,
            userId = userId,
            photoBytes = bytes,
            mimeType = mimeType
        )

        remoteChallengeDataSource.updatePhotoUrl(
            challengeId = challengeId,
            isHost = isHost,
            photoPath = uploadedPhotoPath
        )

        val updatedChallengeDto = remoteChallengeDataSource.getChallengeById(challengeId)
        if (updatedChallengeDto != null) {
            localRoomDataSource.insertChallenge(roomMapper.toEntity(updatedChallengeDto))
        }
    }

    override suspend fun checkActiveHour(challengeId: String): Boolean {
        return runCatching { remoteChallengeDataSource.getChallengeById(challengeId) }
            .getOrNull()?.isActiveHour ?: false
    }

    override suspend fun inciteFriend(payload: PushNotificationPayload) {
        remoteChallengeDataSource.inciteFriend(
            roomId = payload.roomId,
            senderId = payload.senderId,
            recipientId = payload.recipientId
        )
    }

    override suspend fun updateStreak(roomId: String, streakCount: Int, isStreakActive: Boolean) {
        remoteRoomDataSource.updateStreak(roomId, streakCount, isStreakActive)
        localRoomDataSource.updateStreak(roomId, streakCount, isStreakActive)
    }
}