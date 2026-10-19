package com.furiqd.shared.domain.model

data class DailyChallenge(
    val id: String = "",
    val isStreakActive: Boolean = true,
    val streakCount: Int = 0,
    val isActiveHour: Boolean = false,
    val expiresAt: Long? = null,
    val task: ChallengeTask? = null,
    val userPhotoUrl: String? = null,
    val friendPhotoUrl: String? = null,
    val compositeImageUrl: String? = null
) {
    val isUserDone: Boolean get() = userPhotoUrl != null
    val isFriendDone: Boolean get() = friendPhotoUrl != null

    fun getMillisLeft(currentTime: Long = System.currentTimeMillis()): Long {
        if (expiresAt == null) return 0L
        return (expiresAt - currentTime).coerceAtLeast(0L)
    }
}