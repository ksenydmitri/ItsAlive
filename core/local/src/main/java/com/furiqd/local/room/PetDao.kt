package com.furiqd.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.furiqd.local.entity.JointPetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PetDao {
    @Query("SELECT * FROM joint_pets WHERE id = :petId")
    fun observePetById(petId: String): Flow<JointPetEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPet(pet: JointPetEntity)

    @Query("DELETE FROM joint_pets")
    suspend fun deletePet()
}
