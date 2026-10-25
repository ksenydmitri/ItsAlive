package com.furiqd.home.domain.usecase

import android.util.Log
import com.furiqd.home.domain.repository.HomeRepository
import javax.inject.Inject

internal class SubmitChallengePhotoUseCase @Inject constructor(
    private val challengeRepository: HomeRepository
) {
    suspend operator fun invoke(
        roomId: String,
        photoPath: String,
        userId: String
    ): Result<Unit> = runCatching {
        challengeRepository.submitChallengePhoto(roomId, photoPath, userId).getOrThrow()
        val room = challengeRepository.getRoomWithChallenge(roomId, userId).getOrThrow()
        val challenge = room.challenge ?: return@runCatching

        val isBothPhotosDone = challenge.userPhotoUrl != null && challenge.friendPhotoUrl != null
        val isTimeValid = System.currentTimeMillis() <= (challenge.expiresAt ?: 0L)

        if (isBothPhotosDone && isTimeValid) {
            challengeRepository.updateStreak(
                roomId = roomId,
                streakCount = room.streakCount + 1,
                isStreakActive = true
            )
        }
    }
}