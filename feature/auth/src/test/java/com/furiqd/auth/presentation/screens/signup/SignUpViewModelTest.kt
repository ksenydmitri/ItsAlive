package com.furiqd.auth.presentation.screens.signup

import com.furiqd.auth.domain.usecase.AuthUseCases
import com.furiqd.auth.domain.usecase.SignInWithEmailAndPasswordUseCase
import com.furiqd.auth.domain.usecase.SignInWithGoogleUseCase
import com.furiqd.auth.domain.usecase.SignUpWithEmailAndPasswordUseCase
import com.furiqd.auth.testing.FakeAuthRepository
import com.furiqd.auth.testing.FakeUserRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SignUpViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun requiredFieldsMustBeValidAndPrivacyAcceptedBeforeSubmission() = runTest {
        val authRepository = FakeAuthRepository()
        val viewModel = viewModel(authRepository, FakeUserRepository())

        viewModel.handleEvent(SignUpEvent.EmailChanged(""))
        viewModel.handleEvent(SignUpEvent.PasswordChanged(""))
        viewModel.handleEvent(SignUpEvent.SignUpClicked)
        runCurrent()

        assertTrue(viewModel.uiState.value.emailError != null)
        assertTrue(viewModel.uiState.value.passwordError != null)
        assertFalse(viewModel.uiState.value.isSignUpEnabled)
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(0, authRepository.signUpCalls)

        enterValidDetails(viewModel)
        viewModel.handleEvent(SignUpEvent.PrivacyChecked(false))
        viewModel.handleEvent(SignUpEvent.SignUpClicked)
        runCurrent()

        assertFalse(viewModel.uiState.value.isSignUpEnabled)
        assertEquals(0, authRepository.signUpCalls)
    }

    @Test
    fun submissionShowsLoadingAndIgnoresDuplicateTaps() = runTest {
        val gate = CompletableDeferred<Result<Unit>>()
        val authRepository = FakeAuthRepository(signUpResult = { _, _ -> gate.await() })
        val viewModel = viewModel(authRepository, FakeUserRepository())
        val effects = mutableListOf<SignUpEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiEffect.collect(effects::add)
        }
        enterValidDetails(viewModel)

        viewModel.handleEvent(SignUpEvent.SignUpClicked)
        runCurrent()
        assertTrue(viewModel.uiState.value.isLoading)
        assertEquals(1, authRepository.signUpCalls)

        viewModel.handleEvent(SignUpEvent.SignUpClicked)
        runCurrent()
        assertEquals(1, authRepository.signUpCalls)

        gate.complete(Result.success(Unit))
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(listOf(SignUpEffect.NavigateToWelcome(false)), effects)
    }

    @Test
    fun providerFailureIsShownAsSafeGenericMessage() = runTest {
        val authRepository = FakeAuthRepository(signUpResult = { _, _ ->
            Result.failure(IllegalStateException("private provider details"))
        })
        val viewModel = viewModel(authRepository, FakeUserRepository())
        val effects = mutableListOf<SignUpEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiEffect.collect(effects::add)
        }
        enterValidDetails(viewModel)

        viewModel.handleEvent(SignUpEvent.SignUpClicked)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(listOf(SignUpEffect.ShowError("Authentication failed")), effects)
    }

    @Test
    fun duplicateGoogleLaunchIsIgnoredAndCancellationAllowsAnotherAttempt() = runTest {
        val viewModel = viewModel(FakeAuthRepository(), FakeUserRepository())
        val effects = mutableListOf<SignUpEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiEffect.collect(effects::add)
        }

        viewModel.handleEvent(SignUpEvent.GoogleClicked)
        viewModel.handleEvent(SignUpEvent.GoogleClicked)
        runCurrent()
        assertEquals(listOf(SignUpEffect.LaunchGoogleAuth), effects)

        viewModel.onGoogleAuthCancelled()
        viewModel.handleEvent(SignUpEvent.GoogleClicked)
        advanceUntilIdle()

        assertEquals(2, effects.size)
        assertTrue(effects.all { it == SignUpEffect.LaunchGoogleAuth })
    }

    private fun enterValidDetails(viewModel: SignUpViewModel) {
        viewModel.handleEvent(SignUpEvent.EmailChanged("person@example.com"))
        viewModel.handleEvent(SignUpEvent.PasswordChanged("Password1"))
        viewModel.handleEvent(SignUpEvent.PrivacyChecked(true))
        assertTrue(viewModel.uiState.value.isSignUpEnabled)
    }

    private fun viewModel(
        authRepository: FakeAuthRepository,
        userRepository: FakeUserRepository
    ): SignUpViewModel {
        val useCases = AuthUseCases(
            signInWithEmailAndPassword = SignInWithEmailAndPasswordUseCase(
                authRepository,
                userRepository
            ),
            signUpWithEmailAndPassword = SignUpWithEmailAndPasswordUseCase(
                authRepository,
                userRepository
            ),
            signInWithGoogle = SignInWithGoogleUseCase(authRepository, userRepository)
        )
        return SignUpViewModel(useCases)
    }
}
