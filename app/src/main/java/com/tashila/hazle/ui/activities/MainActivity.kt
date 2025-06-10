package com.tashila.hazle.ui.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.tashila.hazle.features.auth.AuthRepository
import com.tashila.hazle.ui.components.PermissionRequester
import com.tashila.hazle.ui.screens.ChatScreen
import com.tashila.hazle.ui.theme.HazleTheme
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HazleTheme {
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

                ChatScreen(koinViewModel())
                RequestNotificationPermission()
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