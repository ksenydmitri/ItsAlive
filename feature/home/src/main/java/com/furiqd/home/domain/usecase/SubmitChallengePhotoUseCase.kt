package com.furiqd.home.domain.usecase

import com.furiqd.home.domain.repository.HomeRepository
import com.furiqd.shared.domain.repository.UserRepository
import javax.inject.Inject

internal class SubmitChallengePhotoUseCase @Inject constructor(
    private val challengeRepository: HomeRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(
        challengeId: String,
        isHost: Boolean,
        photoBytes: ByteArray
    ): Result<Unit> {
        val userId = userRepository.getUser()?.id
            ?: return Result.failure(IllegalStateException("User not found"))

        return challengeRepository.submitChallengePhoto(challengeId, userId, isHost, photoBytes)
    }
}