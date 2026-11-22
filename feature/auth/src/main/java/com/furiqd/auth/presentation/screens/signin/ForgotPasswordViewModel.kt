package com.furiqd.auth.presentation.screens.signin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.furiqd.auth.domain.usecase.AuthUseCases
import com.furiqd.auth.presentation.helper.InputValidator
import com.furiqd.auth.presentation.helper.sanitizedMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class ForgotPasswordViewModel @Inject constructor(
    private val authUseCases: AuthUseCases
) : ViewModel() {
    private val _uiState = MutableStateFlow(ForgotPasswordState())
    val uiState = _uiState.asStateFlow()

    private val _uiEffect = Channel<ForgotPasswordEffect>()
    val uiEffect = _uiEffect.receiveAsFlow()
    private var cooldownJob: Job? = null

    fun handleEvent(event: ForgotPasswordEvent) {
        when (event) {
            is ForgotPasswordEvent.EmailChanged -> {
                _uiState.update { state ->
                    val updated = state.copy(
                        email = event.email,
                        emailError = InputValidator.validateEmail(event.email)
                    )
                    updated.copy(isSubmitEnabled = updated.canSubmit())
                }
            }
            ForgotPasswordEvent.SendResetClicked -> sendResetEmail()
            ForgotPasswordEvent.DismissDialog -> {
                cooldownJob?.cancel()
                viewModelScope.launch { _uiEffect.send(ForgotPasswordEffect.DismissDialog) }
            }
        }
    }

    private fun sendResetEmail() {
        val state = _uiState.value
        if (!state.canSubmit()) return

        _uiState.update { it.copy(isLoading = true, isSubmitEnabled = false) }
        viewModelScope.launch {
            authUseCases.sendPasswordResetEmail(state.email)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isEmailSent = true,
                            cooldownRemainingSeconds = COOLDOWN_SECONDS,
                            isSubmitEnabled = false
                        )
                    }
                    startCooldown()
                }
                .onFailure { error ->
                    _uiState.update { state ->
                        val updated = state.copy(isLoading = false)
                        updated.copy(isSubmitEnabled = updated.canSubmit())
                    }
                    _uiEffect.send(ForgotPasswordEffect.ShowError(error.sanitizedMessage()))
                }
        }
    }

    private fun startCooldown() {
        cooldownJob?.cancel()
        cooldownJob = viewModelScope.launch {
            while (_uiState.value.cooldownRemainingSeconds > 0) {
                delay(1_000)
                _uiState.update { state ->
                    val remaining = (state.cooldownRemainingSeconds - 1).coerceAtLeast(0)
                    val updated = state.copy(cooldownRemainingSeconds = remaining)
                    updated.copy(isSubmitEnabled = updated.canSubmit())
                }
            }
        }
    }

    private fun ForgotPasswordState.canSubmit(): Boolean =
        emailError == null &&
            email.isNotBlank() &&
            !isLoading &&
            cooldownRemainingSeconds == 0

    private companion object {
        const val COOLDOWN_SECONDS = 60
    }
}
