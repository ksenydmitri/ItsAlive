package com.furiqd.home.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.furiqd.home.presentation.screen.main.MainRoute
import com.furiqd.home.presentation.screen.petdashboard.PetDashboardRoute
import com.furiqd.home.presentation.screen.profile.ProfileRoute

fun NavGraphBuilder.homeGraph(navController: NavController,
                              onNavigateToAuth: () -> Unit,
                              onNavigateToPairing: () -> Unit) {
    navigation<HomeGraph>(startDestination = MainDestination) {
        composable<MainDestination> {
            MainRoute(
                onNavigateToProfile = { navController.navigate(ProfileDestination) },
                onNavigateToPairing = { onNavigateToPairing() },
                onNavigateToPetDashboard = { navController.navigate(PetDashboardDestination) }
            )
        }

        composable<ProfileDestination> {
            ProfileRoute(
                onNavigateToMain = { navController.navigate(MainDestination) },
                onNavigateToAuth = { onNavigateToAuth() },
                onNavigateToEditProfile = {}
            )
        }

        composable<PetDashboardDestination> {
            PetDashboardRoute(
                onNavigateBack = { navController.navigateUp() }
            )
        }
    }
}