package com.furiqd.auth.domain.usecase

import com.furiqd.auth.domain.repository.AuthRepository
import com.furiqd.shared.domain.model.AuthResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordRecoveryUseCaseTest {

    @Test
    fun validEmailTrimsWhitespaceAndCallsRepository() = runBlocking {
        val fakeRepo = RecoveryFakeAuthRepository(Result.success(Unit))
        val useCase = SendPasswordResetEmailUseCase(fakeRepo)

        val result = useCase("  person@example.com  ")

        assertTrue(result.isSuccess)
        assertEquals("person@example.com", fakeRepo.lastSentEmail)
    }

    @Test
    fun providerFailureReturnsFailureResult() = runBlocking {
        val providerError = IllegalStateException("Too many requests")
        val fakeRepo = RecoveryFakeAuthRepository(Result.failure(providerError))
        val useCase = SendPasswordResetEmailUseCase(fakeRepo)

        val result = useCase("person@example.com")

        assertTrue(result.isFailure)
        assertSame(providerError, result.exceptionOrNull())
    }

    @Test
    fun cancellationExceptionIsReThrown() = runBlocking {
        val cancellation = CancellationException("cancelled")
        val fakeRepo = RecoveryFakeAuthRepository(Result.failure(cancellation))
        val useCase = SendPasswordResetEmailUseCase(fakeRepo)

        var wasCancelled = false
        try {
            useCase("person@example.com")
        } catch (e: CancellationException) {
            wasCancelled = true
            assertSame(cancellation, e)
        }

        assertTrue(wasCancelled)
    }
}

private class RecoveryFakeAuthRepository(
    private val sendEmailResult: Result<Unit>
) : AuthRepository {
    var lastSentEmail: String? = null

    override suspend fun signInWithEmailAndPassword(email: String, password: String): Result<Unit> =
        Result.failure(UnsupportedOperationException())

    override suspend fun signUpWithEmailAndPassword(email: String, password: String): Result<Unit> =
        Result.failure(UnsupportedOperationException())

    override suspend fun signInWithGoogle(idToken: String): Result<AuthResult> =
        Result.failure(UnsupportedOperationException())

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        lastSentEmail = email
        if (sendEmailResult.isFailure) {
            val exception = sendEmailResult.exceptionOrNull()!!
            if (exception is CancellationException) throw exception
            return sendEmailResult
        }
        return sendEmailResult
    }
}
