package com.tashila.hazle.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

// Helper data class for each aurora "blob"
private data class AuroraBlob(
    val center: Offset,
    val radius: Float,
    val baseColor: Color
)

@OptIn(ExperimentalTime::class)
@Composable
fun AuroraBackground(modifier: Modifier = Modifier) {
    val isDarkTheme = isSystemInDarkTheme()
    val density = LocalDensity.current

    val blobs = remember {
        val random = Random(Clock.System.now().toEpochMilliseconds())

        // Define a palette of highlight colors that would still be visible when transparent
        val colors = listOf(
            Color(0xFF00FFFF), // Cyan
            Color(0xFF00FF00), // Lime Green
            Color(0xFFFFFF00), // Yellow
            Color(0xFFFF00FF), // Magenta
            Color(0xFF0000FF), // Blue
            Color(0xFFFF8C00), // Dark Orange
            Color(0xFF9400D3)  // Dark Violet
        )

        // Generate 3 blobs
        List(3) {
            val baseColor = colors[random.nextInt(colors.size)]
            // Random radius between 200dp and 600dp (converted to pixels in draw scope)
            val radius = with(density) { random.nextFloat() * 400.dp.toPx() + 200.dp.toPx() }
            // Random center position, allowing blobs to start slightly off-screen for a natural look
            val centerX = random.nextFloat() * 1.5f - 0.25f // Ranges from -0.25 to 1.25 relative to screen width
            val centerY = random.nextFloat() * 1.5f - 0.25f // Ranges from -0.25 to 1.25 relative to screen height

            AuroraBlob(
                center = Offset(centerX, centerY),
                radius = radius,
                baseColor = baseColor
            )
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        blobs.forEach { blob ->
            // Convert relative center (0-1.5) to actual pixel coordinates on the canvas
            val actualCenterX = blob.center.x * size.width
            val actualCenterY = blob.center.y * size.height

            // Create a radial gradient for a soft, glowing effect
            val brush = Brush.radialGradient(
                colors = if (isDarkTheme) {
                    listOf(
                        blob.baseColor.copy(alpha = 0.08f), // More opaque at the center
                        blob.baseColor.copy(alpha = 0.00f)  // Fully transparent at the edge
                    )
                } else { // more opaque for light mode
                    listOf(
                        blob.baseColor.copy(alpha = 0.18f), // More opaque at the center
                        blob.baseColor.copy(alpha = 0.00f)  // Fully transparent at the edge
                    )
                },
                center = Offset(actualCenterX, actualCenterY),
                radius = blob.radius
            )

            // Draw the circle with the gradient and Screen blend mode
            // BlendMode.Screen brightens colors and makes them appear transparent,
            // allowing the background behind the Canvas to show through without
            // creating weird dark areas in light mode.
            drawCircle(
                brush = brush,
                center = Offset(actualCenterX, actualCenterY),
                radius = blob.radius,
            )
        }
    }
}