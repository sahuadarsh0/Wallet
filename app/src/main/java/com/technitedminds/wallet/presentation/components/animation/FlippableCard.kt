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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
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
    /**
     * Tilt in degrees from a sensor (or any source). `first` = rollDeg (right-positive),
     * `second` = pitchDeg (toward-user-positive). Pass (0,0) to disable.
     * Recommended source range: ±14°. Drives 3D parallax on the card itself —
     * NOTHING is rendered outside the card's clipped bounds.
     */
    tiltDeg: Pair<Float, Float> = 0f to 0f,
) {
    val rollDeg = tiltDeg.first
    val pitchDeg = tiltDeg.second
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
                // Faceness: 1 when fully front-facing, 0 at edge-on (90°).
                // We only add tilt rotation while a face is mostly visible so the
                // flip animation isn't disrupted, and we never over-rotate.
                val deg = rotation.value
                val faceness = kotlin.math.abs(kotlin.math.cos(Math.toRadians(deg.toDouble())).toFloat())
                rotationY = deg + rollDeg * 0.7f * faceness
                rotationX = -pitchDeg * 0.7f * faceness
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
                Box(modifier = Modifier.fillMaxSize()) {
                    // ── Laminate body slab (the "side face" of the card) ──
                    // Sits behind the front face and translates opposite to the
                    // tilt so its receding edge peeks out — exactly like the
                    // edge thickness you see on a real plastic card.
                    // Clipped by the parent Card's RoundedCornerShape, so it
                    // can never leak past the card's silhouette.
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .graphicsLayer {
                                translationX = -rollDeg / 14f * 10.dp.toPx()
                                translationY = pitchDeg / 14f * 10.dp.toPx()
                            }
                            .drawWithContent {
                                // Dark slab body
                                drawRect(
                                    color = Color.Black.copy(alpha = 0.85f),
                                    size = size,
                                )
                                // Subtle inner top-edge highlight on the slab
                                // so the visible band looks like a lit laminate
                                // strip, not just a black bar.
                                drawRect(
                                    brush = Brush.verticalGradient(
                                        colorStops = arrayOf(
                                            0.0f to Color.White.copy(alpha = 0.10f),
                                            0.06f to Color.Transparent,
                                        ),
                                    ),
                                    size = size,
                                )
                            },
                    )
                    // ── Front face (the printed surface of the card) ──
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .drawWithContent {
                                drawContent()
                                drawTiltSheen(size, rollDeg, pitchDeg)
                            },
                    ) {
                        CardFront(
                            card = card,
                            isCompact = isCompact,
                            showShareButton = false,
                            onShare = null,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
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
                Box(modifier = Modifier.fillMaxSize()) {
                    // Laminate body slab (mirrored for back face)
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .graphicsLayer {
                                // Back face is rotated 180°, so mirror X.
                                translationX = rollDeg / 14f * 10.dp.toPx()
                                translationY = pitchDeg / 14f * 10.dp.toPx()
                            }
                            .drawWithContent {
                                drawRect(
                                    color = Color.Black.copy(alpha = 0.85f),
                                    size = size,
                                )
                                drawRect(
                                    brush = Brush.verticalGradient(
                                        colorStops = arrayOf(
                                            0.0f to Color.White.copy(alpha = 0.10f),
                                            0.06f to Color.Transparent,
                                        ),
                                    ),
                                    size = size,
                                )
                            },
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .drawWithContent {
                                drawContent()
                                drawTiltSheen(size, -rollDeg, pitchDeg)
                            },
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

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawTiltSheen(
    size: Size,
    rollDeg: Float,
    pitchDeg: Float,
) {
    val maxDeg = 14f
    val rollNorm = (rollDeg / maxDeg).coerceIn(-1f, 1f)
    val pitchNorm = (pitchDeg / maxDeg).coerceIn(-1f, 1f)
    val magnitude = kotlin.math.min(1f, kotlin.math.max(kotlin.math.abs(rollNorm), kotlin.math.abs(pitchNorm)))

    // Specular hot-spot: white core fading to fully transparent before reaching
    // the card's edge. radius < shortest side / 2 keeps it strictly inside.
    val cx = size.width / 2f + rollNorm * size.width * 0.30f
    val cy = size.height / 2f - pitchNorm * size.height * 0.30f
    val radius = kotlin.math.min(size.width, size.height) * 0.55f
    val core = (0.18f + 0.18f * magnitude).coerceIn(0f, 0.42f)
    val hot = Brush.radialGradient(
        colorStops = arrayOf(
            0.0f to Color.White.copy(alpha = core),
            0.45f to Color.White.copy(alpha = core * 0.35f),
            1.0f to Color.Transparent,
        ),
        center = Offset(cx, cy),
        radius = radius,
    )
    drawRect(brush = hot, size = size)

    // ── 3D depth edge (the "2dp side face" of the card) ─────────────────────
    // When the card tilts, the receding edge reveals the dark side wall and
    // the leading edge catches a brighter lit highlight. Both bands are drawn
    // strictly INSIDE the card's clip, so nothing can leak into the bg.
    //
    // Visible edge width: a 2dp card seen at ~10° projects only ~0.35dp — too
    // small to read. We exaggerate: base 2dp scaled by (3 + 8*magnitude) for
    // legibility while keeping the "thin laminate" feel.
    val baseEdgePx = 2.dp.toPx()
    val edgeWidthPx = baseEdgePx * (3f + 8f * magnitude)
    val widthFraction = (edgeWidthPx / size.width).coerceIn(0f, 0.18f)
    val heightFraction = (edgeWidthPx / size.height).coerceIn(0f, 0.18f)

    // Horizontal axis: roll
    if (kotlin.math.abs(rollNorm) > 0.02f) {
        val tiltingRight = rollNorm > 0f
        val darkAlpha = (0.32f * kotlin.math.abs(rollNorm)).coerceIn(0f, 0.32f)
        val liteAlpha = (0.22f * kotlin.math.abs(rollNorm)).coerceIn(0f, 0.22f)

        // Receding edge gets the dark side-wall (shadow side of the laminate).
        // Right tilt → left edge recedes → dark band on LEFT.
        val recedingDark = Brush.horizontalGradient(
            colorStops = arrayOf(
                0.0f to Color.Black.copy(alpha = darkAlpha),
                widthFraction to Color.Transparent,
            ),
            startX = if (tiltingRight) 0f else size.width,
            endX = if (tiltingRight) size.width else 0f,
        )
        drawRect(brush = recedingDark, size = size)

        // Leading edge gets a thin lit highlight (lit side of the laminate).
        val leadingLit = Brush.horizontalGradient(
            colorStops = arrayOf(
                0.0f to Color(0xFFFFF6D8).copy(alpha = liteAlpha),
                widthFraction to Color.Transparent,
            ),
            startX = if (tiltingRight) size.width else 0f,
            endX = if (tiltingRight) 0f else size.width,
        )
        drawRect(brush = leadingLit, size = size)
    }

    // Vertical axis: pitch
    if (kotlin.math.abs(pitchNorm) > 0.02f) {
        val tippingForward = pitchNorm > 0f
        val darkAlpha = (0.30f * kotlin.math.abs(pitchNorm)).coerceIn(0f, 0.30f)
        val liteAlpha = (0.18f * kotlin.math.abs(pitchNorm)).coerceIn(0f, 0.18f)

        val recedingDark = Brush.verticalGradient(
            colorStops = arrayOf(
                0.0f to Color.Black.copy(alpha = darkAlpha),
                heightFraction to Color.Transparent,
            ),
            startY = if (tippingForward) size.height else 0f,
            endY = if (tippingForward) 0f else size.height,
        )
        drawRect(brush = recedingDark, size = size)

        val leadingLit = Brush.verticalGradient(
            colorStops = arrayOf(
                0.0f to Color(0xFFFFF6D8).copy(alpha = liteAlpha),
                heightFraction to Color.Transparent,
            ),
            startY = if (tippingForward) 0f else size.height,
            endY = if (tippingForward) size.height else 0f,
        )
        drawRect(brush = leadingLit, size = size)
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

