package com.furiqd.auth.domain.usecase

import com.furiqd.auth.domain.repository.AuthRepository
import com.furiqd.shared.domain.model.AuthResult
import com.furiqd.shared.domain.model.User
import com.furiqd.shared.domain.model.UserSessionState
import com.furiqd.shared.domain.repository.UserRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class SignInUseCaseTest {
    @Test
    fun providerRejectionDoesNotPersistTokenOrSyncSession() = runBlocking {
        val providerError = IllegalStateException("Invalid credentials")
        val authRepository = FakeAuthRepository(Result.failure(providerError))
        val userRepository = FakeUserRepository()

        val result = SignInWithEmailAndPasswordUseCase(
            authRepository = authRepository,
            userRepository = userRepository
        )(email = "person@example.com", password = "Password1")

        assertTrue(result.isFailure)
        assertSame(providerError, result.exceptionOrNull())
        assertEquals(0, userRepository.syncSessionCalls)
        assertEquals(0, userRepository.onboardingChecks)
    }

    @Test
    fun providerConfirmedAccountWithLegacyNicknameCanSignIn() = runBlocking {
        val legacyProfile = User(
            id = "uid-1",
            name = "Person",
            avatarId = null,
            isOnboardingCompleted = true,
            roomIds = emptyList(),
            email = "person@example.com",
            nickName = "Legacy Name!"
        )
        val authRepository = FakeAuthRepository(Result.success(Unit))
        val userRepository = FakeUserRepository(user = legacyProfile)

        val result = SignInWithEmailAndPasswordUseCase(
            authRepository = authRepository,
            userRepository = userRepository
        )(email = "person@example.com", password = "Password1")

        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow())
        assertEquals("person@example.com", authRepository.lastSignInEmail)
        assertEquals(1, userRepository.syncSessionCalls)
        assertEquals(1, userRepository.onboardingChecks)
    }

    @Test
    fun cancellationIsPropagatedWithoutPersistingTokenOrSyncingSession() = runBlocking {
        val cancellation = CancellationException("cancelled")
        val authRepository = FakeAuthRepository(Result.failure(cancellation))
        val userRepository = FakeUserRepository()

        var thrown: CancellationException? = null
        try {
            SignInWithEmailAndPasswordUseCase(
                authRepository = authRepository,
                userRepository = userRepository
            )(email = "person@example.com", password = "Password1")
        } catch (error: CancellationException) {
            thrown = error
        }

        assertSame(cancellation, thrown)
        assertEquals(0, userRepository.syncSessionCalls)
        assertEquals(0, userRepository.onboardingChecks)
    }
}

private class FakeAuthRepository(
    private val signInResult: Result<Unit>
) : AuthRepository {
    var lastSignInEmail: String? = null
        private set

    override suspend fun signInWithEmailAndPassword(email: String, password: String): Result<Unit> {
        lastSignInEmail = email
        return signInResult
    }

    override suspend fun signUpWithEmailAndPassword(email: String, password: String): Result<Unit> =
        Result.failure(UnsupportedOperationException())

    override suspend fun signInWithGoogle(idToken: String): Result<AuthResult> =
        Result.failure(UnsupportedOperationException())
}

private class FakeUserRepository(
    private val user: User? = null
) : UserRepository {
    override val sessionState = MutableStateFlow<UserSessionState>(UserSessionState.NotAuthorized)
    var syncSessionCalls = 0
        private set
    var onboardingChecks = 0
        private set

    override fun observeUser() = kotlinx.coroutines.flow.emptyFlow<User>()
    override suspend fun getUser(): User? = user
    override suspend fun syncSession(): Result<Unit> {
        syncSessionCalls++
        return Result.success(Unit)
    }

    override suspend fun saveUser(user: User): Result<Unit> = Result.failure(UnsupportedOperationException())
    override suspend fun logOut(): Result<Unit> = Result.failure(UnsupportedOperationException())
    override suspend fun checkIsOnboardingCompleted(): Boolean {
        onboardingChecks++
        return user?.isOnboardingCompleted ?: false
    }

    override suspend fun updateUser(user: User): Result<Unit> = Result.failure(UnsupportedOperationException())
    override suspend fun getUserById(userId: String): User? = null
    override suspend fun updateFcmToken(token: String): Result<Unit> = Result.failure(UnsupportedOperationException())
}
