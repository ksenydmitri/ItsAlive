package com.furiqd.home.presentation.model

data class ChallengeUiModel(
    val id: String,
    val expiresAt: Long?,
    val userTask: String,
    val friendTask: String,
    val isUserDone: Boolean,
    val isFriendDone: Boolean,
    val displayState: ChallengeDisplayState
)