package com.technitedminds.wallet.presentation.components.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.technitedminds.wallet.ui.theme.BackgroundPattern
import com.technitedminds.wallet.ui.theme.FolderTheme
import com.technitedminds.wallet.ui.theme.LocalBackgroundPattern
import com.technitedminds.wallet.ui.theme.LocalFolderTheme

/**
 * Draws the theme background colour, then overlays a vertical+radial
 * accent-tinted gradient at the top of the screen — matching the header
 * style used in dialogs throughout the app.
 *
 * The accent color defaults to the active [FolderTheme.accent] (resolved via
 * [LocalFolderTheme]) so changing the folder theme also gently re-tints the
 * top of every screen, keeping the surface cohesive. Callers can still pass an
 * explicit [accentColor] when they need a one-off tint.
 *
 * If the user has selected a non-[BackgroundPattern.NONE] pattern, a faint
 * decorative overlay is painted across the entire surface (dot grid, fine
 * pinstripe, or topographic contour rings). Patterns are deliberately
 * low-contrast so they read as paper texture rather than UI noise.
 *
 * Because most screens use a transparent [androidx.compose.material3.Scaffold]
 * on top of this background, we explicitly publish the theme-correct
 * [LocalContentColor] here so that bare icons (e.g. back buttons),
 * [androidx.compose.material3.IconButton]s and `Text`s inside the scaffold
 * body inherit `onBackground` instead of falling back to the default
 * black — which would be invisible in dark mode.
 */
@Composable
fun ScreenGradientBackground(
    modifier: Modifier = Modifier,
    accentColor: Color = LocalFolderTheme.current.accent,
    gradientHeight: Dp = 220.dp,
    content: @Composable BoxScope.() -> Unit,
) {
    val surfaceColor = MaterialTheme.colorScheme.background
    val onBackground = MaterialTheme.colorScheme.onBackground
    val pattern = LocalBackgroundPattern.current
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(surfaceColor),
    ) {
        // Accent gradient sash at the top of the screen.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(gradientHeight)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.28f),
                            accentColor.copy(alpha = 0.06f),
                            Color.Transparent,
                        ),
                    ),
                ),
        )

        // Optional decorative pattern overlay.
        if (pattern != BackgroundPattern.NONE) {
            BackgroundPatternOverlay(
                pattern = pattern,
                tint = accentColor,
                fallbackTint = onBackground,
                isDark = isDark,
                modifier = Modifier.fillMaxSize(),
            )
        }

        CompositionLocalProvider(LocalContentColor provides onBackground) {
            content()
        }
    }
}

/**
 * Decorative pattern painted across the full screen. Tinted with the active
 * theme accent so the texture feels like part of the same palette as the
 * folders, then mixed with [fallbackTint] (onBackground) to keep enough
 * contrast against both dark and light backgrounds. Opacity is bumped quite a
 * bit higher than the original implementation — the previous values were so
 * faint they were invisible on the actual device.
 */
@Composable
private fun BackgroundPatternOverlay(
    pattern: BackgroundPattern,
    tint: Color,
    fallbackTint: Color,
    isDark: Boolean,
    modifier: Modifier = Modifier,
) {
    // Dark backgrounds need a brighter tint; light backgrounds need a darker
    // one. We blend the theme accent with the readable foreground tint to
    // guarantee the pattern is always perceptible without overwhelming.
    val baseAlpha = if (isDark) 0.18f else 0.14f
    val accentMix = tint.copy(alpha = baseAlpha)
    val outline = fallbackTint.copy(alpha = if (isDark) 0.10f else 0.08f)

    Canvas(modifier = modifier) {
        when (pattern) {
            BackgroundPattern.NONE -> Unit
            BackgroundPattern.DOTS -> drawDotPattern(accentMix, outline)
            BackgroundPattern.GRID -> drawGridPattern(accentMix, outline)
            BackgroundPattern.TOPO -> drawTopoPattern(accentMix)
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDotPattern(
    accent: Color,
    outline: Color,
) {
    val spacing = 36f
    val radius = 2.2f
    var row = 0
    var y = spacing / 2f
    while (y < size.height) {
        // Stagger every other row by half a step for a more "design"-feeling
        // dot field rather than a strict square grid.
        val xOffset = if (row % 2 == 0) 0f else spacing / 2f
        var x = spacing / 2f + xOffset
        while (x < size.width) {
            drawCircle(color = accent, radius = radius, center = Offset(x, y))
            drawCircle(color = outline, radius = radius * 0.5f, center = Offset(x, y))
            x += spacing
        }
        y += spacing
        row += 1
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawGridPattern(
    accent: Color,
    outline: Color,
) {
    val spacing = 40f
    val thinStroke = 0.8f
    val majorStroke = 1.4f
    var i = 0
    var x = 0f
    while (x < size.width) {
        // Every 4th line is a slightly heavier accent line — gives the grid
        // a subtle blueprint cadence instead of looking like graph paper.
        val isMajor = i % 4 == 0
        drawLine(
            color = if (isMajor) accent else outline,
            start = Offset(x, 0f),
            end = Offset(x, size.height),
            strokeWidth = if (isMajor) majorStroke else thinStroke,
        )
        x += spacing
        i += 1
    }
    var j = 0
    var y = 0f
    while (y < size.height) {
        val isMajor = j % 4 == 0
        drawLine(
            color = if (isMajor) accent else outline,
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = if (isMajor) majorStroke else thinStroke,
        )
        y += spacing
        j += 1
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawTopoPattern(color: Color) {
    // Concentric "contour line" rings emanating from a few off-canvas anchors,
    // giving the surface a quiet topographic-map vibe without any single
    // ring dominating the frame.
    val anchors = listOf(
        Offset(size.width * 0.15f, size.height * 0.18f),
        Offset(size.width * 0.85f, size.height * 0.55f),
        Offset(size.width * 0.40f, size.height * 0.95f),
    )
    val ringSpacing = 44f
    val maxRadius = maxOf(size.width, size.height) * 1.1f
    val stroke = Stroke(width = 1.2f)

    anchors.forEach { center ->
        var radius = ringSpacing
        while (radius < maxRadius) {
            drawCircle(
                color = color,
                radius = radius,
                center = center,
                style = stroke,
            )
            radius += ringSpacing
        }
    }
}
