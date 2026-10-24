package com.furiqd.home.data.repository

import android.util.Log
import com.furiqd.home.data.mapper.toDomain
import com.furiqd.home.domain.model.PushNotificationPayload
import com.furiqd.home.domain.repository.HomeRepository
import com.furiqd.local.contract.ImageLocalDataSource
import com.furiqd.network.contract.RemoteChallengeDataSource
import com.furiqd.network.contract.RemoteRoomDataSource
import com.furiqd.network.dto.DailyChallengeDto
import com.furiqd.network.model.PushNotificationType
import com.furiqd.shared.data.mapper.RoomMapper
import com.furiqd.shared.domain.model.DailyChallenge
import com.furiqd.shared.domain.model.PairingRoom
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

internal class HomeRepositoryImpl @Inject constructor(
    private val remoteRoomDataSource: RemoteRoomDataSource,
    private val remoteChallengeDataSource: RemoteChallengeDataSource,
    private val localFileDataSource: ImageLocalDataSource,
    private val roomMapper: RoomMapper
): HomeRepository {
    override fun observeUserRooms(userId: String): Flow<List<PairingRoom>> {
        return remoteRoomDataSource.observeUserRooms(userId)
            .map { roomDtoList ->
                coroutineScope {
                    roomDtoList.map { roomDto ->
                        async {
                            val challengeDto = roomDto.challengeId?.let { challengeId ->
                                runCatching { remoteChallengeDataSource.getChallengeById(challengeId) }
                                    .onFailure { Log.e("HomeRepository", "Error fetching challenge $challengeId", it) }
                                    .getOrNull()
                            }
                            roomMapper.toDomain(roomDto, challengeDto)
                        }
                    }.awaitAll()
                }
            }
            .flowOn(Dispatchers.Default)
            .catch { e ->
                Log.e("HomeRepository", "Error observing user rooms", e)
                emit(emptyList())
            }
    }

    override suspend fun getRoomWithChallenge(roomId: String, userId: String): Result<PairingRoom> =
        runCatching {
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
        }

    override suspend fun deleteRoom(roomId: String) {
        remoteRoomDataSource.deleteRoom(roomId)
    }

    override fun observeChallenge(challengeId: String): Flow<DailyChallenge> {
        return remoteChallengeDataSource.observeChallenge(challengeId)
            .map { it.toDomain() }
            .catch { e ->
                Log.e("HomeRepository", "Error observing challenge $challengeId", e)
            }
    }

    override suspend fun getChallenge(challengeId: String): Result<DailyChallenge> =
        runCatching {
            remoteChallengeDataSource.getChallengeById(challengeId)
        }.map { dto ->
            dto?.toDomain() ?: throw Exception("Challenge not found")
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
}