package com.furiqd.local.contract

import com.furiqd.local.entity.JointPetEntity
import kotlinx.coroutines.flow.Flow

interface PetLocalDataSource {
    fun observeCachedPetById(petId: String): Flow<JointPetEntity?>
    suspend fun cachePet(pet: JointPetEntity)
}