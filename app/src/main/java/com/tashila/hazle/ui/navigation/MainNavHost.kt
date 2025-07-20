package com.tashila.hazle.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
        startDestination = AppDestinations.THREADS_ROUTE,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = spring()
            ) + fadeIn(animationSpec = spring())
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = spring()
            ) + fadeOut(animationSpec = spring())
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = spring()
            ) + fadeIn(animationSpec = spring())
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = spring()
            ) + fadeOut(animationSpec = spring())
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