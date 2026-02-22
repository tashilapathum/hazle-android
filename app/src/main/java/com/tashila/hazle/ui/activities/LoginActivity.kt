package com.tashila.hazle.ui.activities

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.DialogNavigator
import com.tashila.hazle.features.auth.AuthViewModel
import com.tashila.hazle.ui.activities.MainActivity.Companion.TAG
import com.tashila.hazle.ui.components.AuroraBackground
import com.tashila.hazle.ui.components.dialogs.ErrorDialog
import com.tashila.hazle.ui.navigation.AppDestinations.HOME_ROUTE
import com.tashila.hazle.ui.navigation.AppDestinations.LOGIN_ROUTE
import com.tashila.hazle.ui.navigation.AppDestinations.SIGNUP_ROUTE
import com.tashila.hazle.ui.navigation.AppDestinations.VERIFY_ROUTE
import com.tashila.hazle.ui.navigation.AuthNavHost
import com.tashila.hazle.ui.theme.HazleTheme
import org.koin.androidx.compose.koinViewModel

class LoginActivity : ComponentActivity() {
    private var uiErrorMessage by mutableStateOf<String?>(null)
    private lateinit var authViewModel: AuthViewModel

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
                        val navController = rememberSavableNavController()
                        authViewModel = koinViewModel()

                        // Collect one-time auth events for navigation or toasts
                        LaunchedEffect(Unit) {
                            authViewModel.authEvent.collect { event ->
                                when (event) {
                                    AuthViewModel.AuthEvent.LoginSuccess -> {
                                        navController.navigate(HOME_ROUTE) {
                                            popUpTo(LOGIN_ROUTE) { inclusive = true }
                                        }
                                    }

                                    AuthViewModel.AuthEvent.SignupSuccess -> {
                                        navController.navigate(VERIFY_ROUTE) {
                                            popUpTo(SIGNUP_ROUTE) { inclusive = true }
                                        }
                                        authViewModel.startTimer()
                                    }

                                    AuthViewModel.AuthEvent.VerifySuccess -> {
                                        navController.navigate(LOGIN_ROUTE)
                                    }
                                }
                            }
                        }

                        AuthNavHost(
                            navController = navController,
                            authViewModel = authViewModel,
                            onAuthFinished = {
                                startActivity(
                                    Intent(
                                        this@LoginActivity,
                                        MainActivity::class.java
                                    )
                                )
                                finish()
                            }
                        )

                        if (uiErrorMessage != null)
                            ErrorDialog(
                                errorMessage = uiErrorMessage,
                                onDismiss = { uiErrorMessage = null }
                            )

                        intent?.let { // when the app wasn't opened from the background to verify
                            handleEmailConfirmIntent(it)
                        }
                    }
                }
            }
        }
    }

    private fun handleEmailConfirmIntent(intent: Intent?) {
        val uri: Uri = intent?.data ?: return
        if (intent.action == Intent.ACTION_VIEW && uri.host == "api.hazle.tashila.me") {
            val urlFragment = uri.fragment
            if (!urlFragment.isNullOrEmpty()) {
                val params = urlFragment.split(Regex("&|&amp;")).associate {
                    val pair = it.split("=")
                    if (pair.size == 2) pair[0] to pair[1] else pair[0] to ""
                }

                // 1. Check for Errors first
                val error = params["error"]
                if (error != null) {
                    val errorCode = params["error_code"]
                    val errorDescription = params["error_description"]?.replace("+", " ")
                    Log.e(TAG, "Auth Error: $error ($errorCode) - $errorDescription")

                    uiErrorMessage = errorDescription?.replace("+", " ") ?: "Link invalid"
                    return
                }

                // 2. Otherwise, look for Success Tokens
                val accessToken = params["access_token"]
                val refreshToken = params["refresh_token"]

                authViewModel.verifyEmail(accessToken, refreshToken)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent) // Required to update the intent returned by getIntent()
        handleEmailConfirmIntent(intent)
    }

}

@Composable
private fun rememberSavableNavController(): NavHostController {
    val context = LocalContext.current
    return rememberSaveable(saver = navControllerSaver(context)) {
        NavHostController(context).apply {
            navigatorProvider.addNavigator(ComposeNavigator())
            navigatorProvider.addNavigator(DialogNavigator())
        }
    }
}

private fun navControllerSaver(context: Context): Saver<NavHostController, Bundle> = Saver(
    save = { it.saveState() },
    restore = { savedState ->
        NavHostController(context).apply {
            navigatorProvider.addNavigator(ComposeNavigator())
            navigatorProvider.addNavigator(DialogNavigator())
            restoreState(savedState)
        }
    }
)
