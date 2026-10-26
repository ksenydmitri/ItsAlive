package com.furiqd.home.presentation.mapper

import com.furiqd.home.presentation.navigation.ChallengeUiModel
import com.furiqd.shared.domain.model.DailyChallenge

fun DailyChallenge.toUiModel(isCurrentUserHost: Boolean): ChallengeUiModel {
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

    return ChallengeUiModel(
        id = id,
        expiresAt = expiresAt,
        userTask = myTask.orEmpty(),
        friendTask = partnerTask.orEmpty(),
        isUserDone = amIDone,
        isFriendDone = isPartnerDone
    )
}