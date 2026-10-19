package com.furiqd.home.presentation.mapper

import com.furiqd.home.presentation.navigation.ChallengeUiModel
import com.furiqd.shared.domain.model.DailyChallenge
fun DailyChallenge.toUiModel(currentTime: Long = System.currentTimeMillis()): ChallengeUiModel {
    return ChallengeUiModel(
        id = id,
        timeLeft = getMillisLeft(currentTime),
        userTask = task?.userTaskPrompt.orEmpty(),
        friendTask = task?.friendTaskPrompt.orEmpty(),
        isUserDone = isUserDone,
        isFriendDone = isFriendDone
    )
}