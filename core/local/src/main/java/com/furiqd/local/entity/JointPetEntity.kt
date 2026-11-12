package com.furiqd.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "joint_pets")
data class JointPetEntity(
    @PrimaryKey val id: String = "",
    val name: String = "",
    val level: Int = 0,
    val experience: Int = 0,
    val stage: String = "",
    val hatchedAt: Long? = null,
    val petEggType: Int = 0
)
