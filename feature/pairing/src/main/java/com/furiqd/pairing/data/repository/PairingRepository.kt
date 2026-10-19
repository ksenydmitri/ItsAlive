package com.furiqd.pairing.data.repository

import com.furiqd.pairing.data.datasource.PairingLocalDataSource
import com.furiqd.pairing.data.datasource.PairingRemoteDataSource
import com.furiqd.pairing.data.datasource.RoomException
import com.furiqd.pairing.data.datasource.model.dto.JointPetDto
import com.furiqd.pairing.data.datasource.model.dto.PairingRoomDto
import com.furiqd.pairing.data.mapper.toDomain
import com.furiqd.shared.domain.model.PairingRoom
import com.furiqd.shared.domain.model.PetEggType
import kotlinx.coroutines.flow.Flow
import java.util.Date
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
    fun observeRoomState(roomId: String): Flow<PairingRoom>
}

internal class PairingRepositoryImpl @Inject constructor(
    private val remoteDataSource: PairingRemoteDataSource,
    private val localDataSource: PairingLocalDataSource
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
                createdAt = Date().time,
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
            val roomId = remoteDataSource.executeJoinTransaction(inviteCode, userId)
            localDataSource.saveRoomId(roomId)
            localDataSource.setPairingComplete(true)

            roomId
        }
    }

    override suspend fun getPairingRoom(roomId: String): Result<PairingRoom> {
        return runCatching {
            val roomDto = remoteDataSource.getPairingRoom(roomId)
                ?: throw RoomException.RoomNotFoundException()
            roomDto.toDomain()
        }
    }

    override fun observeRoomState(roomId: String): Flow<PairingRoom> {
        TODO("Not yet implemented")
    }
}