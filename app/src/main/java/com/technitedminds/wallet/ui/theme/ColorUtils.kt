package com.technitedminds.wallet.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/**
 * Returns [Color.White] for dark surfaces, [Color.Black] for light surfaces.
 * Uses WCAG relative luminance (perceived brightness) for the decision.
 *
 * The threshold of 0.4 gives slightly earlier switch to white text,
 * which is more readable on mid-tone gradients.
 */
fun Color.contrastText(threshold: Float = 0.4f): Color {
    return if (luminance() > threshold) Color.Black else Color.White
}

/**
 * Composable-friendly variant that remembers the result.
 */
@Composable
fun rememberContrastText(color: Color, threshold: Float = 0.4f): Color {
    return remember(color, threshold) { color.contrastText(threshold) }
}

/**
 * Picks the contrast text color for a gradient by averaging start + end colors.
 */
fun gradientContrastText(
    startColorHex: String,
    endColorHex: String,
    threshold: Float = 0.4f,
): Color {
    return try {
        val start = Color(android.graphics.Color.parseColor(startColorHex))
        val end = Color(android.graphics.Color.parseColor(endColorHex))
        val avg = Color(
            red = (start.red + end.red) / 2f,
            green = (start.green + end.green) / 2f,
            blue = (start.blue + end.blue) / 2f,
        )
        avg.contrastText(threshold)
    } catch (_: Exception) {
        Color.White
    }
}

/**
 * Checks whether a hex color string represents a light color.
 */
fun isLightColor(hexColor: String): Boolean {
    return try {
        val color = Color(android.graphics.Color.parseColor(hexColor))
        color.luminance() > 0.4f
    } catch (_: Exception) {
        false
    }
}
