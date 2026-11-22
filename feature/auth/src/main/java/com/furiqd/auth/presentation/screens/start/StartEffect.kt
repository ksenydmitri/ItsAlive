package com.furiqd.auth.presentation.screens.start

sealed interface StartEffect {
    object NavigateToSignUp : StartEffect
    object NavigateToSignIn : StartEffect
    object LaunchGoogleAuth : StartEffect
    data class NavigateToWelcome(val isOnboardingCompleted: Boolean) : StartEffect
    data class ShowError(val message: String) : StartEffect
}