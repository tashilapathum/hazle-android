package com.tashila.hazle.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.tashila.hazle.features.chat.ChatViewModel
import com.tashila.hazle.ui.navigation.AppDestinations.ONBOARDING_ROUTE
import com.tashila.hazle.ui.navigation.AppDestinations.SETTINGS_ROUTE
import com.tashila.hazle.ui.navigation.AppDestinations.THREADS_ROUTE
import com.tashila.hazle.ui.navigation.AppDestinations.UPGRADE_ROUTE
import com.tashila.hazle.ui.navigation.AppDestinations.chatDetailRoute
import com.tashila.hazle.ui.screens.ChatScreen
import com.tashila.hazle.ui.screens.OnboardingScreen
import com.tashila.hazle.ui.screens.PaywallScreen
import com.tashila.hazle.ui.screens.SettingsScreen
import com.tashila.hazle.ui.screens.ThreadsScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun MainNavHost(
    navController: NavHostController,
    chatViewModel: ChatViewModel,
    onboardingFinished: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = THREADS_ROUTE,
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
        composable(THREADS_ROUTE) {
            ThreadsScreen(
                onClickSettings = {
                    navController.navigate(SETTINGS_ROUTE)
                },
                onCreateNewChat = {
                    // When creating a new chat, pass -1L to signify no existing thread ID
                    chatViewModel.startNewChat()
                    navController.navigate(chatDetailRoute(-1L))
                },
                onThreadSelected = { localThreadId ->
                    // Set active thread in ViewModel and navigate with the specific ID
                    chatViewModel.setActiveThread(localThreadId)
                    navController.navigate(chatDetailRoute(localThreadId))
                },
                threadsViewModel = koinViewModel()
            )
        }
        composable(
            route = AppDestinations.CHAT_VIEW_ROUTE_WITH_ARGS, // Use the route with args
            arguments = AppDestinations.CHAT_DETAIL_ARGS // Define the arguments
        ) { backStackEntry ->
            // Extract the localThreadId from the navigation arguments
            val localThreadId = backStackEntry.arguments?.getLong("localThreadId")

            // ChatScreen will now receive the localThreadId directly from navigation
            ChatScreen(
                onCloseChat = {
                    navController.navigate(THREADS_ROUTE) {
                        popUpTo(THREADS_ROUTE) { inclusive = true }
                    }
                },
                viewModel = chatViewModel,
                initialThreadId = localThreadId
            )
        }
        composable(SETTINGS_ROUTE) {
            SettingsScreen(
                onUpgradeClicked = {
                    navController.navigate(UPGRADE_ROUTE)
                },
                onBackClicked = {
                    navController.popBackStack()
                }
            )
        }
        composable(UPGRADE_ROUTE) {
            PaywallScreen(
                onBackClicked = {
                    navController.popBackStack()
                },
                onPurchaseCompleted = {

                },
                viewModel = koinViewModel()
            )
        }
        composable(ONBOARDING_ROUTE) {
            OnboardingScreen(
                pagerState = rememberPagerState(
                    0,
                    initialPageOffsetFraction = 0F,
                    pageCount = { 4 }
                ),
                onboardingFinished = { onboardingFinished.invoke() }
            )
        }
    }
}