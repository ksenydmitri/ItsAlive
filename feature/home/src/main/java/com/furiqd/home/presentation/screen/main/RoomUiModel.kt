package com.furiqd.home.presentation.screen.main

import com.furiqd.home.presentation.navigation.ChallengeUiModel

sealed interface RoomUiModel {
    val id: String
    val isSelected: Boolean

    data class WaitingForFriend(
        override val id: String,
        override val isSelected: Boolean = false,
        val inviteCode: String,
        val petName: String,
        val petAvatarId: Int,
        val expiringTime: Long,
        val userAvatarUrl: String? = null
    ) : RoomUiModel

    data class Active(
        override val id: String,
        override val isSelected: Boolean = false,
        val petName: String,
        val petAvatarId: Int,
        val userAvatarUrl: String?,
        val friendAvatarUrl: String?,
        val isStreakActive: Boolean,
        val streakCount: Int,
        val isActiveHour: Boolean,
        val challengeUiModel: ChallengeUiModel?
    ) : RoomUiModel

    data class Placeholder(
        override val id: String = "placeholder_id",
        override val isSelected: Boolean = false
    ) : RoomUiModel
}