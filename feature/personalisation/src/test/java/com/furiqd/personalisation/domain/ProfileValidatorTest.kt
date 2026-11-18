package com.furiqd.personalisation.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileValidatorTest {
    @Test
    fun nameIsNormalizedBeforeValidation() {
        val result = ProfileValidator.validate("  Jose\u0301   Smith  ", "User_1")

        assertTrue(result.isSuccess)
        assertEquals("José Smith", result.getOrThrow().name)
        assertEquals("user_1", result.getOrThrow().nickName)
    }

    @Test
    fun nameLengthUsesUnicodeCodePoints() {
        val supplementaryLetter = String(Character.toChars(0x10400))

        assertTrue(ProfileValidator.validate(supplementaryLetter + "a", "ab").isSuccess)
        assertEquals(
            ProfileValidationReason.TOO_SHORT,
            ProfileValidator.validate(supplementaryLetter, "ab").exceptionOrNull()
                ?.let { (it as ProfileValidationException).error.reason }
        )
    }

    @Test
    fun nameOnlyAllowsLettersWithInternalApprovedSeparators() {
        assertTrue(ProfileValidator.validate("Jean-Luc O'Neil", "ab").isSuccess)
        assertTrue(ProfileValidator.validate("Мария", "ab").isSuccess)
        assertFalse(ProfileValidator.validate("-Jean", "ab").isSuccess)
        assertFalse(ProfileValidator.validate("Jean--Luc", "ab").isSuccess)
        assertFalse(ProfileValidator.validate("Jean’Luc", "ab").isSuccess)
        assertFalse(ProfileValidator.validate("Jean2", "ab").isSuccess)
        assertFalse(ProfileValidator.validate("Jean\nLuc", "ab").isSuccess)
    }

    @Test
    fun nameEnforcesNormalizedCodePointBoundaries() {
        assertEquals(
            ProfileValidationReason.TOO_SHORT,
            ProfileValidator.validate("A", "ab").exceptionOrNull()
                ?.let { (it as ProfileValidationException).error.reason }
        )
        assertTrue(ProfileValidator.validate("A".repeat(30), "ab").isSuccess)
        assertEquals(
            ProfileValidationReason.TOO_LONG,
            ProfileValidator.validate("A".repeat(31), "ab").exceptionOrNull()
                ?.let { (it as ProfileValidationException).error.reason }
        )
        assertFalse(ProfileValidator.validate("  ", "ab").isSuccess)
    }

    @Test
    fun nicknameIsLowercasedAndRejectsUnsupportedCharacters() {
        val valid = ProfileValidator.validate("Alice", "A.User_09")

        assertTrue(valid.isSuccess)
        assertEquals("a.user_09", valid.getOrThrow().nickName)
        assertFalse(ProfileValidator.validate("Alice", "ab-cd").isSuccess)
        assertFalse(ProfileValidator.validate("Alice", "яa").isSuccess)
        assertFalse(ProfileValidator.validate("Alice", "a b").isSuccess)
        assertFalse(ProfileValidator.validate("Alice", "a").isSuccess)
        assertTrue(ProfileValidator.validate("Alice", "a".repeat(24)).isSuccess)
        assertFalse(ProfileValidator.validate("Alice", "a".repeat(25)).isSuccess)
        assertTrue(ProfileValidator.validate("Alice", "._").isSuccess)
    }

    @Test
    fun nicknameDoesNotIncludeDisplayPrefix() {
        assertFalse(ProfileValidator.validate("Alice", "@alice").isSuccess)
    }

    @Test
    fun validationFailsBeforeProfilePersistence() = kotlinx.coroutines.runBlocking {
        val repository = RecordingUserRepository()
        val useCase = SaveUserUseCase(repository)

        val result = useCase(
            com.furiqd.shared.domain.model.User(
                id = "",
                name = "1",
                avatarId = null,
                isOnboardingCompleted = false,
                roomIds = emptyList(),
                email = null,
                nickName = "ab"
            )
        )

        assertTrue(result.isFailure)
        assertEquals(0, repository.updateCalls)
    }

    @Test
    fun validatedProfileIsNormalizedBeforePersistence() = kotlinx.coroutines.runBlocking {
        val repository = RecordingUserRepository()
        val useCase = SaveUserUseCase(repository)

        val result = useCase(
            com.furiqd.shared.domain.model.User(
                id = "",
                name = "  Alice   Smith ",
                avatarId = null,
                isOnboardingCompleted = false,
                roomIds = emptyList(),
                email = null,
                nickName = "Alice_1"
            )
        )

        assertTrue(result.isSuccess)
        assertEquals("Alice Smith", repository.updatedUser?.name)
        assertEquals("alice_1", repository.updatedUser?.nickName)
    }

    @Test
    fun unchangedLegacyNicknameRemainsValidWhenSavingOtherProfileFields() =
        kotlinx.coroutines.runBlocking {
            val legacyUser = com.furiqd.shared.domain.model.User(
                id = "uid-1",
                name = "Old name",
                avatarId = null,
                isOnboardingCompleted = false,
                roomIds = emptyList(),
                email = null,
                nickName = "Legacy Name!"
            )
            val repository = RecordingUserRepository(legacyUser)

            val result = SaveUserUseCase(repository)(legacyUser.copy(name = "New name"))

            assertTrue(result.isSuccess)
            assertEquals("Legacy Name!", repository.updatedUser?.nickName)
        }

    @Test
    fun changingLegacyNicknameRequiresTheNewFormat() = kotlinx.coroutines.runBlocking {
        val legacyUser = com.furiqd.shared.domain.model.User(
            id = "uid-1",
            name = "Old name",
            avatarId = null,
            isOnboardingCompleted = false,
            roomIds = emptyList(),
            email = null,
            nickName = "Legacy Name!"
        )
        val repository = RecordingUserRepository(legacyUser)

        val result = SaveUserUseCase(repository)(
            legacyUser.copy(name = "New name", nickName = "Still Invalid!")
        )

        assertTrue(result.isFailure)
        assertEquals(0, repository.updateCalls)
    }
}
