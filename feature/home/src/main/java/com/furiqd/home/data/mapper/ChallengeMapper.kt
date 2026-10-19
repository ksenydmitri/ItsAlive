package com.furiqd.home.data.mapper

import com.furiqd.network.dto.ChallengeTaskDto
import com.furiqd.network.dto.DailyChallengeDto
import com.furiqd.shared.domain.model.ChallengeTask
import com.furiqd.shared.domain.model.DailyChallenge

fun DailyChallengeDto.toDomain(): DailyChallenge = DailyChallenge(
    id = id,
    isActiveHour = isActiveHour,
    expiresAt = expiresAt,
    userPhotoUrl = userPhotoUrl,
    friendPhotoUrl = friendPhotoUrl,
    compositeImageUrl = compositeImageUrl,
    task = task?.toDomain()
)

fun ChallengeTaskDto.toDomain(): ChallengeTask = ChallengeTask(
    id = objectId,
    userTaskPrompt = userTaskPrompt,
    friendTaskPrompt = friendTaskPrompt
)
