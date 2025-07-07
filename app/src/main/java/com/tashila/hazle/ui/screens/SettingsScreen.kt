package com.tashila.hazle.ui.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.tashila.hazle.features.settings.SettingsViewModel
import com.tashila.hazle.ui.activities.LoginActivity
import com.tashila.hazle.ui.components.ConfirmationDialog
import com.tashila.hazle.ui.components.settings.AccountInfoSection
import com.tashila.hazle.ui.components.settings.ApiUrlSettingDialog
import com.tashila.hazle.ui.components.settings.SettingsItem
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = koinViewModel(),
    onBackClicked: () -> Unit = {}
) {
    val context = LocalContext.current

    // Collect states from the ViewModel
    val apiUrlInput by viewModel.apiUrlInput.collectAsState()
    val savedApiUrl by viewModel.savedApiUrl.collectAsState()
    val userInfo by viewModel.userInfo.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val message by viewModel.message.collectAsState()

    // State for managing the visibility of dialogs
    var showApiUrlDialog by remember { mutableStateOf(false) }
    var showConfirmLogoutDialog by remember { mutableStateOf(false) }

    // Scroll behavior for the collapsible toolbar
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    // Snackbar host state for showing messages
    val snackbarHostState = remember { SnackbarHostState() }


    // Effect to show Snackbar messages
    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage() // Clear message after showing
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection), // Apply nested scroll
        topBar = {
            LargeTopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                AccountInfoSection(
                    userInfo = userInfo,
                    onLogoutClicked = { showConfirmLogoutDialog = true },
                    isLoggingOut = isLoading
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Outlined.Link,
                    title = "API Endpoint",
                    description = "Set the API Endpoint if you're self-hosting. Default is https://api.hazle.tashila.me/",
                    onClick = { showApiUrlDialog = true }
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Outlined.Notifications,
                    title = "Notification Settings",
                    description = "Manage app notifications",
                    onClick = { openAppNotificationSettings(context) }
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Outlined.Info,
                    title = "About",
                    description = "Version ${getAppVersion()}",
                    onClick = { /* TODO: Show app version, credits, etc. */ }
                )
            }
        }
    }

    if (showApiUrlDialog) {
        ApiUrlSettingDialog(
            currentApiUrl = apiUrlInput,
            onApiUrlChanged = viewModel::onApiUrlInputChanged,
            onSaveClicked = {
                viewModel.onSaveApiUrlClicked()
                showApiUrlDialog = false
            },
            onDismiss = { showApiUrlDialog = false },
            isSaving = isLoading
        )
    }

    if (showConfirmLogoutDialog) {
        ConfirmationDialog(
            onConfirm = {
                viewModel.onLogoutClicked()
                redirectToLogin(context)
            },
            onDismiss = { showConfirmLogoutDialog = false }
        )
    }
}

fun openAppNotificationSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}

fun redirectToLogin(context: Context) {
    val intent = Intent(context, LoginActivity::class.java)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    context.startActivity(intent)
    (context as Activity).finish()
}

@Composable
fun getAppVersion(): String {
    val context = LocalContext.current
    return remember(context) {
        try {
            val packageManager = context.packageManager
            val packageName = context.packageName
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            packageInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            "1.0.0" // Fallback in case of error
        } as String
    }
}