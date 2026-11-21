package com.furiqd.auth.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.furiqd.auth.presentation.screens.signin.SignInRoute
import com.furiqd.auth.presentation.screens.signin.ForgotPasswordRoute
import com.furiqd.auth.presentation.screens.signup.SignUpRoute
import com.furiqd.auth.presentation.screens.start.StartRoute

fun NavGraphBuilder.authGraph(navController: NavController,
                              onAuthSuccess: (Boolean) -> Unit) {
    navigation<AuthGraph>(startDestination = StartDestination) {

        composable<StartDestination> {
            StartRoute(
                onNavigateToSignUp = { navController.navigate(SignUpDestination) },
                onNavigateToLogin = { navController.navigate(SignInDestination) },
                onNavigateToWelcome = onAuthSuccess
            )
        }

        composable<SignUpDestination> {
            SignUpRoute(
                onNavigateToWelcome = { isOnboardingCompleted -> 
                    onAuthSuccess(isOnboardingCompleted)
                },
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSignIn = { navController.navigate(SignInDestination) }
            )
        }

        composable<SignInDestination> {
            SignInRoute(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSignUp = { navController.navigate(SignUpDestination) },
                onNavigateToForgotPassword = { navController.navigate(ForgotPasswordDestination) },
                onNavigateToWelcome = { isOnboardingCompleted ->
                    onAuthSuccess(isOnboardingCompleted)
                }
            )
        }

        composable<ForgotPasswordDestination> {
            ForgotPasswordRoute(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
