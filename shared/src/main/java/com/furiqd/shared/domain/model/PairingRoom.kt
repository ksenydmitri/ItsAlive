package com.furiqd.shared.domain.model

import java.util.concurrent.TimeUnit

data class PairingRoom(
    val id: String = "",
    val code: String = "",
    val hostId: String = "",
    val friendId: String? = null,
    val pet: JointPet = JointPet(),
    val createdAt: Long = 0L,
    val status: RoomStatus = RoomStatus.WaitingForFriend,
    val challenge: DailyChallenge? = null,
    val isStreakActive: Boolean,
    val streakCount: Int
) {
    fun isExpired(currentTime: Long): Boolean {
        val expirationTime = createdAt + TimeUnit.HOURS.toMillis(24)
        return currentTime > expirationTime
    }

    fun getMillisLeft(currentTime: Long): Long {
        val expirationTime = createdAt + TimeUnit.HOURS.toMillis(24)
        return (expirationTime - currentTime).coerceAtLeast(0L)
    }
}