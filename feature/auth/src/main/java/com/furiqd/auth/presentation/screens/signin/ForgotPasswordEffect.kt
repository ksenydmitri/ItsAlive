package com.furiqd.auth.presentation.screens.signin

sealed interface ForgotPasswordEffect {
    data object DismissDialog : ForgotPasswordEffect
    data class ShowError(val message: String) : ForgotPasswordEffect
}
