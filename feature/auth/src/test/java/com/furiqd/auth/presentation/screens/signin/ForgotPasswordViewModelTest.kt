package com.furiqd.auth.presentation.screens.signin

import com.furiqd.auth.domain.usecase.AuthUseCases
import com.furiqd.auth.domain.usecase.SendPasswordResetEmailUseCase
import com.furiqd.auth.domain.usecase.SignInWithEmailAndPasswordUseCase
import com.furiqd.auth.domain.usecase.SignInWithGoogleUseCase
import com.furiqd.auth.domain.usecase.SignUpWithEmailAndPasswordUseCase
import com.furiqd.auth.presentation.screens.signup.MainDispatcherRule
import com.furiqd.auth.testing.FakeAuthRepository
import com.furiqd.auth.testing.FakeUserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ForgotPasswordViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun validRequestShowsNeutralConfirmationAndCountsDownCooldown() = runTest {
        val authRepository = FakeAuthRepository()
        val viewModel = viewModel(authRepository)
        viewModel.handleEvent(ForgotPasswordEvent.EmailChanged("person@example.com"))

        assertTrue(viewModel.uiState.value.isSubmitEnabled)

        viewModel.handleEvent(ForgotPasswordEvent.SendResetClicked)
        runCurrent()

        assertEquals(1, authRepository.passwordResetCalls)
        assertEquals("person@example.com", authRepository.lastResetEmail)
        assertTrue(viewModel.uiState.value.isEmailSent)
        assertEquals(60, viewModel.uiState.value.cooldownRemainingSeconds)
        assertFalse(viewModel.uiState.value.isSubmitEnabled)

        advanceTimeBy(1_000)
        runCurrent()
        assertEquals(59, viewModel.uiState.value.cooldownRemainingSeconds)

        advanceTimeBy(59_000)
        runCurrent()
        assertEquals(0, viewModel.uiState.value.cooldownRemainingSeconds)
        assertTrue(viewModel.uiState.value.isSubmitEnabled)
    }

    @Test
    fun malformedEmailCannotBeSubmitted() = runTest {
        val authRepository = FakeAuthRepository()
        val viewModel = viewModel(authRepository)
        viewModel.handleEvent(ForgotPasswordEvent.EmailChanged("not-an-email"))
        viewModel.handleEvent(ForgotPasswordEvent.SendResetClicked)
        runCurrent()

        assertFalse(viewModel.uiState.value.isSubmitEnabled)
        assertEquals(0, authRepository.passwordResetCalls)
    }

    @Test
    fun providerFailureIsSanitizedAndAllowsRetry() = runTest {
        val authRepository = FakeAuthRepository(
            sendPasswordResetEmailResult = {
                Result.failure(IllegalStateException("unknown account"))
            }
        )
        val viewModel = viewModel(authRepository)
        val effects = mutableListOf<ForgotPasswordEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiEffect.collect(effects::add)
        }
        viewModel.handleEvent(ForgotPasswordEvent.EmailChanged("person@example.com"))
        viewModel.handleEvent(ForgotPasswordEvent.SendResetClicked)
        runCurrent()

        assertFalse(viewModel.uiState.value.isLoading)
        assertFalse(viewModel.uiState.value.isEmailSent)
        assertTrue(viewModel.uiState.value.isSubmitEnabled)
        assertEquals(
            listOf(ForgotPasswordEffect.ShowError("Authentication failed")),
            effects
        )
    }

    private fun viewModel(authRepository: FakeAuthRepository): ForgotPasswordViewModel {
        val userRepository = FakeUserRepository()
        val useCases = AuthUseCases(
            signInWithEmailAndPassword = SignInWithEmailAndPasswordUseCase(
                authRepository,
                userRepository
            ),
            signUpWithEmailAndPassword = SignUpWithEmailAndPasswordUseCase(
                authRepository,
                userRepository
            ),
            signInWithGoogle = SignInWithGoogleUseCase(authRepository, userRepository),
            sendPasswordResetEmail = SendPasswordResetEmailUseCase(authRepository)
        )
        return ForgotPasswordViewModel(useCases)
    }
}
