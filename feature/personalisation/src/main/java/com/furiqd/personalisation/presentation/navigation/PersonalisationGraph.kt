package com.furiqd.personalisation.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.furiqd.personalisation.presentation.screen.intro.OnboardingIntroScreen
import com.furiqd.personalisation.presentation.screen.onboarding.OnboardingRoute
import com.furiqd.personalisation.presentation.screen.welcome.WelcomeRoute


fun NavGraphBuilder.personalisationGraph(navController: NavController,
                                         onPersonalisationComplete: () -> Unit) {
    navigation<PersonalisationGraph>(startDestination = WelcomeDestination) {

        composable<WelcomeDestination> {
            WelcomeRoute(
                onNavigateToOnboarding = { navController.navigate(OnboardingIntroDestination) }
            )
        }

        composable<OnboardingIntroDestination> {
            OnboardingIntroScreen(
                onSkip = { navController.navigate(OnboardingDestination) { popUpTo<OnboardingIntroDestination> { inclusive = true } } },
                onGetStarted = { navController.navigate(OnboardingDestination) { popUpTo<OnboardingIntroDestination> { inclusive = true } } }
            )
        }

        composable<OnboardingDestination> {
            OnboardingRoute(
                onNavigateToNext = onPersonalisationComplete
            )
        }
    }
}