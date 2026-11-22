package com.furiqd.auth.presentation.screens.signup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.furiqd.auth.R
import com.furiqd.auth.presentation.elements.AuthPrimaryButton
import com.furiqd.auth.presentation.elements.AuthTextField
import com.furiqd.auth.presentation.elements.PasswordStrengthMeter
import com.furiqd.auth.presentation.helper.asString

@Composable
fun SignUpForm(
    state: SignUpState,
    onEvent: (SignUpEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    androidx.compose.foundation.layout.Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AuthTextField(
            value = state.email,
            onValueChange = { onEvent(SignUpEvent.EmailChanged(it)) },
            label = stringResource(R.string.auth_placeholder_email),
            leadingIcon = Icons.Default.Email,
            keyboardType = KeyboardType.Email,
            errorText = state.emailError?.asString(),
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth()
        )
        AuthTextField(
            value = state.password,
            onValueChange = { onEvent(SignUpEvent.PasswordChanged(it)) },
            label = stringResource(R.string.auth_placeholder_password),
            leadingIcon = Icons.Default.Lock,
            isPassword = true,
            errorText = state.passwordError?.asString(),
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth()
        )
        PasswordStrengthMeter(strength = state.passwordStrength)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.auth_privacy_consent),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            Checkbox(
                checked = state.isPrivacyChecked,
                onCheckedChange = { onEvent(SignUpEvent.PrivacyChecked(it)) },
                enabled = !state.isLoading
            )
        }
        AuthPrimaryButton(
            text = stringResource(R.string.auth_signup_submit),
            onClick = { onEvent(SignUpEvent.SignUpClicked) },
            enabled = state.isSignUpEnabled,
            isLoading = state.isLoading
        )
    }
}
