package com.furiqd.auth.domain.usecase

import com.furiqd.auth.domain.repository.AuthRepository
import com.furiqd.shared.domain.model.User
import com.furiqd.shared.domain.repository.UserRepository
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

internal class SignInWithEmailAndPasswordUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<Boolean> {
        return try {
            authRepository.signInWithEmailAndPassword(email, password).getOrThrow()
            userRepository.syncSession()

            val isOnboardingCompleted = userRepository.checkIsOnboardingCompleted()
            Result.success(isOnboardingCompleted)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

internal class SignUpWithEmailAndPasswordUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<Boolean> {
        return try {
            authRepository.signUpWithEmailAndPassword(email, password).getOrThrow()
            userRepository.saveUser(
                User(
                    id = "",
                    name = "",
                    avatarId = null,
                    isOnboardingCompleted = false,
                    roomIds = emptyList(),
                    email = email,
                    nickName = null
                )
            ).getOrThrow()
            Result.success(false)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

internal class SignInWithGoogleUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(idToken: String): Result<Boolean> = try {
        val authResult = authRepository.signInWithGoogle(idToken).getOrThrow()
        userRepository.syncSession().getOrThrow()
        val existingUser = userRepository.getUser()

        val isOnboardingCompleted = if (existingUser == null) {
            val newUser = User(
                id = "",
                name = authResult.name.orEmpty(),
                avatarId = null,
                isOnboardingCompleted = false,
                roomIds = emptyList(),
                email = authResult.email,
                nickName = null
            )
            userRepository.saveUser(newUser).getOrThrow()
            false
        } else {
            existingUser.isOnboardingCompleted
        }

        Result.success(isOnboardingCompleted)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
}

internal class SendPasswordResetEmailUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String): Result<Unit> = try {
        authRepository.sendPasswordResetEmail(email.trim())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
}

internal data class AuthUseCases @Inject constructor(
    val signInWithEmailAndPassword: SignInWithEmailAndPasswordUseCase,
    val signUpWithEmailAndPassword: SignUpWithEmailAndPasswordUseCase,
    val signInWithGoogle: SignInWithGoogleUseCase,
    val sendPasswordResetEmail: SendPasswordResetEmailUseCase
)