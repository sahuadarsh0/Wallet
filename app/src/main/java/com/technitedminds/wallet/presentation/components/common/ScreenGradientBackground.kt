package com.technitedminds.wallet.presentation.components.common

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Draws the theme background colour, then overlays a vertical+radial
 * accent-tinted gradient at the top of the screen — matching the header
 * style used in dialogs throughout the app.
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
    accentColor: Color = MaterialTheme.colorScheme.primary,
    gradientHeight: Dp = 220.dp,
    content: @Composable BoxScope.() -> Unit,
) {
    val surfaceColor = MaterialTheme.colorScheme.background
    val onBackground = MaterialTheme.colorScheme.onBackground

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(surfaceColor),
    ) {
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
        CompositionLocalProvider(LocalContentColor provides onBackground) {
            content()
        }
    }
}
