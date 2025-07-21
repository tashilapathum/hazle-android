package com.tashila.hazle.ui.components.onboard

import android.graphics.DashPathEffect
import android.graphics.Rect
import android.graphics.Typeface
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@Composable
fun AnimatedWhisperText(
    text: String = "Hazle",
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 48.sp,
    color: Color = Color.Black,
    animationDuration: Int = 2000,
    customTypeface: Typeface? = Typeface.DEFAULT
) {
    var animationProgress by remember { mutableFloatStateOf(0f) }

    // Animation that goes from 0 to 1
    LaunchedEffect(Unit) {
        animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = animationDuration,
                easing = FastOutSlowInEasing
            )
        ) { value, _ ->
            animationProgress = value
        }
    }

    Canvas(modifier = modifier) {
        val textPaint = Paint().asFrameworkPaint().apply {
            isAntiAlias = true
            textSize = fontSize.toPx()
            typeface = customTypeface
            this.color = color.toArgb()
        }

        // Calculate text bounds
        val textBounds = Rect()
        textPaint.getTextBounds(text, 0, text.length, textBounds)

        val textWidth = textBounds.width()
        val textHeight = textBounds.height()

        // Center the text
        val x = (size.width - textWidth) / 2
        val y = (size.height + textHeight) / 2

        // Create a path effect that reveals the text progressively
        val pathLength = textWidth * 2 // Approximate path length
        val visibleLength = pathLength * animationProgress

        // Create the stroke effect
        val pathEffect = DashPathEffect(
            floatArrayOf(visibleLength, pathLength - visibleLength),
            0f
        )

        textPaint.pathEffect = pathEffect
        textPaint.style = android.graphics.Paint.Style.STROKE
        textPaint.strokeWidth = 3f

        // Draw the text
        drawContext.canvas.nativeCanvas.drawText(
            text, x, y, textPaint
        )

        // Add a filled version that appears after stroke animation
        if (animationProgress > 0.8f) {
            val fillPaint = Paint().asFrameworkPaint().apply {
                isAntiAlias = true
                textSize = fontSize.toPx()
                typeface = customTypeface
                this.color = color.copy(alpha = (animationProgress - 0.8f) * 5f).toArgb()
                style = android.graphics.Paint.Style.FILL
            }

            drawContext.canvas.nativeCanvas.drawText(
                text, x, y, fillPaint
            )
        }
    }
}