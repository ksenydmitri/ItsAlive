package com.furiqd.local.datasource

import com.furiqd.local.contract.PetLocalDataSource
import com.furiqd.local.entity.JointPetEntity
import com.furiqd.local.room.PetDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PetLocalDataSourceImpl@Inject constructor(
    private val petDao: PetDao
): PetLocalDataSource {
    override fun observeCachedPetById(petId: String): Flow<JointPetEntity?> {
        return petDao.observePetById(petId)
    }

    override suspend fun cachePet(pet: JointPetEntity) {
        petDao.insertPet(pet)
    }
}