package com.tashila.hazle.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tashila.hazle.ui.theme.WhisperFontFamily

@Composable
fun LogoText() {
    Text(
        text = "Hazle",
        style = MaterialTheme.typography.displayMedium,
        fontFamily = WhisperFontFamily,
        modifier = Modifier.padding(bottom = 8.dp),
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}