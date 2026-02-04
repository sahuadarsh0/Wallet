package com.technitedminds.wallet.presentation.components.animation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import com.technitedminds.wallet.domain.model.Card
import com.technitedminds.wallet.presentation.components.common.getIcon
import com.technitedminds.wallet.presentation.components.sharing.CardSharingOption
import com.technitedminds.wallet.presentation.constants.AppConstants

/**
 * Clean and beautiful 3D flippable card with smooth animations
 */
@Composable
fun EnhancedFlippableCard(
    card: Card,
    modifier: Modifier = Modifier,
    isFlipped: Boolean = false,
    onFlip: () -> Unit = {},
    onShare: ((CardSharingOption) -> Unit)? = null,
    onLongPress: (() -> Unit)? = null,
    showShareButtons: Boolean = true,
    elevation: androidx.compose.ui.unit.Dp = 8.dp
) {
    val haptic = LocalHapticFeedback.current
    
    // Smooth flip animation with proper easing
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(
            durationMillis = AppConstants.Animation.FLIP_ANIMATION_DURATION,
            easing = EaseInOutCubic
        ),
        label = AppConstants.AnimationValues.LABEL_FLIP
    )
    
    // Subtle hover effect
    var isHovered by remember { mutableStateOf(false) }
    val hoverScale by animateFloatAsState(
        targetValue = if (isHovered) 1.02f else 1f,
        animationSpec = tween(AppConstants.Animation.HOVER_SCALE_DURATION),
        label = AppConstants.AnimationValues.LABEL_HOVER_SCALE
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.6f)
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .scale(hoverScale)
                .graphicsLayer {
                    rotationY = rotation
                    cameraDistance = AppConstants.Animation.CAMERA_DISTANCE * density // Increased for smoother perspective
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            isHovered = true
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            tryAwaitRelease()
                            isHovered = false
                        },
                        onTap = { onFlip() },
                        onLongPress = { 
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onLongPress?.invoke() 
                        }
                    )
                },
            elevation = CardDefaults.cardElevation(defaultElevation = elevation),
            shape = RoundedCornerShape(AppConstants.Dimensions.ICON_SIZE_MEDIUM), // More rounded for premium look
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            )
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (rotation <= 90f) {
                    // Front side with actual card content
                    CardFrontContent(
                        card = card,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Back side with flipped content
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer { rotationY = 180f }
                    ) {
                        CardBackContent(
                            card = card,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                
                // Share buttons overlay (only on front and when not flipped)
                if (showShareButtons && onShare != null && rotation <= 90f) {
                    ShareButtonsOverlay(
                        onShare = onShare,
                        modifier = Modifier.align(Alignment.BottomEnd)
                    )
                }
            }
        }
    }
}

/**
 * Card front content with proper image/gradient display
 */
@Composable
private fun CardFrontContent(
    card: Card,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(AppConstants.Dimensions.ICON_SIZE_MEDIUM))
            .background(
                brush = if (card.frontImagePath.isNotBlank() && !card.type.supportsOCR()) {
                    // For image cards, use a subtle overlay
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = AppConstants.AnimationValues.ALPHA_SECONDARY),
                            Color.Transparent,
                            Color.Black.copy(alpha = AppConstants.AnimationValues.ALPHA_MEDIUM)
                        )
                    )
                } else {
                    // For credit/debit cards, use gradient
                    Brush.linearGradient(
                        colors = listOf(
                            Color(card.getDisplayColor().toColorInt()),
                            Color(card.getDisplayColor().toColorInt()).copy(alpha = AppConstants.AnimationValues.ALPHA_NEAR_OPAQUE)
                        )
                    )
                }
            )
    ) {
        // Background image for non-OCR cards
        if (card.frontImagePath.isNotBlank() && !card.type.supportsOCR()) {
            // TODO: Add AsyncImage here when Coil is available
            // For now, show a placeholder with the card type icon
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = card.type.getIcon(),
                    contentDescription = null,
                    tint = Color.White.copy(alpha = AppConstants.AnimationValues.ALPHA_SECONDARY),
                    modifier = Modifier.size(80.dp)
                )
            }
        }
        
        // Card content overlay
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(AppConstants.Dimensions.PADDING_MEDIUM),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = card.name,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White,
                        maxLines = 2
                    )
                    Text(
                        text = card.type.getDisplayName(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = AppConstants.AnimationValues.ALPHA_ALMOST_OPAQUE)
                    )
                }
                
                Icon(
                    imageVector = card.type.getIcon(),
                    contentDescription = null,
                    tint = Color.White.copy(alpha = AppConstants.AnimationValues.ALPHA_ALMOST_OPAQUE),
                    modifier = Modifier.size(AppConstants.Dimensions.ICON_SIZE_EXTRA_LARGE)
                )
            }
            
            // Bottom section - show card number for OCR cards
            if (card.type.supportsOCR() && card.getCardNumber() != null) {
                Text(
                    text = String.format(AppConstants.UIText.CARD_NUMBER_FORMAT, card.getCardNumber()?.takeLast(4) ?: "****"),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 2.sp
                    ),
                    color = Color.White
                )
            }
        }
    }
}

