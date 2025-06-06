package com.tashila.hazle.ui.activities

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import com.tashila.hazle.ui.components.PermissionRequester
import com.tashila.hazle.ui.screens.ChatScreen
import com.tashila.hazle.ui.theme.HazleTheme
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HazleTheme {
                ChatScreen(koinViewModel())
                RequestNotificationPermission()
            }
        }
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