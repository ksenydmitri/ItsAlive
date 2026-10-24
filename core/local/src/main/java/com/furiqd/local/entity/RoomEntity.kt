package com.furiqd.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rooms")
data class RoomEntity(
    @PrimaryKey
    val id: String = "",
    val code: String = "",
    val hostId: String = "",
    val friendId: String? = null,
    @Embedded(prefix = "pet_")
    val pet: RoomPetEmbedded = RoomPetEmbedded(),
    val createdAt: Long = 0L,
    val status: String = "",
    val challengeId: String? = null,
    val isStreakActive: Boolean,
    val streakCount: Int
)

data class RoomPetEmbedded(
    val id: String = "",
    val name: String = "",
    val level: Int = 1,
    val experience: Int = 0,
    val stage: String = "EGG",
    val hatchedAt: Long? = null,
    val petEggType: Int = 0
)