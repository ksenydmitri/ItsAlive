package com.furiqd.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_challenges")
class DailyChallengeEntity {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
    var isStreakActive: Boolean = false
    var streakCount: Int = 0
    var isActiveHour: Boolean = false
    var expiresAt: Long = 0
}