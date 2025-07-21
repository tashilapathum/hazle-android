package com.tashila.hazle.ui.activities

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.tashila.hazle.features.auth.AuthRepository
import com.tashila.hazle.features.chat.ChatViewModel
import com.tashila.hazle.features.notifications.NotificationService.Companion.EXTRA_MESSAGE_THREAD_ID
import com.tashila.hazle.features.settings.SettingsRepository
import com.tashila.hazle.ui.components.AuroraBackground
import com.tashila.hazle.ui.components.PermissionRequester
import com.tashila.hazle.ui.navigation.AppDestinations
import com.tashila.hazle.ui.navigation.MainNavHost
import com.tashila.hazle.ui.theme.HazleTheme
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

class MainActivity : ComponentActivity() {
    private var passedThreadId by mutableLongStateOf(-1L)

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            HazleTheme {
                val authRepository: AuthRepository = koinInject()
                val settingsRepository: SettingsRepository = koinInject()
                val chatViewModel: ChatViewModel = koinViewModel()

                val context = LocalContext.current
                val navController = rememberNavController()
                val coroutineScope = rememberCoroutineScope()
                var showSessionTimeOut by remember { mutableStateOf(false) }
                var askNotificationPermission by remember { mutableStateOf(false) }

                // Determine initial navigation based on onboarding and authentication state
                LaunchedEffect(Unit) {
                    val isOnboarded = settingsRepository.isOnboarded()
                    val isAuthenticated = authRepository.isAuthenticated()

                    if (!isOnboarded) {
                        // User is not onboarded, show onboarding
                        // MainNavHost will handle showing the onboarding route
                    } else if (!isAuthenticated) {
                        // User is onboarded but not logged in, prompt login
                        showSessionTimeOut = true
                    } else {
                        // User is onboarded and logged in, navigate to threads and ask for permission
                        navController.navigate(AppDestinations.THREADS_ROUTE) {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true // Clear back stack up to start destination
                            }
                        }
                        askNotificationPermission = true
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        AuroraBackground(modifier = Modifier.fillMaxSize())
                    }
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = Color.Transparent
                    ) {
                        MainNavHost(
                            navController = navController,
                            chatViewModel = chatViewModel,
                            onboardingFinished = {
                                coroutineScope.launch {
                                    settingsRepository.saveOnboardState(isDone = true)
                                    // After onboarding, if authenticated, navigate to threads and ask for permission
                                    if (authRepository.isAuthenticated()) {
                                        navController.navigate(AppDestinations.THREADS_ROUTE) {
                                            popUpTo(navController.graph.startDestinationId) {
                                                inclusive = true
                                            }
                                        }
                                        askNotificationPermission = true
                                    } else {
                                        showLogin(context)
                                    }
                                }
                            }
                        )

                        // When coming from notification
                        passedThreadId = intent.getLongExtra(EXTRA_MESSAGE_THREAD_ID, -1L)
                        LaunchedEffect(key1 = passedThreadId) {
                            if (passedThreadId != -1L) {
                                val targetRoute = AppDestinations.chatDetailRoute(passedThreadId)
                                if (navController.currentDestination?.route != targetRoute) {
                                    chatViewModel.setActiveThread(passedThreadId)
                                    navController.navigate(targetRoute) {
                                        popUpTo(navController.graph.startDestinationId) {
                                            inclusive = false
                                        }
                                        launchSingleTop = true
                                    }
                                }
                            }
                        }

                        if (askNotificationPermission) RequestNotificationPermission()
                    }
                }
                if (showSessionTimeOut) PromptLogin(context)
            }
        }
    }

    companion object {
        const val TAG = "MainActivity"
    }
}

@Composable
private fun PromptLogin(context: Context) {
    AlertDialog(
        onDismissRequest = {},
        title = {
            Text(text = "Session Timed Out")
        },
        text = {
            Text(text = "Your session has expired. Please log in again.")
        },
        confirmButton = {
            TextButton (
                onClick = { showLogin(context) }
            ) {
                Text("OK")
            }
        }
    )
}

private fun showLogin(context: Context) {
    val intent = Intent(context, LoginActivity::class.java)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    context.startActivity(intent)
    (context as? Activity)?.finish()
}

@Composable
fun RequestNotificationPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        PermissionRequester(
            permission = Manifest.permission.POST_NOTIFICATIONS,
            onPermissionGranted = {
                println("Notification permission granted!")
            },
            onPermissionDenied = {
                println("Notification permission denied.")
            },
            onPermissionPermanentlyDenied = {
                println("Notification permission permanently denied. Direct user to settings.")
            }
        )
    }
}