package com.furiqd.home.presentation.navigation

data class ChallengeUiModel(
    val id: String,
    val expiresAt: Long?,
    val userTask: String,
    val friendTask: String,
    val isUserDone: Boolean,
    val isFriendDone: Boolean
)