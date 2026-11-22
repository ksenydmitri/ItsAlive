package com.furiqd.auth.presentation.helper

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.furiqd.auth.R
import com.furiqd.auth.domain.model.ValidationError

@Composable
fun ValidationError.Email.asString(): String {
    return when (this) {
        ValidationError.Email.Empty -> stringResource(R.string.error_email_empty)
        ValidationError.Email.Invalid -> stringResource(R.string.error_email_invalid)
    }
}

@Composable
fun ValidationError.Password.asString(): String {
    return when (this) {
        ValidationError.Password.Empty -> stringResource(R.string.error_password_empty)
        ValidationError.Password.TooShort -> stringResource(R.string.error_password_too_short)
        ValidationError.Password.InvalidCharacters -> stringResource(R.string.error_password_invalid_characters)
    }
}

internal fun Throwable.sanitizedMessage(): String {
    val message = this.message ?: ""
    return when {
        message.contains("email-already-in-use", ignoreCase = true) ||
            message.contains("collision", ignoreCase = true) ||
            message.contains("already in use", ignoreCase = true) ->
            "This email is already registered. Please sign in instead."
        message.contains("invalid-credential", ignoreCase = true) ||
            message.contains("wrong-password", ignoreCase = true) ||
            message.contains("user-not-found", ignoreCase = true) ||
            message.contains("invalid-email", ignoreCase = true) ||
            message.contains("INVALID_LOGIN_CREDENTIALS", ignoreCase = true) ||
            message.contains("ERROR_WRONG_PASSWORD", ignoreCase = true) ||
            message.contains("ERROR_USER_NOT_FOUND", ignoreCase = true) ||
            message.contains("ERROR_INVALID_CREDENTIAL", ignoreCase = true) ->
            "Incorrect email or password. Please try again."
        else -> "Authentication failed"
    }
}