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
    return "Authentication failed"
}