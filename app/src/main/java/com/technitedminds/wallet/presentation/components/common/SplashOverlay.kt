package com.technitedminds.wallet.presentation.components.common

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.technitedminds.wallet.R
import com.technitedminds.wallet.ui.theme.WalletBlue
import com.technitedminds.wallet.ui.theme.WalletPink
import com.technitedminds.wallet.ui.theme.WalletPurple
import com.technitedminds.wallet.ui.theme.WalletSpring
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Full-screen splash overlay with physics-based animations.
 *
 * - Animated radial gradient background that breathes (pink → purple → blue at low brightness)
 * - Spring-bounce icon entrance with overshoot
 * - Spring-based breathing pulse
 * - "CardVault" text fades in during hold, slides down on exit
 * - Zoom-through exit with spring
 */
@Composable
fun SplashOverlay(
    onSplashFinished: () -> Unit,
) {
    // --- Animation state ---
    val iconScale = remember { Animatable(0.4f) }
    val iconAlpha = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }
    val textTranslateY = remember { Animatable(0f) }
    val overlayAlpha = remember { Animatable(1f) }

    // Breathing background gradient shift
    val infiniteTransition = rememberInfiniteTransition(label = "splash_bg")
    val gradientShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "gradient_shift",
    )

    LaunchedEffect(Unit) {
        // Phase 1: Entrance — spring bounce + alpha fade
        launch {
            iconScale.animateTo(
                targetValue = 1f,
                animationSpec = WalletSpring.bouncy(),
            )
        }
        launch {
            iconAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 400),
            )
        }

        // Wait for spring to settle
        delay(600)

        // Phase 2: Text fade in + breathing pulse
        launch {
            textAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 500),
            )
        }

        // Spring-based breathing pulse
        iconScale.animateTo(
            targetValue = 1.04f,
            animationSpec = WalletSpring.gentle(),
        )
        iconScale.animateTo(
            targetValue = 1f,
            animationSpec = WalletSpring.gentle(),
        )

        // Brief hold
        delay(200)

        // Phase 3: Exit — zoom-through + text slides down + overlay fades
        launch {
            iconScale.animateTo(
                targetValue = 1.5f,
                animationSpec = WalletSpring.snappy(),
            )
        }
        launch {
            iconAlpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 400),
            )
        }
        launch {
            textAlpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 300),
            )
        }
        launch {
            textTranslateY.animateTo(
                targetValue = 40f,
                animationSpec = WalletSpring.gentle(),
            )
        }
        launch {
            overlayAlpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 450),
            )
        }

        delay(500)
        onSplashFinished()
    }

    // Animated radial gradient — center color shifts between pink and purple tones
    val centerColor = lerp(
        Color(0xFF1A0A14),
        Color(0xFF140A1E),
        gradientShift,
    )
    val splashGradient = Brush.radialGradient(
        colors = listOf(
            centerColor,
            Color(0xFF12081E),
            Color(0xFF08080F),
        ),
        center = Offset(0.5f, 0.4f),
        radius = 1200f,
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { alpha = overlayAlpha.value }
            .background(splashGradient),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
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

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "CardVault",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = Color.White,
                modifier = Modifier.graphicsLayer {
                    alpha = textAlpha.value
                    translationY = textTranslateY.value
                },
            )
        }
    }
}

/** Simple color lerp helper */
private fun lerp(start: Color, stop: Color, fraction: Float): Color {
    return Color(
        red = start.red + (stop.red - start.red) * fraction,
        green = start.green + (stop.green - start.green) * fraction,
        blue = start.blue + (stop.blue - start.blue) * fraction,
        alpha = start.alpha + (stop.alpha - start.alpha) * fraction,
    )
}
