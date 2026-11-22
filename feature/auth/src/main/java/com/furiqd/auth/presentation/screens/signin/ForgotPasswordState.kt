package com.furiqd.auth.presentation.screens.signin

import com.furiqd.auth.domain.model.ValidationError

data class ForgotPasswordState(
    val email: String = "",
    val emailError: ValidationError.Email? = null,
    val isLoading: Boolean = false,
    val isEmailSent: Boolean = false,
    val cooldownRemainingSeconds: Int = 0,
    val isSubmitEnabled: Boolean = false,
)
