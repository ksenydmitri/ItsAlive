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
    ): Result<Unit> {
        Log.e("SubmitChallengePhotoUseCase", "invoke: $roomId, $photoPath, $userId")
        return challengeRepository.submitChallengePhoto(
            roomId,
            photoPath,
            userId)
    }
}