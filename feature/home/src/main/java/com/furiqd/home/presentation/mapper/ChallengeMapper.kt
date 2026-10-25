package com.furiqd.home.presentation.mapper

import com.furiqd.home.presentation.model.ChallengeDisplayState
import com.furiqd.home.presentation.model.ChallengeUiModel
import com.furiqd.shared.domain.model.DailyChallenge

fun DailyChallenge.toUiModel(
    isCurrentUserHost: Boolean,
    currentTime: Long = System.currentTimeMillis()
): ChallengeUiModel {
    val myTask = if (isCurrentUserHost) {
        task?.userTaskPrompt
    } else {
        task?.friendTaskPrompt
    }

    val partnerTask = if (isCurrentUserHost) {
        task?.friendTaskPrompt
    } else {
        task?.userTaskPrompt
    }

    val amIDone = if (isCurrentUserHost) isUserDone else isFriendDone
    val isPartnerDone = if (isCurrentUserHost) isFriendDone else isUserDone

    val displayState = calculateDisplayState(
        isCurrentUserDone = amIDone,
        isPartnerDone = isPartnerDone,
        expiresAt = expiresAt ?: 0L,
        currentTime = currentTime
    )

    return ChallengeUiModel(
        id = id,
        expiresAt = expiresAt,
        userTask = myTask.orEmpty(),
        friendTask = partnerTask.orEmpty(),
        isUserDone = amIDone,
        isFriendDone = isPartnerDone,
        displayState = displayState
    )
}

private fun calculateDisplayState(
    isCurrentUserDone: Boolean,
    isPartnerDone: Boolean,
    expiresAt: Long,
    currentTime: Long
): ChallengeDisplayState {
    val isExpired = expiresAt in 1 until currentTime

    return when {
        isCurrentUserDone && isPartnerDone -> {
            ChallengeDisplayState.CompositeReady
        }
        isExpired -> {
            ChallengeDisplayState.InactiveWaitingHour
        }
        !isCurrentUserDone -> {
            ChallengeDisplayState.UserTurn(deadlineMillis = expiresAt)
        }
        else -> {
            ChallengeDisplayState.WaitingFriend(deadlineMillis = expiresAt)
        }
    }
}