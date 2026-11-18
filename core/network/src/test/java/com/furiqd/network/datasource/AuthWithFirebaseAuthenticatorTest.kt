package com.furiqd.network.datasource

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthWithFirebaseAuthenticatorTest {
    @Test
    fun networkFailureIsReconciledByConfirmedSignIn() = runBlocking {
        val registrationError = IllegalStateException("request timed out")
        var confirmationCalls = 0

        val result = registerOrReconcile(
            createAccount = { throw registrationError },
            confirmExistingAccount = { confirmationCalls++ },
            isUncertainOutcome = { it === registrationError }
        )

        assertTrue(result.isSuccess)
        assertEquals(1, confirmationCalls)
    }

    @Test
    fun knownNonRetryableFailureDoesNotAttemptSignIn() = runBlocking {
        val providerError = IllegalArgumentException("invalid email")
        var confirmationCalls = 0

        val result = registerOrReconcile(
            createAccount = { throw providerError },
            confirmExistingAccount = { confirmationCalls++ }
        )

        assertSame(providerError, result.exceptionOrNull())
        assertEquals(0, confirmationCalls)
    }

    @Test
    fun failedReconciliationPreservesOriginalError() = runBlocking {
        val registrationError = IllegalStateException("request outcome unknown")
        val confirmationError = IllegalStateException("credentials rejected")

        val result = registerOrReconcile(
            createAccount = { throw registrationError },
            confirmExistingAccount = { throw confirmationError },
            isUncertainOutcome = { it === registrationError }
        )

        assertSame(registrationError, result.exceptionOrNull())
        assertEquals(listOf(confirmationError), registrationError.suppressed.toList())
    }

    @Test
    fun cancellationIsPropagatedAndNeverReconciled() = runBlocking {
        val cancellation = CancellationException("cancelled")
        var confirmationCalls = 0

        val thrown = try {
            registerOrReconcile(
                createAccount = { throw cancellation },
                confirmExistingAccount = { confirmationCalls++ }
            )
            null
        } catch (error: CancellationException) {
            error
        }

        assertSame(cancellation, thrown)
        assertEquals(0, confirmationCalls)
    }
}
