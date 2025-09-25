package com.technitedminds.wallet.presentation.components.animation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Animation utilities and modifiers for enhanced user interactions.
 */

/**
 * Bounce animation modifier for interactive elements
 */
fun Modifier.bounceClick(
    scaleDown: Float = 0.95f,
    onClick: (() -> Unit)? = null
): Modifier = composed {
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) scaleDown else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "bounce_scale"
    )
    
    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    isPressed = true
                    tryAwaitRelease()
                    isPressed = false
                },
                onTap = { onClick?.invoke() }
            )
        }
}

/**
 * Shake animation modifier for error states
 */
fun Modifier.shake(trigger: Boolean): Modifier = composed {
    val shakeOffset by animateFloatAsState(
        targetValue = if (trigger) 1f else 0f,
        animationSpec = keyframes {
            durationMillis = 400
            0f at 0
            10f at 50
            -10f at 100
            10f at 150
            -10f at 200
            5f at 250
            -5f at 300
            0f at 400
        },
        label = "shake_offset"
    )
    
    this.graphicsLayer {
        translationX = shakeOffset
    }
}

/**
 * Pulse animation modifier for attention-grabbing elements
 */
fun Modifier.pulse(
    enabled: Boolean = true,
    minScale: Float = 0.95f,
    maxScale: Float = 1.05f,
    duration: Int = 1000
): Modifier = composed {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_transition")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = minScale,
        targetValue = maxScale,
        animationSpec = infiniteRepeatable(
            animation = tween(duration, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    
    if (enabled) {
        this.graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
    } else {
        this
    }
}

/**
 * Shimmer animation modifier for loading states
 */
fun Modifier.shimmer(
    enabled: Boolean = true,
    duration: Int = 1500
): Modifier = composed {
    if (!enabled) return@composed this
    
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer_transition")
    
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(duration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_offset"
    )
    
    this.graphicsLayer {
        translationX = shimmerOffset * size.width
        alpha = 0.7f + (shimmerOffset + 1f) * 0.15f
    }
}

/**
 * Slide up animation from bottom
 */
fun slideUpFromBottom(
    duration: Int = 300,
    delay: Int = 0
): EnterTransition {
    return slideInVertically(
        initialOffsetY = { it },
        animationSpec = tween(duration, delayMillis = delay, easing = EaseOutCubic)
    ) + fadeIn(
        animationSpec = tween(duration, delayMillis = delay)
    )
}

/**
 * Slide down to bottom animation
 */
fun slideDownToBottom(
    duration: Int = 300
): ExitTransition {
    return slideOutVertically(
        targetOffsetY = { it },
        animationSpec = tween(duration, easing = EaseInCubic)
    ) + fadeOut(
        animationSpec = tween(duration)
    )
}

/**
 * Slide in from right animation
 */
fun slideInFromRight(
    duration: Int = 300,
    delay: Int = 0
): EnterTransition {
    return slideInHorizontally(
        initialOffsetX = { it },
        animationSpec = tween(duration, delayMillis = delay, easing = EaseOutCubic)
    ) + fadeIn(
        animationSpec = tween(duration, delayMillis = delay)
    )
}

/**
 * Slide out to left animation
 */
fun slideOutToLeft(
    duration: Int = 300
): ExitTransition {
    return slideOutHorizontally(
        targetOffsetX = { -it },
        animationSpec = tween(duration, easing = EaseInCubic)
    ) + fadeOut(
        animationSpec = tween(duration)
    )
}

/**
 * Scale and fade in animation
 */
fun scaleAndFadeIn(
    initialScale: Float = 0.8f,
    duration: Int = 300,
    delay: Int = 0
): EnterTransition {
    return scaleIn(
        initialScale = initialScale,
        animationSpec = tween(duration, delayMillis = delay, easing = EaseOutBack)
    ) + fadeIn(
        animationSpec = tween(duration, delayMillis = delay)
    )
}

/**
 * Scale and fade out animation
 */
fun scaleAndFadeOut(
    targetScale: Float = 0.8f,
    duration: Int = 300
): ExitTransition {
    return scaleOut(
        targetScale = targetScale,
        animationSpec = tween(duration, easing = EaseInBack)
    ) + fadeOut(
        animationSpec = tween(duration)
    )
}

/**
 * Spring-based bounce animation
 */
fun bounceIn(
    initialScale: Float = 0f,
    dampingRatio: Float = Spring.DampingRatioLowBouncy,
    stiffness: Float = Spring.StiffnessMedium
): EnterTransition {
    return scaleIn(
        initialScale = initialScale,
        animationSpec = spring(
            dampingRatio = dampingRatio,
            stiffness = stiffness
        )
    ) + fadeIn()
}

/**
 * Expand animation for revealing content
 */
fun expandIn(
    expandFrom: Alignment = Alignment.Center,
    duration: Int = 300
): EnterTransition {
    return expandIn(
        expandFrom = expandFrom,
        animationSpec = tween(duration, easing = EaseOutCubic)
    ) + fadeIn(
        animationSpec = tween(duration)
    )
}

/**
 * Shrink animation for hiding content
 */
fun shrinkOut(
    shrinkTowards: Alignment = Alignment.Center,
    duration: Int = 300
): ExitTransition {
    return shrinkOut(
        shrinkTowards = shrinkTowards,
        animationSpec = tween(duration, easing = EaseInCubic)
    ) + fadeOut(
        animationSpec = tween(duration)
    )
}

/**
 * Floating animation modifier
 */
fun Modifier.floating(
    enabled: Boolean = true,
    offsetRange: Dp = 4.dp,
    duration: Int = 2000
): Modifier = composed {
    if (!enabled) return@composed this
    
    val density = LocalDensity.current
    val offsetPx = with(density) { offsetRange.toPx() }
    
    val infiniteTransition = rememberInfiniteTransition(label = "floating_transition")
    
    val offsetY by infiniteTransition.animateFloat(
        initialValue = -offsetPx,
        targetValue = offsetPx,
        animationSpec = infiniteRepeatable(
            animation = tween(duration, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floating_offset"
    )
    
    this.graphicsLayer {
        translationY = offsetY
    }
}

/**
 * Rotation animation modifier
 */
fun Modifier.rotate(
    enabled: Boolean = true,
    duration: Int = 2000
): Modifier = composed {
    if (!enabled) return@composed this
    
    val infiniteTransition = rememberInfiniteTransition(label = "rotation_transition")
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(duration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation_angle"
    )
    
    this.graphicsLayer {
        rotationZ = rotation
    }
}

/**
 * Breathing animation (scale in and out)
 */
fun Modifier.breathe(
    enabled: Boolean = true,
    minScale: Float = 0.98f,
    maxScale: Float = 1.02f,
    duration: Int = 2000
): Modifier = composed {
    if (!enabled) return@composed this
    
    val infiniteTransition = rememberInfiniteTransition(label = "breathe_transition")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = minScale,
        targetValue = maxScale,
        animationSpec = infiniteRepeatable(
            animation = tween(duration, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathe_scale"
    )
    
    this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/**
 * Staggered animation delay calculator
 */
fun calculateStaggerDelay(
    index: Int,
    baseDelay: Int = 50,
    maxDelay: Int = 500
): Int {
    return (index * baseDelay).coerceAtMost(maxDelay)
}

/**
 * Animation duration based on distance
 */
fun calculateAnimationDuration(
    distance: Float,
    baseSpeed: Float = 1000f, // pixels per second
    minDuration: Int = 200,
    maxDuration: Int = 800
): Int {
    val calculatedDuration = (distance / baseSpeed * 1000).toInt()
    return calculatedDuration.coerceIn(minDuration, maxDuration)
}