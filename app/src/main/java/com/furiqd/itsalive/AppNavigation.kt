package com.furiqd.itsalive

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.furiqd.auth.presentation.navigation.authGraph
import com.furiqd.auth.presentation.navigation.AuthGraph
import com.furiqd.home.presentation.navigation.HomeGraph
import com.furiqd.home.presentation.navigation.homeGraph
import com.furiqd.pairing.presentation.navigation.PairingGraph
import com.furiqd.pairing.presentation.navigation.pairingGraph
import com.furiqd.personalisation.presentation.navigation.personalisationGraph
import com.furiqd.personalisation.presentation.navigation.PersonalisationGraph
import com.furiqd.shared.domain.model.UserSessionState

@Composable
fun AppNavigation(
    sessionState: UserSessionState,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val initialStartDestination = remember {
        when (sessionState) {
            UserSessionState.NeedsOnboarding -> PersonalisationGraph
            UserSessionState.FullyAuthorized -> HomeGraph
            else -> AuthGraph
        }
    }

    LaunchedEffect(sessionState) {
        when (sessionState) {
            UserSessionState.NotAuthorized -> {
                navController.navigate(AuthGraph) {
                    popUpTo(0) { inclusive = true }
                }
            }
            UserSessionState.NeedsOnboarding -> {
                navController.navigate(PersonalisationGraph) {
                    popUpTo(0) { inclusive = true }
                }
            }
            UserSessionState.FullyAuthorized -> {
                navController.navigate(HomeGraph) {
                    popUpTo(0) { inclusive = true }
                }
            }
            UserSessionState.Loading -> {}
        }
    }

    NavHost(
        navController = navController,
        startDestination = initialStartDestination,
        modifier = modifier
    ) {
        authGraph(
            navController = navController,
            onAuthSuccess = { isOnboardingCompleted ->
                val targetDestination = if (isOnboardingCompleted) {
                    HomeGraph
                } else {
                    PersonalisationGraph
                }

                navController.navigate(targetDestination) {
                    popUpTo(AuthGraph) { inclusive = true }
                }
            }
        )

        personalisationGraph(
            navController = navController,
            onPersonalisationComplete = {
                navController.navigate(HomeGraph) {
                    popUpTo(PersonalisationGraph) { inclusive = true }
                }
            }
        )

        homeGraph(
            navController = navController,
            onNavigateToAuth = {
                navController.navigate(AuthGraph) {
                    popUpTo(0) { inclusive = true }
                }
            },
            onNavigateToPairing = { navController.navigate(PairingGraph) }
        )

        pairingGraph(
            navController = navController,
            onNavigateToMain = { navController.navigate(HomeGraph) {
                popUpTo(PairingGraph) { inclusive = true }
            }}
        )
    }
}