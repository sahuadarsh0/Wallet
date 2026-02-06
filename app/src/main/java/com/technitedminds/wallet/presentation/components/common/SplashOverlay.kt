package com.technitedminds.wallet.presentation.components.common

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.technitedminds.wallet.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Full-screen splash overlay that displays the app logo with a premium
 * spring-bounce entrance, subtle breathing pulse, and zoom-through exit.
 *
 * The background is a vertical gradient mirroring the wallet icon's
 * pink → purple → blue palette at ~8% brightness.
 *
 * @param onSplashFinished Callback invoked when the splash animation completes.
 */
@Composable
fun SplashOverlay(
    onSplashFinished: () -> Unit,
) {
    // Animation state holders
    val iconScale = remember { Animatable(0.5f) }
    val iconAlpha = remember { Animatable(0f) }
    val overlayAlpha = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        // Phase 1: Entrance — spring bounce + fade in
        launch {
            iconScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = 0.55f,
                    stiffness = 200f,
                ),
            )
        }
        launch {
            iconAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 400,
                    easing = FastOutSlowInEasing,
                ),
            )
        }

        // Wait for entrance to settle
        delay(700)

        // Phase 2: Breathing pulse — subtle scale 1.0 → 1.03 → 1.0
        iconScale.animateTo(
            targetValue = 1.03f,
            animationSpec = tween(
                durationMillis = 300,
                easing = FastOutSlowInEasing,
            ),
        )
        iconScale.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 300,
                easing = FastOutSlowInEasing,
            ),
        )

        // Phase 3: Exit — zoom-through + fade out
        launch {
            iconScale.animateTo(
                targetValue = 1.4f,
                animationSpec = tween(
                    durationMillis = 500,
                    easing = FastOutSlowInEasing,
                ),
            )
        }
        launch {
            iconAlpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(
                    durationMillis = 500,
                    easing = FastOutSlowInEasing,
                ),
            )
        }
        launch {
            overlayAlpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(
                    durationMillis = 500,
                    easing = FastOutSlowInEasing,
                ),
            )
        }

        delay(500)
        onSplashFinished()
    }

    // Gradient echoing the wallet's pink → purple → blue at ~8% brightness
    val splashGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1A0A14), // very dark warm pink
            Color(0xFF12081E), // very dark purple
            Color(0xFF081018), // very dark teal-black
        ),
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { alpha = overlayAlpha.value }
            .background(splashGradient),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.splash_logo),
            contentDescription = "CardVault",
            modifier = Modifier
                .size(140.dp)
                .graphicsLayer {
                    scaleX = iconScale.value
                    scaleY = iconScale.value
                    alpha = iconAlpha.value
                },
        )
    }
}
