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
        SignUpHeader(onBack = { onEvent(SignUpEvent.BackClicked) })
        Spacer(modifier = Modifier.height(24.dp))
        LoginWithGoogleButton(
            onClick = { onEvent(SignUpEvent.GoogleClicked) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(id = R.string.auth_or_email),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        SignUpForm(state = state, onEvent = onEvent)
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