package com.technitedminds.wallet.presentation.components.animation

import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.technitedminds.wallet.domain.model.Card
import com.technitedminds.wallet.presentation.components.sharing.CardSharingOption
import com.technitedminds.wallet.presentation.constants.AppConstants

/**
 * Flippable card component with 3D rotation animation and sharing integration.
 * Supports both textual cards (with OCR data) and image-only cards.
 */
@Composable
fun FlippableCard(
    card: Card,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false,
    showShareButtons: Boolean = true,
    onShare: ((CardSharingOption) -> Unit)? = null,
    onCardClick: (() -> Unit)? = null
) {
    var isFlipped by remember { mutableStateOf(false) }
    val hapticFeedback = LocalHapticFeedback.current
    
    // Animation for card flip
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(
            durationMillis = 600,
            easing = FastOutSlowInEasing
        ),
        label = "card_flip"
    )
    
    // Animation for card scale on press
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) AppConstants.AnimationValues.SCALE_PRESSED else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "card_scale"
    )
    
    Box(
        modifier = modifier
            .aspectRatio(AppConstants.Defaults.CREDIT_CARD_ASPECT_RATIO) // Standard credit card ratio
            .graphicsLayer {
                rotationY = rotation
                scaleX = scale
                scaleY = scale
                cameraDistance = 12f * density
            }
    ) {
        if (rotation <= 90f) {
            // Front side
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null
                    ) {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (onCardClick != null) {
                            onCardClick()
                        } else {
                            isFlipped = !isFlipped
                        }
                    },
                shape = RoundedCornerShape(if (isCompact) 8.dp else 12.dp),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = if (isCompact) 4.dp else 8.dp
                )
            ) {
                CardFront(
                    card = card,
                    isCompact = isCompact,
                    showShareButton = showShareButtons,
                    onShare = onShare,
                    modifier = Modifier.fillMaxSize()
                )
            }
        } else {
            // Back side
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationY = 180f }
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null
                    ) {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (onCardClick != null) {
                            onCardClick()
                        } else {
                            isFlipped = !isFlipped
                        }
                    },
                shape = RoundedCornerShape(if (isCompact) 8.dp else 12.dp),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = if (isCompact) 4.dp else 8.dp
                )
            ) {
                CardBack(
                    card = card,
                    isCompact = isCompact,
                    showShareButton = showShareButtons,
                    onShare = onShare,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        
        // Flip indicator (bottom center)
        if (!isCompact && card.backImagePath.isNotBlank()) {
            FlipIndicator(
                isFlipped = isFlipped,
                onFlip = { 
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    isFlipped = !isFlipped 
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(8.dp)
            )
        }
        
        // Share both sides button (top center)
        if (showShareButtons && onShare != null && !isCompact && card.backImagePath.isNotBlank()) {
            ShareBothSidesButton(
                onShare = { onShare(CardSharingOption.BothSides) },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(8.dp)
            )
        }
        
        // Press effect handler
        LaunchedEffect(isPressed) {
            if (isPressed) {
                kotlinx.coroutines.delay(100)
                isPressed = false
            }
        }
    }
}

/**
 * Flip indicator component
 */
@Composable
private fun FlipIndicator(
    isFlipped: Boolean,
    onFlip: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .size(AppConstants.Dimensions.ICON_SIZE_EXTRA_LARGE)
            .clickable { onFlip() },
        shape = androidx.compose.foundation.shape.CircleShape,
        color = androidx.compose.ui.graphics.Color.Black.copy(alpha = AppConstants.AnimationValues.ALPHA_HIGH)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = if (isFlipped) "F" else "B",
                style = MaterialTheme.typography.labelMedium,
                color = androidx.compose.ui.graphics.Color.White
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
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clickable { onShare() },
        shape = RoundedCornerShape(16.dp),
        color = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.6f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = null,
                tint = androidx.compose.ui.graphics.Color.White,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "Both",
                style = MaterialTheme.typography.labelSmall,
                color = androidx.compose.ui.graphics.Color.White
            )
        }
    }
}

/**
 * Enhanced flippable card with gesture support
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
    onCardLongPress: (() -> Unit)? = null
) {
    var isFlipped by remember { mutableStateOf(false) }
    val hapticFeedback = LocalHapticFeedback.current
    
    // Animation states
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(
            durationMillis = 600,
            easing = FastOutSlowInEasing
        ),
        label = "enhanced_card_flip"
    )
    
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "enhanced_card_scale"
    )
    
    // Gesture handling
    val gestureModifier = if (enableSwipeToFlip) {
        Modifier.pointerInput(Unit) {
            detectDragGestures(
                onDragStart = { isPressed = true },
                onDragEnd = { 
                    isPressed = false
                    // Implement swipe to flip logic here if needed
                }
            ) { _, _ -> }
        }
    } else {
        Modifier
    }
    
    Box(
        modifier = modifier
            .aspectRatio(1.586f)
            .then(gestureModifier)
            .graphicsLayer {
                rotationY = rotation
                scaleX = scale
                scaleY = scale
                cameraDistance = 12f * density
            }
    ) {
        if (rotation <= 90f) {
            // Front side
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null
                    ) {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        onCardClick?.invoke() ?: run { isFlipped = !isFlipped }
                    },
                shape = RoundedCornerShape(if (isCompact) 8.dp else 12.dp),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = if (isCompact) 4.dp else 8.dp
                )
            ) {
                CardFront(
                    card = card,
                    isCompact = isCompact,
                    showShareButton = showShareButtons,
                    onShare = onShare,
                    modifier = Modifier.fillMaxSize()
                )
            }
        } else {
            // Back side
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationY = 180f }
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null
                    ) {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        onCardClick?.invoke() ?: run { isFlipped = !isFlipped }
                    },
                shape = RoundedCornerShape(if (isCompact) 8.dp else 12.dp),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = if (isCompact) 4.dp else 8.dp
                )
            ) {
                CardBack(
                    card = card,
                    isCompact = isCompact,
                    showShareButton = showShareButtons,
                    onShare = onShare,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        
        // Enhanced controls
        if (!isCompact) {
            CardControls(
                card = card,
                isFlipped = isFlipped,
                onFlip = { 
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    isFlipped = !isFlipped 
                },
                onShare = onShare,
                showShareButtons = showShareButtons,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/**
 * Card controls overlay
 */
@Composable
private fun CardControls(
    card: Card,
    isFlipped: Boolean,
    onFlip: () -> Unit,
    onShare: ((CardSharingOption) -> Unit)?,
    showShareButtons: Boolean,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        // Flip indicator (bottom center)
        if (card.backImagePath.isNotBlank()) {
            FlipIndicator(
                isFlipped = isFlipped,
                onFlip = onFlip,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(8.dp)
            )
        }
        
        // Share both sides button (top center)
        if (showShareButtons && onShare != null && card.backImagePath.isNotBlank()) {
            ShareBothSidesButton(
                onShare = { onShare(CardSharingOption.BothSides) },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(8.dp)
            )
        }
    }
}