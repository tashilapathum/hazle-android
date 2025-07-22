package com.tashila.hazle.ui.components

import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/**
 * Composable to handle requesting any single Android runtime permission.
 *
 * @param permission The Android Manifest permission string to request (e.g., Manifest.permission.CAMERA, Manifest.permission.POST_NOTIFICATIONS).
 * @param onPermissionGranted Callback invoked when the permission is granted.
 * @param onPermissionDenied Callback invoked when the permission is denied (user clicked "Deny").
 * @param onPermissionPermanentlyDenied Callback invoked when the permission is permanently denied
 * (user checked "Don't ask again" or denied multiple times).
 */
@Composable
fun PermissionRequester(
    permission: String,
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit,
    onPermissionPermanentlyDenied: () -> Unit
) {
    val context = LocalContext.current

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted: Boolean ->
            if (isGranted) {
                onPermissionGranted()
            } else {
                // Permission was denied. Now, check if it was a permanent denial.
                val showRationale = shouldShowRequestPermissionRationale(context, permission)
                if (!showRationale) {
                    onPermissionPermanentlyDenied()
                } else {
                    // User denied, but didn't check "Don't ask again". Can ask again.
                    onPermissionDenied()
                }
            }
        }
    )

    LaunchedEffect(permission) {
        when {
            // 1. Check if the permission is already granted
            ContextCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_GRANTED -> {
                onPermissionGranted() // Permission is already granted
            }
            // 2. Check if we should show a rationale (user previously denied but didn't permanently deny)
            shouldShowRequestPermissionRationale(context, permission) -> {
                onPermissionDenied()
            }
            // 3. Otherwise, request the permission directly
            else -> {
                requestPermissionLauncher.launch(permission)
            }
        }
    }
}

/**
 * Helper function to determine if the system recommends showing a permission rationale.
 * This function requires an Activity context.
 *
 * @param context The Android Context, which should be an Activity context for this check.
 * @param permission The permission string to check.
 * @return True if a rationale should be shown, false otherwise.
 */
private fun shouldShowRequestPermissionRationale(context: Context, permission: String): Boolean {
    // Attempt to cast the context to an Activity. This is safe if the Composable is hosted by an Activity.
    val activity = context as? android.app.Activity
    return activity?.let {
        androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale(it, permission)
    } == true // Return false if context is not an Activity or if method is not available
}