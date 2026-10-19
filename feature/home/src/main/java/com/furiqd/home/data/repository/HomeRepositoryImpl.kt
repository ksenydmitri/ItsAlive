package com.furiqd.home.data.repository

import com.furiqd.home.data.mapper.toDomain
import com.furiqd.home.domain.repository.HomeRepository
import com.furiqd.network.contract.RemoteChallengeDataSource
import com.furiqd.network.contract.RemoteRoomDataSource
import com.furiqd.network.dto.DailyChallengeDto
import com.furiqd.shared.data.mapper.RoomMapper
import com.furiqd.shared.domain.model.DailyChallenge
import com.furiqd.shared.domain.model.PairingRoom
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

internal class HomeRepositoryImpl@Inject constructor(
    private val remoteRoomDataSource: RemoteRoomDataSource,
    private val remoteChallengeDataSource: RemoteChallengeDataSource,
    private val roomMapper: RoomMapper
): HomeRepository {
    override fun observeUserRooms(userId: String): Flow<List<PairingRoom>> {
        return remoteRoomDataSource.observeUserRooms(userId)
            .map { roomDtoList ->
                coroutineScope {
                    roomDtoList.map { roomDto ->
                        async {
                            val challengeDto = roomDto.challengeId?.let { challengeId ->
                                remoteChallengeDataSource.getChallengeById(challengeId)
                            }
                            roomMapper.toDomain(roomDto, challengeDto)
                        }
                    }.awaitAll()
                }
            }
    }

    override suspend fun getRoomWithChallenge(roomId: String, userId: String): Result<PairingRoom> =
        runCatching { coroutineScope {
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
        return remoteChallengeDataSource.observeChallenge(challengeId).map { it.toDomain() }
    }

    override suspend fun getChallenge(challengeId: String): Result<DailyChallenge> =
        runCatching {
            remoteChallengeDataSource.getChallengeById(challengeId)
        }.map { dto ->
            dto?.toDomain() ?: throw Exception("Challenge not found")
        }

    override suspend fun submitChallengePhoto(
        challengeId: String,
        userId: String,
        isHost: Boolean,
        photoBytes: ByteArray
    ): Result<Unit> = runCatching {
        val uploadedPhotoPath = remoteChallengeDataSource.uploadUserPhoto(
            challengeId = challengeId,
            userId = userId,
            photoBytes = photoBytes
        )

        remoteChallengeDataSource.updatePhotoUrl(
            challengeId = challengeId,
            isHost = isHost,
            photoPath = uploadedPhotoPath
        )
    }

    override suspend fun checkActiveHour(challengeId: String): Boolean {
        val challenge = remoteChallengeDataSource.getChallengeById(challengeId)
        return challenge?.isActiveHour ?: false
    }
}