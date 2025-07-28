package com.tashila.hazle.ui.components.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tashila.hazle.R
import com.tashila.hazle.utils.SERVER_URL

/**
 * Composable for the API URL setting dialog.
 */
@Composable
fun ApiUrlSettingDialog(
    currentApiUrl: String,
    onApiUrlChanged: (String) -> Unit,
    onSaveClicked: () -> Unit,
    onDismiss: () -> Unit,
    isSaving: Boolean
) {
    var dialogApiUrlInput by remember { mutableStateOf(currentApiUrl) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.api_endpoint_title)) },
        text = {
            Column {
                OutlinedTextField(
                    value = dialogApiUrlInput,
                    onValueChange = { dialogApiUrlInput = it },
                    label = { Text("URL") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                if (isSaving) {
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth()) // Linear progress for dialog
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (dialogApiUrlInput.isBlank()) // revert to default
                        dialogApiUrlInput = SERVER_URL
                    onApiUrlChanged(dialogApiUrlInput)
                    onSaveClicked()
                },
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}