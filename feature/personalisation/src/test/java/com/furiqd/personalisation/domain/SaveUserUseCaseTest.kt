package com.furiqd.personalisation.domain

import com.furiqd.shared.domain.model.User
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SaveUserUseCaseTest {
    @Test
    fun profileSaveCanBeRetriedForTheSameIdentity() = runBlocking {
        val repository = RecordingUserRepository(
            updateResults = mutableListOf(
                Result.failure(IllegalStateException("temporary write failure")),
                Result.success(Unit)
            )
        )
        val useCase = SaveUserUseCase(repository)
        val user = User(
            id = "uid-42",
            name = "Alice Smith",
            avatarId = null,
            isOnboardingCompleted = true,
            roomIds = emptyList(),
            email = "alice@example.com",
            nickName = "alice_42"
        )

        assertTrue(useCase(user).isFailure)
        assertTrue(useCase(user).isSuccess)

        assertEquals(2, repository.updateCalls)
        assertEquals(listOf("uid-42", "uid-42"), repository.updatedUsers.map(User::id))
    }

    @Test
    fun failedProfileSaveDoesNotChangeTheIdentityForRetry() = runBlocking {
        val repository = RecordingUserRepository(
            updateResults = mutableListOf(
                Result.failure(IllegalStateException("temporary write failure")),
                Result.success(Unit)
            )
        )
        val user = User(
            id = "uid-99",
            name = "Bob",
            avatarId = null,
            isOnboardingCompleted = true,
            roomIds = emptyList(),
            email = "bob@example.com",
            nickName = "bob_99"
        )
        val useCase = SaveUserUseCase(repository)

        assertFalse(useCase(user).isSuccess)
        useCase(user)

        assertEquals("uid-99", repository.updatedUsers.last().id)
        assertEquals("bob@example.com", repository.updatedUsers.last().email)
    }
}
