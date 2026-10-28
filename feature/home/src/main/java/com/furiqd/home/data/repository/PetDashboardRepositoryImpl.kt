package com.furiqd.home.data.repository

import com.furiqd.home.domain.repository.PetDashboardRepository
import com.furiqd.local.contract.PetLocalDataSource
import com.furiqd.local.room.RoomDao
import com.furiqd.shared.data.mapper.toDomain
import com.furiqd.shared.domain.model.JointPet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class PetDashboardRepositoryImpl@Inject constructor(
    private val petLocalDataSource: PetLocalDataSource,
    private val roomDao: RoomDao
): PetDashboardRepository {

    override fun observePetStateById(petId: String): Flow<JointPet?> {
        return petLocalDataSource.observeCachedPetById(petId).map { it?.toDomain()
        }
    }

    override fun observePetStateByRoomId(roomId: String): Flow<JointPet?> {
        return roomDao.observeRoomById(roomId).map { roomEntity ->
            roomEntity?.pet?.toDomain()
        }
    }
}