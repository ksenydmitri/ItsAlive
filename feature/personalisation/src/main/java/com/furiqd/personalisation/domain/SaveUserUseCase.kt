package com.furiqd.personalisation.domain

import com.furiqd.shared.domain.model.User
import com.furiqd.shared.domain.repository.UserRepository
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

class SaveUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(user: User): Result<Unit> {
        val existingUser = try {
            userRepository.getUser()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: Exception) {
            return Result.failure(error)
        }
        val profile = ProfileValidator.validate(
            name = user.name,
            nickName = user.nickName,
            unchangedExistingNickName = existingUser?.nickName
        )
        if (profile.isFailure) {
            return Result.failure(profile.exceptionOrNull()!!)
        }

        val validatedProfile = profile.getOrThrow()
        return userRepository.updateUser(
            user.copy(
                name = validatedProfile.name,
                nickName = validatedProfile.nickName
            )
        )
    }
}