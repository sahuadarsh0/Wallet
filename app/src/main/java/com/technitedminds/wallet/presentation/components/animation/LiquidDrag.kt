package com.technitedminds.wallet.presentation.components.animation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.technitedminds.wallet.ui.theme.WalletSpring
import kotlinx.coroutines.launch

/**
 * Modifier that allows the element to be dragged with a liquid-feel wobble.
 *
 * On drag the element follows the finger. On release it wobbles back to its
 * origin using [WalletSpring.liquid]. A subtle rotationZ tilt is applied
 * during drag for a natural 3D feel.
 *
 * @param maxOffset Maximum offset in each direction before clamping.
 * @param enableTilt Whether to apply rotationZ tilt proportional to X offset.
 */
fun Modifier.liquidDrag(
    maxOffset: Dp = 20.dp,
    enableTilt: Boolean = true,
): Modifier = composed {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val maxPx = with(density) { maxOffset.toPx() }
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }

    this
        .pointerInput(Unit) {
            detectDragGestures(
                onDrag = { change, dragAmount ->
                    change.consume()
                    scope.launch {
                        offsetX.snapTo(
                            (offsetX.value + dragAmount.x).coerceIn(-maxPx, maxPx),
                        )
                    }
                    scope.launch {
                        offsetY.snapTo(
                            (offsetY.value + dragAmount.y).coerceIn(-maxPx, maxPx),
                        )
                    }
                },
                onDragEnd = {
                    scope.launch { offsetX.animateTo(0f, WalletSpring.liquid()) }
                    scope.launch { offsetY.animateTo(0f, WalletSpring.liquid()) }
                },
                onDragCancel = {
                    scope.launch { offsetX.animateTo(0f, WalletSpring.liquid()) }
                    scope.launch { offsetY.animateTo(0f, WalletSpring.liquid()) }
                },
            )
        }
        .graphicsLayer {
            translationX = offsetX.value
            translationY = offsetY.value
            if (enableTilt) {
                rotationZ = offsetX.value * 0.05f
            }
        }
}

/**
 * Simpler press-only variant: scales down on press with elastic spring,
 * then bounces back on release. No drag tracking.
 *
 * @param pressScale Scale factor when pressed (1.0 = no change).
 */
fun Modifier.liquidPress(
    pressScale: Float = 0.95f,
): Modifier = composed {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressScale else 1f,
        animationSpec = WalletSpring.elastic(),
        label = "liquid_press",
    )

    this
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    isPressed = true
                    tryAwaitRelease()
                    isPressed = false
                },
            )
        }
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
}

/**
 * Subtle vertical parallax tied to a [ScrollState].
 *
 * @param scrollState The scroll state to observe.
 * @param factor Multiplier for the parallax offset (0.0 = none, 0.1 = subtle).
 */
fun Modifier.parallaxScroll(
    scrollState: ScrollState,
    factor: Float = 0.1f,
): Modifier = this.graphicsLayer {
    translationY = scrollState.value * factor
}
