package com.furiqd.auth.presentation.screens.signup

import com.furiqd.auth.domain.usecase.AuthUseCases
import com.furiqd.auth.domain.usecase.SendPasswordResetEmailUseCase
import com.furiqd.auth.domain.usecase.SignInWithEmailAndPasswordUseCase
import com.furiqd.auth.domain.usecase.SignInWithGoogleUseCase
import com.furiqd.auth.domain.usecase.SignUpWithEmailAndPasswordUseCase
import com.furiqd.auth.domain.model.PasswordStrength
import com.furiqd.auth.testing.FakeAuthRepository
import com.furiqd.auth.testing.FakeUserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SignUpViewModelPrivacyCheckTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun validCredentialsDoNotEnableSignUpUntilPrivacyIsAccepted() = runTest {
        val authRepository = FakeAuthRepository()
        val userRepository = FakeUserRepository()
        val viewModel = SignUpViewModel(
            AuthUseCases(
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
        )

        viewModel.handleEvent(SignUpEvent.EmailChanged("person@example.com"))
        viewModel.handleEvent(SignUpEvent.PasswordChanged("Password1"))
        viewModel.handleEvent(SignUpEvent.PrivacyChecked(false))
        runCurrent()

        assertEquals(PasswordStrength.MEDIUM, viewModel.uiState.value.passwordStrength)
        assertFalse(viewModel.uiState.value.isSignUpEnabled)

        viewModel.handleEvent(SignUpEvent.PrivacyChecked(true))
        runCurrent()

        assertTrue(viewModel.uiState.value.isSignUpEnabled)

        viewModel.handleEvent(SignUpEvent.PasswordChanged("Password1!"))
        runCurrent()
        assertEquals(PasswordStrength.STRONG, viewModel.uiState.value.passwordStrength)
    }
}
