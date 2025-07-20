package com.tashila.hazle.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.tashila.hazle.ui.components.thread.getIconsList
import kotlin.random.Random

// Generates consistent colors and icons based on an ID
class ColorGenerator(private val id: Long = Random.nextLong()) {
    private val baseIcons: List<ImageVector> by lazy { getIconsList() }
    private val randomGenerator: Random by lazy { Random(id) }

    // Generates a base random color based on the ID
    private val generatedBaseColor: Color by lazy {
        Color(randomGenerator.nextFloat(), randomGenerator.nextFloat(), randomGenerator.nextFloat())
    }

    // Public properties for the colors and icon
    val circleColor: Color by lazy { generatedBaseColor.copy(alpha = 0.1f) }
    val iconTint: Color by lazy { generatedBaseColor.copy(alpha = 0.7f) }
    val icon: ImageVector by lazy { baseIcons[randomGenerator.nextInt(baseIcons.size)] }
}