package com.furiqd.personalisation.presentation.screen.onboarding

import com.furiqd.personalisation.domain.RecordingUserRepository
import com.furiqd.personalisation.domain.SaveUserUseCase
import com.furiqd.shared.domain.model.User
import com.furiqd.shared.domain.repository.StorageRepository
import com.furiqd.shared.domain.usecase.UpdateAvatarUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun profileCompletionCanRetryWithoutChangingIdentity() = runTest {
        val signedInUser = User(
            id = "uid-42",
            name = "",
            avatarId = null,
            isOnboardingCompleted = false,
            roomIds = emptyList(),
            email = "alice@example.com",
            nickName = null
        )
        val repository = RecordingUserRepository(
            currentUser = signedInUser,
            updateResults = mutableListOf(
                Result.success(Unit),
                Result.failure(IllegalStateException("profile write failed")),
                Result.success(Unit),
                Result.success(Unit)
            )
        )
        val viewModel = OnboardingViewModel(
            saveUserUseCase = SaveUserUseCase(repository),
            userRepository = repository,
            updateAvatarUseCase = UpdateAvatarUseCase(repository, FakeStorageRepository())
        )
        val effects = mutableListOf<OnboardingEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effect.collect(effects::add)
        }

        viewModel.handleEvent(OnboardingEvent.NameChanged("Alice Smith"))
        viewModel.handleEvent(OnboardingEvent.NicknameChanged("alice_42"))
        viewModel.handleEvent(OnboardingEvent.CustomAvatarSelected(byteArrayOf(1), "avatar://local"))
        assertTrue(viewModel.uiState.value.isNextButtonEnabled)

        viewModel.handleEvent(OnboardingEvent.NextClicked)
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.isNextButtonEnabled)
        assertTrue(effects.any { it is OnboardingEffect.ShowError })

        viewModel.handleEvent(OnboardingEvent.NextClicked)
        advanceUntilIdle()

        val completedProfiles = repository.updatedUsers.filter { it.isOnboardingCompleted }
        assertEquals(2, completedProfiles.size)
        assertEquals(listOf("uid-42", "uid-42"), completedProfiles.map(User::id))
        assertEquals(
            listOf("alice@example.com", "alice@example.com"),
            completedProfiles.map(User::email)
        )
        assertTrue(effects.contains(OnboardingEffect.NavigateToNext))
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val dispatcher: TestDispatcher = StandardTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        kotlinx.coroutines.Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        kotlinx.coroutines.Dispatchers.resetMain()
    }
}

private class FakeStorageRepository : StorageRepository {
    override suspend fun uploadImage(
        bytes: ByteArray,
        path: String,
        bucketKey: String
    ): Result<String> = Result.success("avatar://remote")

    override suspend fun downloadImage(path: String): Result<ByteArray> =
        Result.failure(UnsupportedOperationException())
}
