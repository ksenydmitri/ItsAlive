package com.furiqd.auth.presentation.screens.signin

sealed interface ForgotPasswordEvent {
    data class EmailChanged(val email: String) : ForgotPasswordEvent
    data object SendResetClicked : ForgotPasswordEvent
    data object DismissDialog : ForgotPasswordEvent
}
