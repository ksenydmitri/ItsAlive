package com.furiqd.auth.presentation.elements

import com.furiqd.auth.presentation.screens.signin.SignInState

internal fun SignInState.canSubmit(): Boolean =
    email.isNotBlank() &&
        emailError == null &&
        password.isNotBlank() &&
        passwordError == null
