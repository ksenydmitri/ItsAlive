package com.furiqd.auth.presentation.screens.signup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.furiqd.auth.R
import com.furiqd.shared.ui.elements.BackButton
import com.furiqd.shared.ui.elements.ErrorSnackbar
import com.furiqd.auth.presentation.elements.LoginWithGoogleButton
import com.furiqd.auth.presentation.helper.AuthWithGoogleHelper
import com.furiqd.auth.presentation.helper.sanitizedMessage
import com.furiqd.auth.presentation.helper.asString
import kotlinx.coroutines.launch

@Composable
fun SignUpScreen(
    state: SignUpState,
    onEvent: (SignUpEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        BackButton(
            onClick = { onEvent(SignUpEvent.BackClicked) },
            modifier = Modifier)

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = stringResource(id = R.string.auth_signup_title),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        LoginWithGoogleButton(onClick = { onEvent(SignUpEvent.GoogleClicked) },
            modifier = Modifier)

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = stringResource(id = R.string.auth_or_email),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = state.email,
            onValueChange = { onEvent(SignUpEvent.EmailChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(id = R.string.auth_placeholder_email)) },
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )

        if (state.emailError != null) {
            Text(
                text = state.emailError.asString(),
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = state.password,
            onValueChange = { onEvent(SignUpEvent.PasswordChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(id = R.string.auth_placeholder_password)) },
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )

        if (state.passwordError != null) {
            Text(
                text = state.passwordError.asString(),
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.auth_privacy_text),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                fontSize = 14.sp
            )
            Checkbox(
                checked = state.isPrivacyChecked,
                onCheckedChange = { onEvent(SignUpEvent.PrivacyChecked(it)) },            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { onEvent(SignUpEvent.SignUpClicked) },
            enabled = state.isSignUpEnabled && !state.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
            } else {
                Text(text = stringResource(id = R.string.auth_btn_get_started), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.auth_start_already_have_account),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp
            )
            TextButton(
                onClick = {
                    onEvent(SignUpEvent.GoToSignInClicked)
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.auth_start_btn_login),
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun SignUpRoute(
    onNavigateToWelcome: (Boolean) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToSignIn: () -> Unit,
    authHelper: AuthWithGoogleHelper = remember { AuthWithGoogleHelper() }
) {
    val viewModel: SignUpViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is SignUpEffect.NavigateToWelcome -> onNavigateToWelcome(effect.isOnboardingCompleted)
                SignUpEffect.NavigateBack -> onNavigateBack()
                SignUpEffect.NavigateToSignIn -> onNavigateToSignIn()
                is SignUpEffect.ShowError -> {
                    snackbarHostState.showSnackbar(
                        message = effect.message,
                        withDismissAction = true
                    )
                }
                SignUpEffect.LaunchGoogleAuth -> {
                    scope.launch {
                        when (val result = authHelper.launchAuthUi(context)) {
                            is AuthWithGoogleHelper.AuthUiResult.GoogleSuccess -> {
                                viewModel.onGoogleTokenReceived(result.idToken)
                            }
                            is AuthWithGoogleHelper.AuthUiResult.Failure -> {
                                viewModel.onGoogleAuthFailed(
                                    result.error.sanitizedMessage()
                                )
                            }
                            AuthWithGoogleHelper.AuthUiResult.Cancelled -> {
                                viewModel.onGoogleAuthCancelled()
                            }
                        }
                    }
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        SignUpScreen(
            state = state,
            onEvent = viewModel::handleEvent,
            modifier = Modifier.fillMaxSize()
        )
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(16.dp)
        ) { data ->
            ErrorSnackbar(snackbarData = data)
        }
    }
}