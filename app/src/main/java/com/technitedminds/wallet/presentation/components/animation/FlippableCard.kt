package com.technitedminds.wallet.presentation.components.animation

import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.technitedminds.wallet.domain.model.Card

/**
 * A composable that provides 3D card flip animation with smooth transitions.
 * Optimized for 60fps performance with proper easing curves.
 */
@Composable
fun FlippableCard(
    card: Card,
    modifier: Modifier = Modifier,
    isFlipped: Boolean = false,
    onFlip: () -> Unit = {},
    animationDuration: Int = 300,
    enableClick: Boolean = true,
    cardAspectRatio: Float = 1.586f // Standard credit card ratio
) {
    val hapticFeedback = LocalHapticFeedback.current
    
    // Animation state for the flip rotation
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(
            durationMillis = animationDuration,
            easing = FastOutSlowInEasing
        ),
        label = "card_flip_rotation"
    )
    
    // Calculate if we should show the back side (rotation > 90 degrees)
    val showBack = rotation > 90f
    
    Box(
        modifier = modifier
            .aspectRatio(cardAspectRatio)
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = enableClick) {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                onFlip()
            }
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            },
        contentAlignment = Alignment.Center
    ) {
        if (showBack) {
            // Show back side (rotated 180 degrees to appear correct)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        rotationY = 180f
                    }
            ) {
                CardBack(
                    card = card,
                    modifier = Modifier.fillMaxSize()
                )
            }
        } else {
            // Show front side
            CardFront(
                card = card,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/**
 * Enhanced FlippableCard with additional animation controls and state management
 */
@Composable
fun AdvancedFlippableCard(
    card: Card,
    modifier: Modifier = Modifier,
    initialSide: CardSide = CardSide.FRONT,
    onSideChanged: (CardSide) -> Unit = {},
    animationDuration: Int = 300,
    enableClick: Boolean = true,
    enableHapticFeedback: Boolean = true,
    cardAspectRatio: Float = 1.586f,
    elevation: Float = 8f
) {
    var currentSide by remember { mutableStateOf(initialSide) }
    val hapticFeedback = LocalHapticFeedback.current
    
    // Animation state with spring physics for more natural feel
    val animationSpec = remember {
        spring<Float>(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    }
    
    val rotation by animateFloatAsState(
        targetValue = when (currentSide) {
            CardSide.FRONT -> 0f
            CardSide.BACK -> 180f
        },
        animationSpec = animationSpec,
        label = "advanced_card_flip"
    )
    
    // Elevation animation for depth effect
    val cardElevation by animateFloatAsState(
        targetValue = if (rotation > 45f && rotation < 135f) elevation * 1.5f else elevation,
        animationSpec = tween(durationMillis = animationDuration / 2),
        label = "card_elevation"
    )
    
    val showBack = rotation > 90f
    
    Card(
        modifier = modifier
            .aspectRatio(cardAspectRatio)
            .clickable(enabled = enableClick) {
                if (enableHapticFeedback) {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                }
                val newSide = if (currentSide == CardSide.FRONT) CardSide.BACK else CardSide.FRONT
                currentSide = newSide
                onSideChanged(newSide)
            }
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            },
        elevation = CardDefaults.cardElevation(defaultElevation = cardElevation.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (showBack) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            rotationY = 180f
                        }
                ) {
                    CardBack(
                        card = card,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                CardFront(
                    card = card,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

/**
 * Enum representing the card sides
 */
enum class CardSide {
    FRONT,
    BACK
}

/**
 * Performance-optimized FlippableCard for use in lists
 */
@Composable
fun ListFlippableCard(
    card: Card,
    modifier: Modifier = Modifier,
    isFlipped: Boolean = false,
    onFlip: () -> Unit = {},
    cardAspectRatio: Float = 1.586f
) {
    // Simplified animation for better list performance
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(
            durationMillis = 250, // Slightly faster for lists
            easing = LinearOutSlowInEasing
        ),
        label = "list_card_flip"
    )
    
    val showBack = rotation > 90f
    
    Card(
        modifier = modifier
            .aspectRatio(cardAspectRatio)
            .clickable { onFlip() }
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 8f * density // Reduced camera distance for subtle effect
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(8.dp) // Smaller radius for list items
    ) {
        if (showBack) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationY = 180f }
            ) {
                CardBack(
                    card = card,
                    modifier = Modifier.fillMaxSize(),
                    isCompact = true
                )
            }
        } else {
            CardFront(
                card = card,
                modifier = Modifier.fillMaxSize(),
                isCompact = true
            )
        }
    }
}