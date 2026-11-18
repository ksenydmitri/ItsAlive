package com.furiqd.auth.presentation.screens.signin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.furiqd.auth.domain.usecase.AuthUseCases
import com.furiqd.auth.presentation.helper.AuthWithGoogleHelper
import com.furiqd.auth.presentation.helper.AuthWithGoogleHelper.AuthUiResult
import com.furiqd.auth.presentation.helper.InputValidator
import com.furiqd.auth.presentation.helper.sanitizedMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class SignInViewModel @Inject constructor(
    private val authUseCases: AuthUseCases,
    private val authHelper: AuthWithGoogleHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignInState())
    val uiState = _uiState.asStateFlow()

    private val _uiEffect = Channel<SignInEffect>()
    val uiEffect = _uiEffect.receiveAsFlow()

    fun handleEvent(event: SignInEvent, context: android.content.Context? = null) {
        when (event) {
            is SignInEvent.GoogleClicked -> {
                if (!_uiState.value.isLoading) {
                    context?.let { signInWithGoogle(it) }
                }
            }
            is SignInEvent.SignInClicked -> {
                val currentState = _uiState.value
                if (currentState.isLoading || !currentState.canSignIn()) return
                _uiState.update { it.copy(isLoading = true) }
                signInWithEmail(currentState.email, currentState.password)
            }
            is SignInEvent.EmailChanged -> {
                val error = InputValidator.validateEmail(event.email)
                _uiState.update { state ->
                    val newState = state.copy(email = event.email, emailError = error)
                    newState.copy(isSignInEnabled = newState.canSignIn())
                }
            }
            is SignInEvent.PasswordChanged -> {
                val error = InputValidator.validatePassword(event.password)
                _uiState.update { state ->
                    val newState = state.copy(password = event.password, passwordError = error)
                    newState.copy(isSignInEnabled = newState.canSignIn())
                }
            }
            is SignInEvent.GoToSignUpClicked -> {
                viewModelScope.launch { _uiEffect.send(SignInEffect.NavigateToSignUp) }
            }
            is SignInEvent.BackClicked -> {
                viewModelScope.launch { _uiEffect.send(SignInEffect.NavigateBack) }
            }
            is SignInEvent.ForgotPasswordClicked -> {
                viewModelScope.launch { _uiEffect.send(SignInEffect.NavigateToForgotPassword) }
            }
        }
    }
    private fun signInWithGoogle(context: android.content.Context) {
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            when (val uiResult = authHelper.launchAuthUi(context)) {
                is AuthUiResult.GoogleSuccess -> {
                    val domainResult = authUseCases.signInWithGoogle(uiResult.idToken)

                    domainResult.onSuccess { isOnboardingCompleted ->
                        _uiState.update { it.copy(isLoading = false) }
                        _uiEffect.send(SignInEffect.NavigateToWelcome(isOnboardingCompleted))
                    }.onFailure { error ->
                        _uiState.update { it.copy(isLoading = false) }
                        _uiEffect.send(SignInEffect.ShowError(error.sanitizedMessage()))
                    }
                }
                is AuthUiResult.Cancelled -> {
                    _uiState.update { it.copy(isLoading = false) }
                }
                is AuthUiResult.Failure -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _uiEffect.send(SignInEffect.ShowError(uiResult.error.sanitizedMessage()))
                }
            }
        }
    }

    private fun signInWithEmail(email: String, password: String) {
        viewModelScope.launch {
            val result = authUseCases.signInWithEmailAndPassword(
                email = email,
                password = password
            )
            result.onSuccess { isOnboardingCompleted ->
                _uiState.update { it.copy(isLoading = false) }
                _uiEffect.send(SignInEffect.NavigateToWelcome(isOnboardingCompleted))
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false) }
                _uiEffect.send(SignInEffect.ShowError(error.sanitizedMessage()))
            }
        }
    }

    private fun SignInState.canSignIn(): Boolean {
        return email.isNotBlank() &&
            emailError == null &&
            password.isNotBlank() &&
            passwordError == null
    }
}
