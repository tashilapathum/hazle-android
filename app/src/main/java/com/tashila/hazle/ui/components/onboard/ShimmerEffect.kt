package com.tashila.hazle.ui.components.onboard

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun Modifier.shimmerEffect(): Modifier = composed {
    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.09f),
        Color.LightGray.copy(alpha = 0.03f),
        Color.LightGray.copy(alpha = 0.09f),
    )

    val transition = rememberInfiniteTransition(label = "ShimmerAnimation")
    val translateAnimation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f, // Adjust for desired shimmer movement distance
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2000, // Adjust for shimmer speed
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ShimmerTranslate"
    )

    background(
        brush = Brush.linearGradient(
            colors = shimmerColors,
            start = Offset(x = translateAnimation - 1000f, y = 0f), // Adjust start offset
            end = Offset(x = translateAnimation, y = 1000f) // Adjust end offset
        )
    )
}