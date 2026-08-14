package com.silentwitness.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.silentwitness.presentation.calculator.CalculatorScreen
import com.silentwitness.presentation.onboarding.OnboardingScreen
import com.silentwitness.presentation.securelayout.SecureLayout
import com.silentwitness.presentation.session.SessionState
import com.silentwitness.presentation.session.SessionViewModel

@Composable
fun AppNavHost(sessionViewModel: SessionViewModel) {
    val navController = rememberNavController()
    val sessionState by sessionViewModel.state.collectAsState()

    NavHost(
        navController = navController,
        startDestination = when (sessionState) {
            SessionState.Onboarding -> OnboardingRoute
            else -> CalculatorRoute
        }
    ) {
        // Onboarding – outside of graphs (no PIN yet)
        composable<OnboardingRoute> {
            OnboardingScreen(
                onComplete = { pin ->
                    sessionViewModel.completeOnboarding(pin)
                    navController.navigate(CalculatorRoute) {
                        popUpTo(OnboardingRoute) { inclusive = true }
                    }
                }
            )
        }

        // DisguiseGraph – Calculator only
        composable<CalculatorRoute> {
            CalculatorScreen(
                sessionViewModel = sessionViewModel,
                onUnlock = {
                    navController.navigate(SecureGraph) {
                        popUpTo(CalculatorRoute) { inclusive = false }
                    }
                }
            )
        }

        // SecureGraph – hosts SecureLayout which contains the nested secure NavHost
        composable<SecureGraph> {
            SecureLayout(
                outerNavController = navController,
                sessionViewModel = sessionViewModel,
                onLock = {
                    sessionViewModel.lock()
                    navController.popBackStack(CalculatorRoute, inclusive = false)
                }
            )
        }
    }
}
