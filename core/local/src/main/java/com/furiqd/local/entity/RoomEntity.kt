package com.furiqd.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "rooms",
    foreignKeys = [
        ForeignKey(
            entity = DailyChallengeEntity::class,
            parentColumns = ["id"],
            childColumns = ["challengeId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["challengeId"]),
        Index(value = ["hostId"]),
        Index(value = ["friendId"])
    ]
)
data class RoomEntity(
    @PrimaryKey
    val id: String,
    val code: String,
    val hostId: String,
    val friendId: String?,
    @Embedded(prefix = "pet_")
    val pet: JointPetEntity,
    val createdAt: Long,
    val status: String,
    val challengeId: String?,
    val isStreakActive: Boolean,
    val streakCount: Int
)