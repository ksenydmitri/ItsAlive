package com.furiqd.auth.presentation.screens.start

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.furiqd.auth.R
import com.furiqd.auth.presentation.helper.AuthWithGoogleHelper
import com.furiqd.auth.presentation.helper.sanitizedMessage
import com.furiqd.shared.ui.theme.ItsAliveTheme
import kotlinx.coroutines.launch

@Composable
fun StartScreen(
    state: StartState,
    onEvent: (StartEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.auth_start_title),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            textAlign = TextAlign.Center
        )
        StartHeader(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 24.dp)
        )
        StartActions(
            onGoogleClick = { onEvent(StartEvent.GoogleClicked) },
            onEmailSignInClick = { onEvent(StartEvent.SignInClicked) },
            onSignUpClick = { onEvent(StartEvent.SignUpClicked) },
            isLoading = state.isLoading,
            modifier = Modifier.padding(bottom = 24.dp)
        )
    }
}

@Composable
fun StartRoute(
    onNavigateToSignUp: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToWelcome: (Boolean) -> Unit,
){
    val viewModel: StartViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val authHelper = remember { AuthWithGoogleHelper() }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                StartEffect.NavigateToSignUp -> onNavigateToSignUp()
                StartEffect.NavigateToSignIn -> onNavigateToLogin()
                is StartEffect.NavigateToWelcome -> onNavigateToWelcome(effect.isOnboardingCompleted)
                StartEffect.LaunchGoogleAuth -> scope.launch {
                    when (val result = authHelper.launchAuthUi(context)) {
                        is AuthWithGoogleHelper.AuthUiResult.GoogleSuccess ->
                            viewModel.onGoogleTokenReceived(result.idToken)
                        is AuthWithGoogleHelper.AuthUiResult.Failure ->
                            viewModel.onGoogleAuthFailed(result.error.sanitizedMessage())
                        AuthWithGoogleHelper.AuthUiResult.Cancelled ->
                            viewModel.onGoogleAuthCancelled()
                    }
                }
                is StartEffect.ShowError ->
                    snackbarHostState.showSnackbar(effect.message, withDismissAction = true)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        StartScreen(
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
        )
    }
}

@Preview(name = "Dark Theme", showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun PreviewStartScreenDark() {
    ItsAliveTheme(darkTheme = true) {
        StartScreen(
            state = StartState(),
            onEvent = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview(name = "Light Theme", showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun PreviewStartScreenLight() {
    ItsAliveTheme(darkTheme = false) {
        StartScreen(
            state = StartState(),
            onEvent = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}