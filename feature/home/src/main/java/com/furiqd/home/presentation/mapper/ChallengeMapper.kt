package com.furiqd.home.presentation.mapper

import com.furiqd.home.presentation.navigation.ChallengeUiModel
import com.furiqd.shared.domain.model.DailyChallenge
fun DailyChallenge.toUiModel(): ChallengeUiModel {
    return ChallengeUiModel(
        id = id,
        expiresAt = expiresAt,
        userTask = task?.userTaskPrompt.orEmpty(),
        friendTask = task?.friendTaskPrompt.orEmpty(),
        isUserDone = isUserDone,
        isFriendDone = isFriendDone
    )
}