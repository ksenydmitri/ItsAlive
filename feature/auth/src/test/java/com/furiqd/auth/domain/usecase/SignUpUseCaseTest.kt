package com.furiqd.auth.domain.usecase

import com.furiqd.auth.testing.FakeAuthRepository
import com.furiqd.auth.testing.FakeUserRepository
import com.furiqd.shared.domain.model.AuthResult
import com.furiqd.shared.domain.model.User
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlinx.coroutines.runBlocking

class SignUpUseCaseTest {
    @Test
    fun providerConfirmedRegistrationSavesAnIncompleteProfile() = runBlocking {
        val authRepository = FakeAuthRepository()
        val userRepository = FakeUserRepository()

        val result = SignUpWithEmailAndPasswordUseCase(authRepository, userRepository)(
            "person@example.com",
            "Password1"
        )

        assertTrue(result.isSuccess)
        assertFalse(result.getOrThrow())
        assertEquals(1, authRepository.signUpCalls)
        assertEquals("person@example.com", userRepository.savedUsers.single().email)
        assertFalse(userRepository.savedUsers.single().isOnboardingCompleted)
    }

    @Test
    fun providerRejectionDoesNotSaveAProfile() = runBlocking {
        val providerError = IllegalStateException("network unavailable")
        val authRepository = FakeAuthRepository(signUpResult = { _, _ -> Result.failure(providerError) })
        val userRepository = FakeUserRepository()

        val result = SignUpWithEmailAndPasswordUseCase(authRepository, userRepository)(
            "person@example.com",
            "Password1"
        )

        assertTrue(result.isFailure)
        assertSame(providerError, result.exceptionOrNull())
        assertTrue(userRepository.savedUsers.isEmpty())
    }

    @Test
    fun profilePersistenceFailureIsReturnedToTheCaller() = runBlocking {
        val persistenceError = IllegalStateException("profile write failed")
        val authRepository = FakeAuthRepository()
        val userRepository = FakeUserRepository(saveUserResult = Result.failure(persistenceError))

        val result = SignUpWithEmailAndPasswordUseCase(
            authRepository,
            userRepository
        )("person@example.com", "Password1")

        assertTrue(result.isFailure)
        assertSame(persistenceError, result.exceptionOrNull())
        assertEquals(1, authRepository.signUpCalls)
        assertEquals(1, userRepository.savedUsers.size)
    }

    @Test
    fun profileRecoveryReusesTheConfirmedIdentityWithoutSigningUpAgain() = runBlocking {
        val providerUser = User(
            id = "uid-42",
            name = "",
            avatarId = null,
            isOnboardingCompleted = false,
            roomIds = emptyList(),
            email = "person@example.com",
            nickName = null
        )
        val authRepository = FakeAuthRepository()
        val userRepository = FakeUserRepository(
            saveUserResult = Result.failure(IllegalStateException("profile write outcome unknown")),
            currentUser = providerUser
        )

        val registration = SignUpWithEmailAndPasswordUseCase(authRepository, userRepository)(
            "person@example.com",
            "Password1"
        )
        val profileRetry = userRepository.updateUser(
            providerUser.copy(name = "Person", nickName = "person_42", isOnboardingCompleted = true)
        )

        assertTrue(registration.isFailure)
        assertTrue(profileRetry.isSuccess)
        assertEquals(1, authRepository.signUpCalls)
        assertEquals("uid-42", userRepository.updatedUsers.single().id)
    }

    @Test
    fun ambiguousProviderFailureDoesNotPretendRegistrationSucceeded() = runBlocking {
        val providerError = IllegalStateException("request outcome unknown")
        val authRepository = FakeAuthRepository(signUpResult = { _, _ -> Result.failure(providerError) })
        val userRepository = FakeUserRepository()

        val result = SignUpWithEmailAndPasswordUseCase(authRepository, userRepository)(
            "person@example.com",
            "Password1"
        )

        assertTrue(result.isFailure)
        assertSame(providerError, result.exceptionOrNull())
        assertTrue(userRepository.savedUsers.isEmpty())
    }

    @Test
    fun googleRegistrationRequiresProfilePersistenceToSucceed() = runBlocking {
        val persistenceError = IllegalStateException("profile write failed")
        val authRepository = FakeAuthRepository(
            googleSignInResult = {
                Result.success(AuthResult(email = "person@example.com", name = "Person"))
            }
        )
        val userRepository = FakeUserRepository(
            saveUserResult = Result.failure(persistenceError)
        )

        val result = SignInWithGoogleUseCase(authRepository, userRepository)("google-id-token")

        assertTrue(result.isFailure)
        assertSame(persistenceError, result.exceptionOrNull())
        assertEquals(1, authRepository.googleSignInCalls)
        assertEquals("person@example.com", userRepository.savedUsers.single().email)
    }

    @Test
    fun googleRegistrationDoesNotCreateProfileWhenSessionSyncFails() = runBlocking {
        val syncError = IllegalStateException("session profile could not be loaded")
        val authRepository = FakeAuthRepository(
            googleSignInResult = {
                Result.success(AuthResult(email = "person@example.com", name = "Person"))
            }
        )
        val userRepository = FakeUserRepository(syncSessionResult = Result.failure(syncError))

        val result = SignInWithGoogleUseCase(authRepository, userRepository)("google-id-token")

        assertTrue(result.isFailure)
        assertSame(syncError, result.exceptionOrNull())
        assertTrue(userRepository.savedUsers.isEmpty())
    }
}
