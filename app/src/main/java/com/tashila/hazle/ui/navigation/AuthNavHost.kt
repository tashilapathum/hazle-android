package com.tashila.hazle.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.tashila.hazle.features.auth.AuthViewModel
import com.tashila.hazle.ui.navigation.AppDestinations.HOME_ROUTE
import com.tashila.hazle.ui.navigation.AppDestinations.LOGIN_ROUTE
import com.tashila.hazle.ui.navigation.AppDestinations.SIGNUP_ROUTE
import com.tashila.hazle.ui.navigation.AppDestinations.VERIFY_ROUTE
import com.tashila.hazle.ui.screens.EmailVerificationScreen
import com.tashila.hazle.ui.screens.LoginScreen
import com.tashila.hazle.ui.screens.SignupScreen

@Composable
fun AuthNavHost(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    onAuthFinished: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = SIGNUP_ROUTE,
        enterTransition = {
            scaleIn(initialScale = 0.8f, animationSpec = tween()
            ) + fadeIn(animationSpec = tween())
        },
        exitTransition = {
            scaleOut(targetScale = 1.1f, animationSpec = tween()
            ) + fadeOut(animationSpec = tween())
        },
        popEnterTransition = {
            scaleIn(initialScale = 1.1f, animationSpec = tween()
            ) + fadeIn(animationSpec = tween())
        },
        popExitTransition = {
            scaleOut(targetScale = 0.8f, animationSpec = tween()
            ) + fadeOut(animationSpec = tween())
        }
    ) {
        composable(LOGIN_ROUTE) {
            LoginScreen(
                email = authViewModel.email.collectAsState().value,
                onEmailChange = authViewModel::onEmailChanged,
                password = authViewModel.password.collectAsState().value,
                onPasswordChange = authViewModel::onPasswordChanged,
                onLoginClick = authViewModel::onLoginClick,
                errorMessage = authViewModel.errorMessage.collectAsState().value,
                isLoading = authViewModel.isLoading.collectAsState().value,
                onSignupClick = {
                    navController.navigate(SIGNUP_ROUTE) {
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = true
                        }
                    }
                    authViewModel.clearError()
                },
            )
        }
        composable(SIGNUP_ROUTE) {
            SignupScreen(
                email = authViewModel.email.collectAsState().value,
                onEmailChange = authViewModel::onEmailChanged,
                password = authViewModel.password.collectAsState().value,
                onPasswordChange = authViewModel::onPasswordChanged,
                confirmPassword = authViewModel.confirmPassword.collectAsState().value,
                onConfirmPasswordChange = authViewModel::onConfirmPasswordChanged,
                onSignupClick = authViewModel::onSignupClick,
                errorMessage = authViewModel.errorMessage.collectAsState().value,
                isLoading = authViewModel.isLoading.collectAsState().value,
                onLoginClick = {
                    navController.navigate(LOGIN_ROUTE) {
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = true
                        }
                    }
                    authViewModel.clearError()
                },
            )
        }
        composable(VERIFY_ROUTE) {
            EmailVerificationScreen(
                email = authViewModel.email.value,
                timerValue = authViewModel.resendTimer,
                onResendClick = {
                    authViewModel.resendEmail(authViewModel.email.value)
                    authViewModel.startTimer()
                },
                onBackToLogin = {
                    navController.navigate(LOGIN_ROUTE) {
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = true
                        }
                    }
                }
            )
        }
        composable(HOME_ROUTE) {
            onAuthFinished.invoke()
        }
    }
}