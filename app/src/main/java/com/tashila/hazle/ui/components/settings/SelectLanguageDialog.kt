package com.tashila.hazle.ui.components.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.tashila.hazle.R


@Composable
fun SelectLanguageDialog(
    onDismiss: () -> Unit,
    onLanguageSelected: (String) -> Unit
) {
    val currentLocale = LocalConfiguration.current.locales[0].language

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.select_language_dialog_title)) },
        text = {
            Column {
                LanguageOption(
                    languageName = stringResource(id = R.string.language_english),
                    localeTag = "en",
                    currentLocale = currentLocale,
                    onSelect = onLanguageSelected
                )
                LanguageOption(
                    languageName = stringResource(id = R.string.language_german),
                    localeTag = "de",
                    currentLocale = currentLocale,
                    onSelect = onLanguageSelected
                )
                LanguageOption(
                    languageName = stringResource(id = R.string.language_sinhala),
                    localeTag = "si",
                    currentLocale = currentLocale,
                    onSelect = onLanguageSelected
                )
            }
        },
        confirmButton = {}
    )
}

@Composable
fun LanguageOption(
    languageName: String,
    localeTag: String,
    currentLocale: String,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = currentLocale == localeTag,
                onClick = { onSelect(localeTag) },
                role = Role.RadioButton
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = currentLocale == localeTag,
            onClick = null
        )
        Text(text = languageName, modifier = Modifier.padding(start = 8.dp))
    }
}