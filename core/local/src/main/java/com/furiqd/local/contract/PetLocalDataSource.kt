package com.furiqd.local.contract

import com.furiqd.local.entity.JointPetEntity
import kotlinx.coroutines.flow.Flow

interface PetLocalDataSource {
    fun observeCachedPet(): Flow<JointPetEntity?>
    suspend fun cachePet(pet: JointPetEntity)
}