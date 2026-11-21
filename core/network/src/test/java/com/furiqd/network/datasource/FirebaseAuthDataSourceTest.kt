package com.furiqd.network.datasource

import com.furiqd.network.dto.UserDto
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.fail
import org.junit.Test

class FirebaseAuthDataSourceTest {
    @Test
    fun missingProfileReturnsNullWithoutDecoding() {
        val profile = decodeUserProfile(documentExists = false) {
            fail("Missing documents must not be decoded")
            null
        }

        assertNull(profile)
    }

    @Test
    fun existingProfileIsDecoded() {
        val expected = UserDto(id = "uid", name = "Buddy")

        val profile = decodeUserProfile(documentExists = true) { expected }

        assertSame(expected, profile)
    }

    @Test
    fun nonFirestoreFailureIsNotReclassified() = runBlocking {
        val failure = IllegalStateException("failure")

        try {
            withFirestoreErrors<UserDto?> { throw failure }
            fail("Expected failure")
        } catch (error: IllegalStateException) {
            assertSame(failure, error)
        }
    }

    @Test
    fun cancellationIsPropagatedUnchanged() = runBlocking {
        val cancellation = CancellationException("cancelled")

        try {
            withFirestoreErrors<Unit> { throw cancellation }
            fail("Expected cancellation")
        } catch (error: CancellationException) {
            assertSame(cancellation, error)
        }
    }

    @Test
    fun fcmRegistrationUsesTheActiveUserAndDeviceDetails() {
        val request = createDeviceRegistrationRequest(
            userId = "uid",
            token = "synthetic-token",
            deviceId = "synthetic-device",
            osVersion = "15"
        )

        assertEquals("uid", request.userId)
        assertEquals("synthetic-token", request.deviceToken)
        assertEquals("synthetic-device", request.deviceId)
        assertEquals("ANDROID", request.os)
        assertEquals("15", request.osVersion)
        assertEquals(listOf("default"), request.channels)
    }
}
