package com.furiqd.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_challenges")
data class DailyChallengeEntity(
    @PrimaryKey val id: String,
    val isStreakActive: Boolean,
    val streakCount: Int,
    val isActiveHour: Boolean,
    val expiresAt: Long?,
    val userTaskPrompt: String?,
    val friendTaskPrompt: String?,
    val userPhotoUrl: String?,
    val friendPhotoUrl: String?,
    val compositeImageUrl: String?
)