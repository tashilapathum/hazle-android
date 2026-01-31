package com.tashila.hazle.ui.components.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun ErrorDialog(errorMessage: String?, onDismiss: () -> Unit) {
    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = onDismiss) { Text("OK") }
            },
            title = { Text("Error") },
            text = { Text(errorMessage) }
        )
    }
}