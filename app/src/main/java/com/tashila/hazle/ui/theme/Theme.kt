package com.tashila.hazle.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Hazel_Dark_Primary,
    onPrimary = Hazel_Dark_OnPrimary,
    primaryContainer = Hazel_Dark_PrimaryContainer,
    onPrimaryContainer = Hazel_Dark_OnPrimaryContainer,
    secondary = Hazel_Dark_Secondary,
    onSecondary = Hazel_Dark_OnSecondary,
    secondaryContainer = Hazel_Dark_SecondaryContainer,
    onSecondaryContainer = Hazel_Dark_OnSecondaryContainer,
    tertiary = Hazel_Dark_Tertiary,
    onTertiary = Hazel_Dark_OnTertiary,
    tertiaryContainer = Hazel_Dark_TertiaryContainer,
    onTertiaryContainer = Hazel_Dark_OnTertiaryContainer,
    error = Hazel_Dark_Error,
    onError = Hazel_Dark_OnError,
    errorContainer = Hazel_Dark_ErrorContainer,
    onErrorContainer = Hazel_Dark_OnErrorContainer,

    background = Hazel_Dark_Background, // Opaque dark background
    onBackground = Hazel_Dark_OnBackground, // Light text on dark background
    onSurface = Hazel_Dark_OnBackground, // Text on transparent surface should contrast with main background

    surfaceVariant = Hazel_Dark_SurfaceVariant, // Opaque surface variant
    onSurfaceVariant = Hazel_Dark_OnSurfaceVariant, // Contrasting text for surface variant
    outline = Hazel_Dark_Outline
)

private val LightColorScheme = lightColorScheme(
    primary = Hazel_Light_Primary,
    onPrimary = Hazel_Light_OnPrimary,
    primaryContainer = Hazel_Light_PrimaryContainer,
    onPrimaryContainer = Hazel_Light_OnPrimaryContainer,
    secondary = Hazel_Light_Secondary,
    onSecondary = Hazel_Light_OnSecondary,
    secondaryContainer = Hazel_Light_SecondaryContainer,
    onSecondaryContainer = Hazel_Light_OnSecondaryContainer,
    tertiary = Hazel_Light_Tertiary,
    onTertiary = Hazel_Light_OnTertiary,
    tertiaryContainer = Hazel_Light_TertiaryContainer,
    onTertiaryContainer = Hazel_Light_OnTertiaryContainer,
    error = Hazel_Light_Error,
    onError = Hazel_Light_OnError,
    errorContainer = Hazel_Light_ErrorContainer,
    onErrorContainer = Hazel_Light_OnErrorContainer,

    background = Hazel_Light_Background, // Opaque light background
    onBackground = Hazel_Light_OnBackground, // Dark text on light background
    onSurface = Hazel_Light_OnBackground, // Text on transparent surface should contrast with main background

    surfaceVariant = Hazel_Light_SurfaceVariant, // Opaque surface variant
    onSurfaceVariant = Hazel_Light_OnSurfaceVariant, // Contrasting text for surface variant
    outline = Hazel_Light_Outline
)

@Composable
fun HazleTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}