/**
 * Card back content
 */
@Composable
private fun CardBackContent(
    card: Card,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(AppConstants.Dimensions.ICON_SIZE_MEDIUM))
            .background(
                brush = if (card.backImagePath.isNotBlank() && !card.type.supportsOCR()) {
                    // For image cards, use a subtle overlay
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = AppConstants.AnimationValues.ALPHA_SECONDARY),
                            Color.Transparent,
                            Color.Black.copy(alpha = AppConstants.AnimationValues.ALPHA_MEDIUM)
                        )
                    )
                } else {
                    // For credit/debit cards, use darker gradient
                    Brush.linearGradient(
                        colors = listOf(
                            Color(card.getDisplayColor().toColorInt()).copy(alpha = AppConstants.AnimationValues.ALPHA_NEAR_OPAQUE),
                            Color(card.getDisplayColor().toColorInt()).copy(alpha = AppConstants.AnimationValues.ALPHA_HIGH)
                        )
                    )
                }
            )
    ) {
        // Background image for non-OCR cards
        if (card.backImagePath.isNotBlank() && !card.type.supportsOCR()) {
            // TODO: Add AsyncImage here when Coil is available
            // For now, show a placeholder
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = card.type.getIcon(),
                    contentDescription = null,
                    tint = Color.White.copy(alpha = AppConstants.AnimationValues.ALPHA_SECONDARY),
                    modifier = Modifier.size(80.dp)
                )
            }
        }
        
        // Back content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(AppConstants.Dimensions.PADDING_MEDIUM),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Magnetic stripe simulation for credit/debit cards
            if (card.type.supportsOCR()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .background(
                            Color.Black,
                            RoundedCornerShape(AppConstants.Dimensions.SPACING_EXTRA_SMALL)
                        )
                )
            }
            
            // CVV area for credit/debit cards
            if (card.type.supportsOCR()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = AppConstants.UIText.CVV_LABEL,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = AppConstants.AnimationValues.ALPHA_VERY_HIGH)
                        )
                        Box(
                            modifier = Modifier
                                .width(60.dp)
                                .height(24.dp)
                                .background(
                                    Color.White,
                                    RoundedCornerShape(AppConstants.Dimensions.SPACING_EXTRA_SMALL)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = card.getCVV()?.let { AppConstants.UIText.MASKED_CVV } ?: AppConstants.UIText.MASKED_CVV,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Black
                            )
                        }
                    }
                }
            } else {
                // For other cards, show additional info
                Text(
                    text = AppConstants.UIText.ADDITIONAL_INFO_BACK,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = AppConstants.AnimationValues.ALPHA_NEAR_OPAQUE),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Enhanced share buttons overlay with smooth animations
 */
@Composable
private fun ShareButtonsOverlay(
    onShare: (CardSharingOption) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    Box(modifier = modifier.padding(AppConstants.Dimensions.PADDING_MEDIUM)) {
        // Main share button
        FloatingActionButton(
            onClick = { 
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                isExpanded = !isExpanded 
            },
            modifier = Modifier.size(48.dp),
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = AppConstants.AnimationValues.ALPHA_ALMOST_OPAQUE),
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = AppConstants.UIText.SHARE_OPTIONS,
                modifier = Modifier.size(AppConstants.Dimensions.ICON_SIZE_MEDIUM)
            )
        }

        // Expanded share options
        AnimatedVisibility(
            visible = isExpanded,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            ) + fadeIn(),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(AppConstants.Animation.EXIT_ANIMATION_DURATION)
            ) + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .offset(y = (-60).dp)
                    .padding(bottom = AppConstants.Dimensions.SPACING_SMALL),
                verticalArrangement = Arrangement.spacedBy(AppConstants.Dimensions.SPACING_SMALL),
                horizontalAlignment = Alignment.End
            ) {
                ShareOptionButton(
                    text = AppConstants.UIText.SHARE_FRONT_ONLY,
                    onClick = { 
                        onShare(CardSharingOption.FrontOnly)
                        isExpanded = false
                    }
                )
                ShareOptionButton(
                    text = AppConstants.UIText.SHARE_BOTH_SIDES,
                    onClick = { 
                        onShare(CardSharingOption.BothSides)
                        isExpanded = false
                    }
                )
                ShareOptionButton(
                    text = AppConstants.UIText.SHARE_INFO_ONLY,
                    onClick = { 
                        onShare(CardSharingOption.FrontOnly) // Using FrontOnly as fallback
                        isExpanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ShareOptionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = AppConstants.AnimationValues.LABEL_SHARE_BUTTON_SCALE
    )

    Surface(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        modifier = modifier.scale(scale),
        shape = RoundedCornerShape(AppConstants.Dimensions.ICON_SIZE_MEDIUM),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        shadowElevation = AppConstants.Dimensions.CARD_ELEVATION_DEFAULT
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = AppConstants.Dimensions.SPACING_MEDIUM, vertical = 6.dp)
        )
    }
}

