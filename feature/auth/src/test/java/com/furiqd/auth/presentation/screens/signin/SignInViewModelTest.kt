package com.furiqd.auth.presentation.screens.signin

import com.furiqd.auth.domain.usecase.AuthUseCases
import com.furiqd.auth.domain.usecase.SignInWithEmailAndPasswordUseCase
import com.furiqd.auth.domain.usecase.SignInWithGoogleUseCase
import com.furiqd.auth.domain.usecase.SignUpWithEmailAndPasswordUseCase
import com.furiqd.auth.presentation.helper.AuthWithGoogleHelper
import com.furiqd.auth.presentation.screens.signup.MainDispatcherRule
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
class SignInViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun invalidFieldsPreventSubmission() = runTest {
        val authRepository = FakeAuthRepository()
        val viewModel = viewModel(authRepository)

        viewModel.handleEvent(SignInEvent.SignInClicked)
        runCurrent()
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(0, authRepository.signInCalls)

        viewModel.handleEvent(SignInEvent.EmailChanged("not-an-email"))
        viewModel.handleEvent(SignInEvent.PasswordChanged("short"))
        viewModel.handleEvent(SignInEvent.SignInClicked)
        runCurrent()

        assertFalse(viewModel.uiState.value.isSignInEnabled)
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(0, authRepository.signInCalls)
    }

    @Test
    fun submissionShowsLoadingAndIgnoresDuplicateTaps() = runTest {
        val gate = CompletableDeferred<Result<Unit>>()
        val authRepository = FakeAuthRepository(signInResult = { _, _ -> gate.await() })
        val viewModel = viewModel(authRepository)
        val effects = mutableListOf<SignInEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiEffect.collect(effects::add)
        }
        enterValidDetails(viewModel)

        viewModel.handleEvent(SignInEvent.SignInClicked)
        runCurrent()
        assertTrue(viewModel.uiState.value.isLoading)
        assertEquals(1, authRepository.signInCalls)

        viewModel.handleEvent(SignInEvent.SignInClicked)
        runCurrent()
        assertEquals(1, authRepository.signInCalls)

        gate.complete(Result.success(Unit))
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(listOf(SignInEffect.NavigateToWelcome(false)), effects)
    }

    @Test
    fun providerFailureIsShownAsSafeGenericMessage() = runTest {
        val authRepository = FakeAuthRepository(
            signInResult = { _, _ -> Result.failure(IllegalStateException("private provider details")) }
        )
        val viewModel = viewModel(authRepository)
        val effects = mutableListOf<SignInEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiEffect.collect(effects::add)
        }
        enterValidDetails(viewModel)

        viewModel.handleEvent(SignInEvent.SignInClicked)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(listOf(SignInEffect.ShowError("Authentication failed")), effects)
    }

    @Test
    fun networkFailureIsShownAsSafeGenericMessage() = runTest {
        var attempts = 0
        val authRepository = FakeAuthRepository(
            signInResult = { _, _ ->
                attempts++
                if (attempts == 1) {
                    Result.failure(java.io.IOException("private network details"))
                } else {
                    Result.success(Unit)
                }
            }
        )
        val viewModel = viewModel(authRepository)
        val effects = mutableListOf<SignInEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiEffect.collect(effects::add)
        }
        enterValidDetails(viewModel)

        viewModel.handleEvent(SignInEvent.SignInClicked)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(listOf(SignInEffect.ShowError("Authentication failed")), effects)

        viewModel.handleEvent(SignInEvent.SignInClicked)
        advanceUntilIdle()

        assertEquals(2, authRepository.signInCalls)
        assertEquals(
            listOf(
                SignInEffect.ShowError("Authentication failed"),
                SignInEffect.NavigateToWelcome(false)
            ),
            effects
        )
    }

    private fun enterValidDetails(viewModel: SignInViewModel) {
        viewModel.handleEvent(SignInEvent.EmailChanged("person@example.com"))
        viewModel.handleEvent(SignInEvent.PasswordChanged("Password1"))
        assertTrue(viewModel.uiState.value.isSignInEnabled)
    }

    private fun viewModel(authRepository: FakeAuthRepository): SignInViewModel {
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
            signInWithGoogle = SignInWithGoogleUseCase(authRepository, userRepository)
        )
        return SignInViewModel(useCases, AuthWithGoogleHelper())
    }
}
