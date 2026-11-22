package com.furiqd.auth.presentation.screens.signin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.furiqd.auth.R
import com.furiqd.auth.presentation.elements.AuthPrimaryButton
import com.furiqd.auth.presentation.elements.AuthTextField

@Composable
fun ForgotPasswordDialog(
    state: ForgotPasswordState,
    onEmailChanged: (String) -> Unit,
    onSubmitClicked: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Key Icon Box
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .rotate(-4f)
                        .background(
                            color = Color(0xFFFFF0EF),
                            shape = RoundedCornerShape(20.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🔑", fontSize = 28.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.auth_forgot_password_title),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.03).sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.auth_forgot_password_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (state.isEmailSent) {
                    // Success Confirmation State Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = Color(0xFFE7F9F7),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "✉️", fontSize = 24.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Email sent! Check your inbox.",
                                color = Color(0xFF258A83),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (state.cooldownRemainingSeconds > 0) {
                                Text(
                                    text = "Resend available in ${state.cooldownRemainingSeconds}s",
                                    color = Color(0xFF76727D),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                } else {
                    AuthTextField(
                        value = state.email,
                        onValueChange = onEmailChanged,
                        label = stringResource(R.string.auth_placeholder_email),
                        leadingIcon = Icons.Default.Email,
                        errorText = state.emailError?.let { stringResource(R.string.error_email_invalid) },
                        enabled = !state.isLoading
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    AuthPrimaryButton(
                        text = "Send Reset Link →",
                        onClick = onSubmitClicked,
                        enabled = state.isSubmitEnabled,
                        isLoading = state.isLoading
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = onDismiss) {
                    Text(
                        text = "Back to Sign In",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun ForgotPasswordRoute(
    onNavigateBack: () -> Unit
) {
    val viewModel: ForgotPasswordViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    ForgotPasswordDialog(
        state = state,
        onEmailChanged = { viewModel.handleEvent(ForgotPasswordEvent.EmailChanged(it)) },
        onSubmitClicked = { viewModel.handleEvent(ForgotPasswordEvent.SendResetClicked) },
        onDismiss = onNavigateBack
    )
}
