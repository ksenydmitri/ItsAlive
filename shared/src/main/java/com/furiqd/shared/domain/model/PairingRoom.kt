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
    val expirationTime: Long
        get() = createdAt + TimeUnit.HOURS.toMillis(EXPIRATION_HOURS)

    fun isExpired(currentTime: Long): Boolean {
        return currentTime > expirationTime
    }

    fun getMillisLeft(currentTime: Long): Long {
        return (expirationTime - currentTime).coerceAtLeast(0L)
    }

    companion object {
        const val EXPIRATION_HOURS = 24L
    }
}