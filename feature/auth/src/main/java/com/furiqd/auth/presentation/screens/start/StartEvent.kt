package com.furiqd.auth.presentation.screens.start

sealed interface StartEvent {
    object SignUpClicked : StartEvent
    object SignInClicked : StartEvent
    object GoogleClicked : StartEvent
}