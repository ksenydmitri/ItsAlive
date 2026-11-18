package com.furiqd.auth.presentation.helper

import org.junit.Assert.assertEquals
import org.junit.Test

class ErrorMapperTest {
    @Test
    fun sanitizedMessage_hidesProviderDetails() {
        val throwable = IllegalStateException("Firebase: invalid credentials")

        assertEquals("Authentication failed", throwable.sanitizedMessage())
    }

    @Test
    fun sanitizedMessage_hidesUnknownProviderMessage() {
        val throwable = IllegalStateException("User not found")

        assertEquals("Authentication failed", throwable.sanitizedMessage())
    }

    @Test
    fun sanitizedMessage_hidesArbitrarySensitiveDetails() {
        val throwable = IllegalStateException("Request failed for person@example.com: internal host auth-01")

        assertEquals("Authentication failed", throwable.sanitizedMessage())
    }

    @Test
    fun sanitizedMessage_returnsGenericFailureForBlankMessage() {
        val throwable = IllegalStateException()

        assertEquals("Authentication failed", throwable.sanitizedMessage())
    }
}
