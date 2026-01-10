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
import com.tashila.hazle.ui.screens.ChatScreen
import com.tashila.hazle.ui.screens.OnboardingScreen
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
        startDestination = AppDestinations.ONBOARDING_ROUTE,
        enterTransition = {
            scaleIn(
                initialScale = 0.8f,
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        exitTransition = {
            scaleOut(
                targetScale = 1.1f,
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        },
        popEnterTransition = {
            scaleIn(
                initialScale = 1.1f,
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        popExitTransition = {
            scaleOut(
                targetScale = 0.8f,
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        }
    ) {
        composable(AppDestinations.THREADS_ROUTE) {
            ThreadsScreen(
                onClickSettings = {
                    navController.navigate(AppDestinations.SETTINGS_ROUTE)
                },
                onCreateNewChat = {
                    // When creating a new chat, pass -1L to signify no existing thread ID
                    chatViewModel.startNewChat()
                    navController.navigate(AppDestinations.chatDetailRoute(-1L))
                },
                onThreadSelected = { localThreadId ->
                    // Set active thread in ViewModel and navigate with the specific ID
                    chatViewModel.setActiveThread(localThreadId)
                    navController.navigate(AppDestinations.chatDetailRoute(localThreadId))
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
                    navController.navigate(AppDestinations.THREADS_ROUTE) {
                        popUpTo(AppDestinations.THREADS_ROUTE) { inclusive = true }
                    }
                },
                viewModel = chatViewModel,
                initialThreadId = localThreadId
            )
        }
        composable(AppDestinations.SETTINGS_ROUTE) {
            SettingsScreen(
                onBackClicked = {
                    navController.popBackStack()
                }
            )
        }
        composable(AppDestinations.ONBOARDING_ROUTE) {
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