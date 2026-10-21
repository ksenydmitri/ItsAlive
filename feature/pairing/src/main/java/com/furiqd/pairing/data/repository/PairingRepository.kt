package com.furiqd.pairing.data.repository

import com.furiqd.local.contract.PairingLocalDataSource
import com.furiqd.network.contract.RemoteRoomDataSource
import com.furiqd.network.dto.JointPetDto
import com.furiqd.network.dto.PairingRoomDto
import com.furiqd.shared.data.mapper.RoomMapper
import com.furiqd.shared.domain.model.PairingRoom
import com.furiqd.shared.domain.model.PetEggType
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

internal interface PairingRepository {
    suspend fun createRoom(hostUserId: String,
                           petName: String,
                           petEggType: PetEggType): Result<String>

    suspend fun joinRoom(
        inviteCode: String,
        userId: String
    ): Result<String>
    suspend fun getPairingRoom(roomId: String): Result<PairingRoom>
    fun observeRoomState(roomId: String): Flow<PairingRoom?>
}

internal class PairingRepositoryImpl @Inject constructor(
    private val remoteDataSource: RemoteRoomDataSource,
    private val localDataSource: PairingLocalDataSource,
    private val roomMapper: RoomMapper
) : PairingRepository {

    override suspend fun createRoom(hostUserId: String,
                                    petName: String,
                                    petEggType: PetEggType): Result<String> {
        return runCatching {
            var uniqueCode: String
            do {
                uniqueCode = (100000..999999).random().toString()
            } while (remoteDataSource.isCodeExisting(uniqueCode))
            
            val roomDto = PairingRoomDto(
                code = uniqueCode,
                hostId = hostUserId,
                friendId = null,
                status = "WAITING",
                createdAt = System.currentTimeMillis(),
                pet = JointPetDto(
                    id = UUID.randomUUID().toString(),
                    name = petName,
                    petEggType = petEggType.id,
                    stage = "EGG",
                    level = 1,
                    experience = 0,
                    hatchedAt = null
                )
            )
            val roomId = remoteDataSource.createRoom(roomDto)
            localDataSource.saveRoomId(roomId)
            roomId
        }
    }

    override suspend fun joinRoom(inviteCode: String, userId: String): Result<String> {
        return runCatching {
            val roomId = remoteDataSource.joinRoom(inviteCode, userId)
            localDataSource.saveRoomId(roomId)
            localDataSource.setPairingComplete(true)

            roomId
        }
    }

    override suspend fun getPairingRoom(roomId: String): Result<PairingRoom> {
        return runCatching {
            val roomDto = remoteDataSource.getRoomDetails(roomId)
                ?: throw IllegalStateException("Room not found")
            roomMapper.toDomain(roomDto)
        }
    }

    override fun observeRoomState(roomId: String): Flow<PairingRoom?> {
        return remoteDataSource.observeRoom(roomId).map { roomMapper.toDomain(it
            ?: PairingRoomDto()) }
    }
}