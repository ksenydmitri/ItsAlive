package com.furiqd.shared.data.repositories

import com.furiqd.shared.data.mapper.toDomain
import com.furiqd.shared.data.mapper.toEntity
import com.furiqd.shared.domain.model.PairingRoom
import com.furiqd.shared.domain.repository.RoomRepository
import com.furiqd.local.contract.PairingLocalDataSource
import com.furiqd.network.contract.RemoteRoomDataSource
import com.furiqd.shared.data.mapper.RoomMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RoomRepositoryImpl @Inject constructor(
    private val remoteDataSource: RemoteRoomDataSource,
    private val localDataSource: PairingLocalDataSource,
    private val roomMapper: RoomMapper
) : RoomRepository {

    override fun observeCurrentRoom(roomId: String): Flow<PairingRoom?> {
        return remoteDataSource.observeRoom(roomId)
            .map { remoteRoomDto ->
                if (remoteRoomDto == null) return@map null

                val domainRoom = roomMapper.toDomain(remoteRoomDto)
                val localPetEntity = domainRoom.pet.toEntity()
                localDataSource.cachePet(localPetEntity)

                domainRoom
            }
    }

    override suspend fun nudgeFriend(
        roomId: String,
        senderUserId: String
    ): Result<Unit> {
        return runCatching {
            remoteDataSource.sendNudge(roomId, senderUserId)
        }
    }

    override suspend fun fetchRoomDetails(roomId: String): Result<PairingRoom> {
        return runCatching {
            val remoteRoomDto = remoteDataSource.getRoomDetails(roomId)
                ?: throw NoSuchElementException("Room with id $roomId not found")

            val domainRoom = roomMapper.toDomain(remoteRoomDto)
            val localPetEntity = domainRoom.pet.toEntity()
            localDataSource.cachePet(localPetEntity)

            domainRoom
        }
    }
}