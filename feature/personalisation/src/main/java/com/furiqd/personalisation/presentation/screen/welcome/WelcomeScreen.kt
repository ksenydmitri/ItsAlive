package com.furiqd.personalisation.presentation.screen.welcome

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.furiqd.personalisation.R

@Composable
fun WelcomeRoute(
    onNavigateToOnboarding: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WelcomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                WelcomeEffect.NavigateToNext -> onNavigateToOnboarding()
                is WelcomeEffect.ShowError -> {}
            }
        }
    }

    WelcomeScreen(
        uiState = state,
        onEvent = viewModel::handleEvent,
        modifier = modifier
    )
}

@Composable
fun WelcomeScreen(
    uiState: WelcomeUiState,
    onEvent: (WelcomeEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 24.dp)
        ) {
            // Mascot Egg Scene
            Box(
                modifier = Modifier.size(220.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(190.dp)
                        .background(Color(0xFFE7F9F7), CircleShape)
                )
                Image(
                    painter = painterResource(com.furiqd.shared.R.drawable.green_egg),
                    contentDescription = null,
                    modifier = Modifier.size(160.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .background(Color(0xFFE7F9F7), RoundedCornerShape(99.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "TOGETHER, EVERY DAY",
                    color = Color(0xFF258A83),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Small moments.\nMore us. 🐾",
                color = Color(0xFF222026),
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                lineHeight = 34.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "A shared world for you and your favorite person.",
                color = Color(0xFF76727D),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }

        Button(
            onClick = { onEvent(WelcomeEvent.onGetStartedClicked) },
            enabled = !uiState.isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF6B6B),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(bottom = 16.dp)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.5.dp
                )
            } else {
                Text(
                    text = "Get Started →",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}


@Composable
@Preview(name = "With Authorized Name", showBackground = true)
fun PreviewWelcomeScreenWithName() {
    WelcomeScreen(
        uiState = WelcomeUiState(userDisplayName = "Alex", isLoading = false),
        onEvent = {}
    )
}

@Composable
@Preview(name = "Default Anonymous/Email User", showBackground = true)
fun PreviewWelcomeScreenDefault() {
    WelcomeScreen(
        uiState = WelcomeUiState(userDisplayName = null, isLoading = false),
        onEvent = {}
    )
}