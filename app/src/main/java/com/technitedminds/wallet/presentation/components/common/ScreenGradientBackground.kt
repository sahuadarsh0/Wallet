package com.technitedminds.wallet.presentation.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
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
 */
@Composable
fun ScreenGradientBackground(
    modifier: Modifier = Modifier,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    gradientHeight: Dp = 220.dp,
    content: @Composable BoxScope.() -> Unit,
) {
    val surfaceColor = MaterialTheme.colorScheme.background

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
        content()
    }
}
