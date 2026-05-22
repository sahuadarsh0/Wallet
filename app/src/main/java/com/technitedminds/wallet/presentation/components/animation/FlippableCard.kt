package com.technitedminds.wallet.presentation.components.animation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.technitedminds.wallet.domain.model.Card
import com.technitedminds.wallet.presentation.components.common.gradientShadow
import com.technitedminds.wallet.presentation.constants.AppConstants
import com.technitedminds.wallet.ui.theme.WalletSpring
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Flippable card with **magnetic drag** physics for 3D rotation.
 *
 * Inspired by the WalletAnimation.kt gist:
 * - Horizontal drag rotates the card in 3D
 * - Magnetic midpoint: drag sensitivity drops near 90° (edge-on) for natural resistance
 * - Clamped to ±180° from starting face to prevent continuous spinning
 * - Velocity-based snap: fast flick triggers flip, slow drag snaps back
 * - Tap-to-flip as fallback
 * - Spring settle with `WalletSpring.card()` for premium bounce
 */
@Composable
fun FlippableCard(
    card: Card,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false,
    onCardClick: (() -> Unit)? = null,
    onCardLongPress: (() -> Unit)? = null,
) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current.density
    val hapticFeedback = LocalHapticFeedback.current

    // --- Magnetic drag rotation state ---
    val rotation = remember { Animatable(0f) }
    val dragStartFace = remember { mutableFloatStateOf(0f) }

    val normalizedAngle = (rotation.value % 360 + 360) % 360
    val isBackVisible = normalizedAngle in 90f..270f

    // Press scale for feel
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = WalletSpring.snappy(),
        label = "card_press_scale",
    )

    val gradientColors = remember(card) { getCardGradientColors(card) }

    // Subtle idle breathing glow -- pulsing shadow elevation multiplier
    val breathingGlow by rememberInfiniteTransition(label = "glow").animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing_glow"
    )
    val shadowMultiplier = if (isPressed) 1f else breathingGlow

    Box(
        modifier = modifier
            .aspectRatio(card.getDisplayAspectRatio())
            .graphicsLayer {
                rotationY = rotation.value
                scaleX = scale
                scaleY = scale
                cameraDistance = 14f * density
            }
            // Magnetic horizontal drag
            .draggable(
                state = rememberDraggableState { delta ->
                    scope.launch {
                        val direction = if (isBackVisible) -1f else 1f

                        // Magnetic midpoint: reduce sensitivity near 90° edge
                        val current = rotation.value
                        val angleInHalfTurn =
                            ((current - dragStartFace.floatValue) % 180f + 180f) % 180f
                        val distanceFromMid = abs(angleInHalfTurn - 90f)
                        val magneticFactor = androidx.compose.ui.util.lerp(
                            start = 0.25f,
                            stop = 1f,
                            fraction = (distanceFromMid / 90f).coerceIn(0f, 1f),
                        )

                        val proposed =
                            current + delta * 0.6f * magneticFactor * direction
                        val clamped = proposed.coerceIn(
                            minimumValue = dragStartFace.floatValue - 180f,
                            maximumValue = dragStartFace.floatValue + 180f,
                        )
                        rotation.snapTo(clamped)
                    }
                },
                orientation = Orientation.Horizontal,
                onDragStarted = {
                    isPressed = true
                    dragStartFace.floatValue =
                        (rotation.value / 180f).roundToInt() * 180f
                },
                onDragStopped = { velocity ->
                    isPressed = false
                    val current = rotation.value
                    val base = dragStartFace.floatValue
                    val offset = current - base

                    // Snap based on velocity + position
                    val target = when {
                        velocity > 800f -> base + 180f
                        velocity < -800f -> base - 180f
                        offset > 60f -> base + 180f
                        offset < -60f -> base - 180f
                        else -> base // snap back
                    }
                    scope.launch {
                        if (target != base) {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                        rotation.animateTo(
                            targetValue = target,
                            animationSpec = WalletSpring.card(),
                        )
                    }
                },
            )
            // Tap-to-flip fallback
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (onCardClick != null) {
                            onCardClick()
                        } else {
                            scope.launch {
                                val nearest =
                                    (rotation.value / 180f).roundToInt() * 180f
                                val target = nearest + 180f
                                rotation.animateTo(
                                    targetValue = target,
                                    animationSpec = WalletSpring.card(),
                                )
                            }
                        }
                    },
                    onLongPress = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        onCardLongPress?.invoke()
                    },
                )
            },
    ) {
        val cornerRadius = if (isCompact) {
            AppConstants.Dimensions.CORNER_RADIUS_COMPACT
        } else {
            AppConstants.Dimensions.CORNER_RADIUS_NORMAL
        }
        val elevation = if (isCompact) {
            AppConstants.Dimensions.SPACING_EXTRA_SMALL
        } else {
            AppConstants.Dimensions.SPACING_SMALL
        }

        val baseShadow = if (isCompact) 6.dp else 10.dp
        val animatedShadow = baseShadow * shadowMultiplier

        if (!isBackVisible) {
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .gradientShadow(
                        colors = gradientColors.toList(),
                        shadowElevation = animatedShadow,
                        cornerRadius = cornerRadius,
                    ),
                shape = RoundedCornerShape(cornerRadius),
                elevation = CardDefaults.cardElevation(defaultElevation = elevation),
            ) {
                CardFront(
                    card = card,
                    isCompact = isCompact,
                    showShareButton = false,
                    onShare = null,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        } else {
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .gradientShadow(
                        colors = gradientColors.toList(),
                        shadowElevation = animatedShadow,
                        cornerRadius = cornerRadius,
                    )
                    .graphicsLayer { rotationY = 180f },
                shape = RoundedCornerShape(cornerRadius),
                elevation = CardDefaults.cardElevation(defaultElevation = elevation),
            ) {
                CardBack(
                    card = card,
                    isCompact = isCompact,
                    showShareButton = false,
                    onShare = null,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        // Flip indicator
        if (!isCompact && card.backImagePath.isNotBlank()) {
            FlipIndicator(
                isFlipped = isBackVisible,
                onFlip = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    scope.launch {
                        val nearest =
                            (rotation.value / 180f).roundToInt() * 180f
                        rotation.animateTo(
                            nearest + 180f,
                            WalletSpring.card(),
                        )
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(AppConstants.Dimensions.SPACING_SMALL),
            )
        }

    }
}

private fun getCardGradientColors(card: Card): Pair<Color, Color> {
    val gradient = card.getGradient()
    val startColor = try {
        Color(gradient.startColor.toColorInt())
    } catch (e: Exception) {
        Color(Card.getDefaultGradientForType(card.type).startColor.toColorInt())
    }

    val endColor = try {
        Color(gradient.endColor.toColorInt())
    } catch (e: Exception) {
        Color(Card.getDefaultGradientForType(card.type).endColor.toColorInt())
    }
    return startColor to endColor
}

@Composable
private fun FlipIndicator(
    isFlipped: Boolean,
    onFlip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
            .clickable { onFlip() },
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier.size(32.dp),
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.2f),
            border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.3f))
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize(),
            ) {
                Icon(
                    imageVector = Icons.Default.FlipCameraAndroid,
                    contentDescription = if (isFlipped) "Show front" else "Show back",
                    tint = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

