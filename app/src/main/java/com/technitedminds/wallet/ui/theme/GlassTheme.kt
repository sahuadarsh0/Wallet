package com.technitedminds.wallet.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Glass morphism design tokens that adapt to light/dark theme.
 */
@Immutable
data class GlassColors(
    /** Semi-transparent surface fill */
    val surface: Color,
    /** Slightly more opaque surface for emphasis */
    val surfaceElevated: Color,
    /** Iridescent gradient border brush */
    val border: Brush,
    /** Subtle single-color border for less emphasis */
    val borderSubtle: Color,
)

/** Glass tokens for light mode */
val LightGlassColors = GlassColors(
    surface = Color.White.copy(alpha = 0.55f),
    surfaceElevated = Color.White.copy(alpha = 0.75f),
    border = Brush.linearGradient(
        colors = listOf(
            Color(0xFF818CF8).copy(alpha = 0.35f),
            Color(0xFFC084FC).copy(alpha = 0.20f),
            Color(0xFFFFFFFF).copy(alpha = 0.10f),
        ),
    ),
    borderSubtle = Color.Black.copy(alpha = 0.08f),
)

/** Glass tokens for dark mode */
val DarkGlassColors = GlassColors(
    surface = Color.White.copy(alpha = 0.08f),
    surfaceElevated = Color.White.copy(alpha = 0.12f),
    border = Brush.linearGradient(
        colors = listOf(
            Color(0xFF818CF8).copy(alpha = 0.50f),
            Color(0xFFC084FC).copy(alpha = 0.30f),
            Color(0xFFFFFFFF).copy(alpha = 0.10f),
        ),
    ),
    borderSubtle = Color.White.copy(alpha = 0.10f),
)

val LocalGlassColors = staticCompositionLocalOf { LightGlassColors }

/**
 * Convenience accessor for glass colors within a composable scope.
 */
object Glass {
    val colors: GlassColors
        @Composable get() = LocalGlassColors.current

    val CornerRadius = 16.dp
    val BorderWidth = 1.dp
    val PillCornerRadius = 28.dp
}

/**
 * A composable Box with glass morphism styling — semi-transparent background
 * with iridescent border. Adapts to light/dark theme automatically.
 */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(Glass.CornerRadius),
    borderWidth: Dp = Glass.BorderWidth,
    useElevated: Boolean = false,
    content: @Composable BoxScope.() -> Unit,
) {
    val glass = Glass.colors
    val bg = if (useElevated) glass.surfaceElevated else glass.surface

    Box(
        modifier = modifier
            .clip(shape)
            .background(bg, shape)
            .border(borderWidth, glass.border, shape),
        content = content,
    )
}
