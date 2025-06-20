package com.tashila.hazle.ui.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.tashila.hazle.features.auth.AuthRepository
import com.tashila.hazle.features.chat.ChatViewModel
import com.tashila.hazle.features.notifications.NotificationService.Companion.EXTRA_MESSAGE_THREAD_ID
import com.tashila.hazle.ui.components.AuroraBackground
import com.tashila.hazle.ui.components.PermissionRequester
import com.tashila.hazle.ui.navigation.AppDestinations
import com.tashila.hazle.ui.navigation.ChatNavHost
import com.tashila.hazle.ui.theme.HazleTheme
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

class MainActivity : ComponentActivity() {
    private var passedThreadId by mutableLongStateOf(-1L)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HazleTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        AuroraBackground(modifier = Modifier.fillMaxSize())
                    }
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = Color.Transparent
                    ) {
                        val context = LocalContext.current
                        val authRepository: AuthRepository = koinInject()

                        LaunchedEffect(Unit) {
                            if (authRepository.isAuthenticated().not()) {
                                val intent = Intent(context, LoginActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                context.startActivity(intent)
                                (context as? Activity)?.finish()
                                return@LaunchedEffect
                            }
                        }

                        val navController = rememberNavController() // Create NavController here
                        val chatViewModel: ChatViewModel = koinViewModel() // Get shared ChatViewModel

                        ChatNavHost(
                            navController = navController,
                            chatViewModel = chatViewModel
                        )

                        // When coming from notification
                        passedThreadId = intent.getLongExtra(EXTRA_MESSAGE_THREAD_ID, -1L)
                        LaunchedEffect(key1 = passedThreadId) {
                            if (passedThreadId != -1L) {
                                val targetRoute = AppDestinations.chatDetailRoute(passedThreadId)
                                if (navController.currentDestination?.route != targetRoute) {
                                    chatViewModel.setActiveThread(passedThreadId)
                                    navController.navigate(targetRoute) {
                                        popUpTo(navController.graph.startDestinationId) { inclusive = false }
                                        launchSingleTop = true
                                    }
                                }
                            }
                        }
                        RequestNotificationPermission()
                    }
                }
            }
        }
    }
    companion object {
        const val TAG = "MainActivity"
    }
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