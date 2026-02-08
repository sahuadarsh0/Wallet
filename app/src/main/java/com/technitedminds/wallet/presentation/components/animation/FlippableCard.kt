package com.technitedminds.wallet.presentation.components.animation

import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.technitedminds.wallet.domain.model.Card
import com.technitedminds.wallet.presentation.components.sharing.CardSharingDialog
import com.technitedminds.wallet.presentation.components.sharing.CardSharingOption
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
    showShareButtons: Boolean = true,
    onShare: ((CardSharingOption) -> Unit)? = null,
    onCardClick: (() -> Unit)? = null,
    onCardLongPress: (() -> Unit)? = null,
) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current.density
    val hapticFeedback = LocalHapticFeedback.current

    var showSharingDialog by remember { mutableStateOf(false) }
    var pendingShareOption by remember { mutableStateOf<CardSharingOption?>(null) }

    val handleShareClick: (CardSharingOption) -> Unit = { option ->
        pendingShareOption = option
        showSharingDialog = true
    }

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

    Box(
        modifier = modifier
            .aspectRatio(AppConstants.Defaults.CREDIT_CARD_ASPECT_RATIO)
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

        if (!isBackVisible) {
            // Front side
            Card(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(cornerRadius),
                elevation = CardDefaults.cardElevation(defaultElevation = elevation),
            ) {
                CardFront(
                    card = card,
                    isCompact = isCompact,
                    showShareButton = showShareButtons,
                    onShare = if (onShare != null) handleShareClick else null,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        } else {
            // Back side (mirrored so text reads correctly)
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationY = 180f },
                shape = RoundedCornerShape(cornerRadius),
                elevation = CardDefaults.cardElevation(defaultElevation = elevation),
            ) {
                CardBack(
                    card = card,
                    isCompact = isCompact,
                    showShareButton = showShareButtons,
                    onShare = if (onShare != null) handleShareClick else null,
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

        // Share both sides button
        if (showShareButtons && onShare != null && !isCompact && card.backImagePath.isNotBlank()) {
            ShareBothSidesButton(
                onShare = { handleShareClick(CardSharingOption.BothSides) },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(AppConstants.Dimensions.SPACING_SMALL),
            )
        }
    }

    // Sharing dialog
    if (showShareButtons && onShare != null && !isCompact) {
        CardSharingDialog(
            card = card,
            isVisible = showSharingDialog,
            onDismiss = {
                showSharingDialog = false
                pendingShareOption = null
            },
            initialOption = pendingShareOption,
            onShare = { option, _ ->
                onShare(option)
                showSharingDialog = false
                pendingShareOption = null
            },
        )
    }
}

/**
 * Flip indicator component
 */
@Composable
private fun FlipIndicator(
    isFlipped: Boolean,
    onFlip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .size(AppConstants.Dimensions.ICON_SIZE_EXTRA_LARGE)
            .clickable { onFlip() },
        shape = androidx.compose.foundation.shape.CircleShape,
        color = androidx.compose.ui.graphics.Color.Black.copy(alpha = AppConstants.AnimationValues.ALPHA_HIGH),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize(),
        ) {
            Text(
                text = if (isFlipped) "F" else "B",
                style = MaterialTheme.typography.labelMedium,
                color = androidx.compose.ui.graphics.Color.White,
            )
        }
    }
}

/**
 * Share both sides button
 */
@Composable
private fun ShareBothSidesButton(
    onShare: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.clickable { onShare() },
        shape = RoundedCornerShape(AppConstants.Dimensions.CORNER_RADIUS_LARGE),
        color = androidx.compose.ui.graphics.Color.Black.copy(alpha = AppConstants.AnimationValues.ALPHA_HIGH),
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = AppConstants.Dimensions.SPACING_MEDIUM,
                vertical = 6.dp,
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppConstants.Dimensions.SPACING_EXTRA_SMALL),
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = AppConstants.ContentDescriptions.SHARE_BOTH_SIDES,
                tint = androidx.compose.ui.graphics.Color.White,
                modifier = Modifier.size(AppConstants.Dimensions.ICON_SIZE_SMALL),
            )
            Text(
                text = AppConstants.UIText.BOTH_SIDES,
                style = MaterialTheme.typography.labelSmall,
                color = androidx.compose.ui.graphics.Color.White,
            )
        }
    }
}

/**
 * Enhanced flippable card — alias to [FlippableCard] with magnetic drag.
 * Kept for backward compatibility with screens that reference this name.
 */
@Composable
fun EnhancedFlippableCard(
    card: Card,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false,
    showShareButtons: Boolean = true,
    enableSwipeToFlip: Boolean = true,
    onShare: ((CardSharingOption) -> Unit)? = null,
    onCardClick: (() -> Unit)? = null,
    onCardLongPress: (() -> Unit)? = null,
) {
    FlippableCard(
        card = card,
        modifier = modifier,
        isCompact = isCompact,
        showShareButtons = showShareButtons,
        onShare = onShare,
        onCardClick = onCardClick,
        onCardLongPress = onCardLongPress,
    )
}

/**
 * Card controls overlay — kept for backward compat but
 * controls are now built into [FlippableCard] directly.
 */
@Composable
private fun CardControls(
    card: Card,
    isFlipped: Boolean,
    onFlip: () -> Unit,
    onShare: ((CardSharingOption) -> Unit)?,
    showShareButtons: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        if (card.backImagePath.isNotBlank()) {
            FlipIndicator(
                isFlipped = isFlipped,
                onFlip = onFlip,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(AppConstants.Dimensions.SPACING_SMALL),
            )
        }
        if (showShareButtons && onShare != null && card.backImagePath.isNotBlank()) {
            ShareBothSidesButton(
                onShare = { onShare(CardSharingOption.BothSides) },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(AppConstants.Dimensions.SPACING_SMALL),
            )
        }
    }
}
