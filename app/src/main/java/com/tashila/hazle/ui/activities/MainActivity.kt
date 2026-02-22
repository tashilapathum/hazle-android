package com.tashila.hazle.ui.activities

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.os.LocaleListCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavHostController
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.DialogNavigator
import com.tashila.hazle.R
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

class MainActivity : AppCompatActivity() {
    private var passedThreadId by mutableLongStateOf(-1L)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            HazleTheme {
                val authRepository: AuthRepository = koinInject()
                val settingsRepository: SettingsRepository = koinInject()
                val chatViewModel: ChatViewModel = koinViewModel()

                val context = LocalContext.current
                val navController = rememberSavableNavController()
                val coroutineScope = rememberCoroutineScope()
                var askNotificationPermission by rememberSaveable { mutableStateOf(false) }
                var initialNavigationDone by rememberSaveable { mutableStateOf(false) }

                // Set language
                LaunchedEffect(Unit) {
                    setAppLocale(settingsRepository.getLanguage())
                }

                // Determine initial navigation based on onboarding and authentication state
                if (!initialNavigationDone) {
                    LaunchedEffect(Unit) {
                        val isOnboarded = settingsRepository.isOnboarded()
                        val isAuthenticated = authRepository.isAuthenticated()

                        if (!isOnboarded) {
                            // User is not onboarded, show onboarding
                            navController.navigate(AppDestinations.ONBOARDING_ROUTE) {
                                popUpTo(navController.graph.startDestinationId) {
                                    inclusive = true // Clear back stack up to start destination
                                }
                            }
                        } else if (!isAuthenticated) {
                            // User is onboarded but not logged in, show login
                            showLogin(this@MainActivity)
                        } else {
                            // User is onboarded and logged in, navigate to threads and ask for permission
                            navController.navigate(AppDestinations.THREADS_ROUTE) {
                                popUpTo(navController.graph.startDestinationId) {
                                    inclusive = true
                                }
                            }
                            askNotificationPermission = true
                        }
                        initialNavigationDone = true
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
            }
        }
    }

    companion object {
        const val TAG = "MainActivity"
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

fun setAppLocale(localeTag: String) {
    val localeList = LocaleListCompat.forLanguageTags(localeTag)
    AppCompatDelegate.setApplicationLocales(localeList)
}

private fun showLogin(context: Context) {
    val intent = Intent(context, LoginActivity::class.java)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    context.startActivity(intent)
    (context as? Activity)?.finish()
}

@Composable
fun RequestNotificationPermission() {
    val context = LocalContext.current
    var showPermissionRationale by rememberSaveable { mutableStateOf(false) }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        PermissionRequester(
            permission = Manifest.permission.POST_NOTIFICATIONS,
            onPermissionGranted = { showPermissionRationale = false },
            onPermissionDenied = { showPermissionRationale = true },
            onPermissionPermanentlyDenied = {
                showPermissionRationale = false
                showNotificationSettings(context)
            }
        )
    }

    if (showPermissionRationale) {
        NotificationPermissionRationaleDialog(
            onDismiss = { showPermissionRationale = false },
            onGrant = {
                showPermissionRationale = false
                showNotificationSettings(context)
            }
        )
    }
}

@Composable
fun NotificationPermissionRationaleDialog(
    onDismiss: () -> Unit,
    onGrant: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.notification_permission_title)) },
        text = { Text(stringResource(id = R.string.notification_permission_text)) },
        confirmButton = {
            Button(onClick = onGrant) {
                Text(stringResource(id = R.string.grant_permission))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.no_thanks))
            }
        },
    )
}

private fun showNotificationSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
    }
    context.startActivity(intent)
}
