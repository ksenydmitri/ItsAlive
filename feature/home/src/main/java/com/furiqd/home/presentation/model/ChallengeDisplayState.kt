package com.furiqd.home.presentation.model

sealed interface ChallengeDisplayState {
    data object InactiveWaitingHour : ChallengeDisplayState
    data object CompositeReady : ChallengeDisplayState
    data class UserTurn(val deadlineMillis: Long) : ChallengeDisplayState
    data class WaitingFriend(val deadlineMillis: Long) : ChallengeDisplayState
}