/**
 * Enhanced slide-in animation for list items
 */
@Composable
fun EnhancedSlideInItem(
    visible: Boolean,
    index: Int = 0,
    baseDelay: Int = AppConstants.Animation.SLIDE_IN_DURATION_INCREMENT,
    content: @Composable () -> Unit
) {
    val delay = index * baseDelay
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(
                durationMillis = AppConstants.Animation.SLIDE_IN_DURATION_BASE + delay,
                delayMillis = delay,
                easing = EaseOutCubic
            )
        ) + fadeIn(
            animationSpec = tween(
                durationMillis = AppConstants.Animation.SLIDE_IN_DURATION_BASE + delay,
                delayMillis = delay,
                easing = EaseOutCubic
            )
        ),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(AppConstants.Animation.EXIT_ANIMATION_DURATION)
        ) + fadeOut(animationSpec = tween(AppConstants.Animation.EXIT_ANIMATION_DURATION))
    ) {
        content()
    }
}

/**
 * Enhanced stagger animation for multiple items
 */
@Composable
fun EnhancedStaggeredAnimation(
    items: List<Any>,
    content: @Composable (index: Int, item: Any) -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(items) {
        visible = true
    }

    items.forEachIndexed { index, item ->
        EnhancedSlideInItem(
            visible = visible,
            index = index
        ) {
            content(index, item)
        }
    }
}

/**
 * Enhanced shimmer loading effect
 */
@Composable
fun EnhancedShimmerEffect(
    modifier: Modifier = Modifier,
    isLoading: Boolean = true
) {
    val transition = rememberInfiniteTransition(label = AppConstants.AnimationValues.LABEL_SHIMMER)
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(AppConstants.Animation.SHIMMER_ANIMATION_DURATION, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = AppConstants.AnimationValues.LABEL_SHIMMER_TRANSLATE
    )

    if (isLoading) {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(AppConstants.Dimensions.CORNER_RADIUS_COMPACT))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = AppConstants.AnimationValues.ALPHA_SECONDARY),
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = AppConstants.AnimationValues.ALPHA_HIGH),
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = AppConstants.AnimationValues.ALPHA_SECONDARY)
                        ),
                        start = androidx.compose.ui.geometry.Offset(translateAnim - 200f, 0f),
                        end = androidx.compose.ui.geometry.Offset(translateAnim, 0f)
                    )
                )
        )
    }
}

/**
 * Enhanced bounce animation for interactive elements
 */
@Composable
fun EnhancedBounceClickable(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = AppConstants.AnimationValues.LABEL_BOUNCE_SCALE
    )

    Box(
        modifier = modifier
            .scale(scale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = {
                        onClick()
                    }
                )
            }
    ) {
        content()
    }
}