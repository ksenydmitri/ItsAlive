package com.furiqd.itsalive

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.furiqd.auth.presentation.navigation.AuthGraph
import com.furiqd.shared.ui.theme.ItsAliveTheme
import com.furiqd.shared.domain.model.UserSessionState
import com.furiqd.home.presentation.navigation.HomeGraph
import com.furiqd.personalisation.presentation.navigation.PersonalisationGraph
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.syncFcmToken()
        enableEdgeToEdge()
        setContent {
            val sessionState by viewModel.sessionState.collectAsState()

            ItsAliveTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    when (sessionState) {
                        UserSessionState.Loading -> {
                            LoadingScreen(modifier = Modifier.padding(innerPadding))
                        }
                        UserSessionState.NotAuthorized -> {
                            AppNavigation(
                                startGraph = AuthGraph,
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        UserSessionState.NeedsOnboarding -> {
                            AppNavigation(
                                startGraph = PersonalisationGraph,
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        UserSessionState.FullyAuthorized -> {
                            AppNavigation(
                                startGraph = HomeGraph,
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LoadingScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}