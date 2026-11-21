package com.furiqd.auth.presentation.screens.start

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.furiqd.auth.domain.usecase.AuthUseCases
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
internal class StartViewModel @Inject constructor(
    private val authUseCases: AuthUseCases
) : ViewModel() {

    private val _uiEffect = Channel<StartEffect>()
    val uiEffect = _uiEffect.receiveAsFlow()
    private val _uiState = MutableStateFlow(StartState())
    val uiState = _uiState.asStateFlow()

    fun handleEvent(event: StartEvent) {
        when (event) {
            StartEvent.SignUpClicked -> {
                _uiEffect.trySend(StartEffect.NavigateToSignUp)
            }
            StartEvent.SignInClicked -> {
                _uiEffect.trySend(StartEffect.NavigateToSignIn)
            }
            StartEvent.GoogleClicked -> {
                if (!_uiState.value.isLoading) {
                    _uiState.update { it.copy(isLoading = true) }
                    _uiEffect.trySend(StartEffect.LaunchGoogleAuth)
                }
            }
        }
    }

    fun onGoogleTokenReceived(idToken: String) {
        viewModelScope.launch {
            authUseCases.signInWithGoogle(idToken)
                .onSuccess { completed ->
                    _uiState.update { it.copy(isLoading = false) }
                    _uiEffect.send(StartEffect.NavigateToWelcome(completed))
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false) }
                    _uiEffect.send(StartEffect.ShowError(error.sanitizedMessage()))
                }
        }
    }

    fun onGoogleAuthFailed(errorMessage: String) {
        _uiState.update { it.copy(isLoading = false) }
        _uiEffect.trySend(StartEffect.ShowError(errorMessage))
    }

    fun onGoogleAuthCancelled() {
        _uiState.update { it.copy(isLoading = false) }
    }
}