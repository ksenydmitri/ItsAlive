package com.furiqd.auth.presentation.screens.signin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.furiqd.auth.R
import com.furiqd.auth.presentation.elements.AuthPrimaryButton
import com.furiqd.auth.presentation.elements.AuthTextField
import com.furiqd.auth.presentation.helper.asString

@Composable
fun SignInForm(
    state: SignInState,
    onEvent: (SignInEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AuthTextField(
            value = state.email,
            onValueChange = { onEvent(SignInEvent.EmailChanged(it)) },
            label = stringResource(R.string.auth_placeholder_email),
            leadingIcon = Icons.Default.Email,
            keyboardType = KeyboardType.Email,
            errorText = state.emailError?.asString(),
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth()
        )
        AuthTextField(
            value = state.password,
            onValueChange = { onEvent(SignInEvent.PasswordChanged(it)) },
            label = stringResource(R.string.auth_placeholder_password),
            leadingIcon = Icons.Default.Lock,
            isPassword = true,
            errorText = state.passwordError?.asString(),
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth()
        )
        TextButton(
            onClick = { onEvent(SignInEvent.ForgotPasswordClicked) },
            enabled = !state.isLoading,
            modifier = Modifier.align(androidx.compose.ui.Alignment.End)
        ) {
            Text(
                text = stringResource(R.string.auth_forgot_password),
                color = MaterialTheme.colorScheme.secondary
            )
        }
        AuthPrimaryButton(
            text = stringResource(R.string.auth_signin_submit),
            onClick = { onEvent(SignInEvent.SignInClicked) },
            enabled = state.isSignInEnabled,
            isLoading = state.isLoading
        )
    }
}
