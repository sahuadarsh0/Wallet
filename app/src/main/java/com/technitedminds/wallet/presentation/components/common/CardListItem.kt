package com.technitedminds.wallet.presentation.components.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.technitedminds.wallet.domain.model.Card
import com.technitedminds.wallet.domain.model.CardType
import com.technitedminds.wallet.presentation.constants.AppConstants
import com.technitedminds.wallet.ui.theme.WalletSpring
import java.io.File
import androidx.core.graphics.toColorInt

/**
 * Reusable card list item component with thumbnail, title, and category display.
 * Supports selection states and various interaction modes.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CardListItem(
    card: Card,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    onMenuClick: (() -> Unit)? = null,
    isSelected: Boolean = false,
    showMenu: Boolean = true,
    showCategory: Boolean = true,
    isCompact: Boolean = false
) {
    val hapticFeedback = LocalHapticFeedback.current
    val context = LocalContext.current

    // Spring press scale
    var isPressed by remember { mutableStateOf(false) }
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = WalletSpring.snappy(),
        label = "card_press_scale",
    )
    
    // Animate selection state
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(200),
        label = "card_selection_background"
    )
    
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        animationSpec = tween(200),
        label = "card_selection_content"
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                )
            }
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick?.invoke()
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) AppConstants.Dimensions.CARD_ELEVATION_SELECTED else AppConstants.Dimensions.CARD_ELEVATION_DEFAULT
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (isCompact) AppConstants.Dimensions.PADDING_MEDIUM else AppConstants.Dimensions.PADDING_LARGE),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Card thumbnail
            CardThumbnail(
                card = card,
                size = if (isCompact) AppConstants.Dimensions.CARD_THUMBNAIL_COMPACT else AppConstants.Dimensions.CARD_THUMBNAIL_NORMAL,
                modifier = Modifier.padding(end = AppConstants.Dimensions.PADDING_LARGE)
            )
            
            // Card information
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Card name
                Text(
                    text = card.name,
                    style = if (isCompact) {
                        MaterialTheme.typography.titleSmall
                    } else {
                        MaterialTheme.typography.titleMedium
                    },
                    color = contentColor,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Card type
                Text(
                    text = card.type.getDisplayName(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor.copy(alpha = AppConstants.AnimationValues.ALPHA_VERY_HIGH),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Category (if enabled and not compact)
                if (showCategory && !isCompact && card.categoryId.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(AppConstants.Dimensions.SPACING_EXTRA_SMALL))
                    CategoryChip(
                        category = resolveCategoryName(card.categoryId),
                        isCompact = true
                    )
                }
                
                // Additional info for non-compact mode
                if (!isCompact) {
                    val cardNumber = card.customFields["cardNumber"]
                    if (!cardNumber.isNullOrEmpty() && card.type.supportsOCR()) {
                        Text(
                            text = String.format(AppConstants.UIText.CARD_NUMBER_MASKED, cardNumber.takeLast(4)),
                            style = MaterialTheme.typography.bodySmall,
                            color = contentColor.copy(alpha = AppConstants.AnimationValues.ALPHA_HIGH),
                            modifier = Modifier.padding(top = AppConstants.Dimensions.CARD_ELEVATION_DEFAULT)
                        )
                    }
                }
            }
            
            // Menu button
            if (showMenu && onMenuClick != null) {
                IconButton(
                    onClick = onMenuClick,
                    modifier = Modifier.size(if (isCompact) AppConstants.Dimensions.CARD_LIST_ITEM_MENU_SIZE_COMPACT else AppConstants.Dimensions.CARD_LIST_ITEM_MENU_SIZE_NORMAL)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = AppConstants.ContentDescriptions.CARD_OPTIONS,
                        tint = contentColor.copy(alpha = AppConstants.AnimationValues.ALPHA_VERY_HIGH)
                    )
                }
            }
        }
    }
}

/**
 * Card thumbnail component with fallback to card type icon
 */
@Composable
private fun CardThumbnail(
    card: Card,
    size: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(AppConstants.Dimensions.CORNER_RADIUS_COMPACT))
            .background(getCardTypeColor(card)),
        contentAlignment = Alignment.Center
    ) {
        // Try to load card image
        if (card.frontImagePath.isNotBlank()) {
            val imageFile = File(card.frontImagePath)
            if (imageFile.exists()) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(imageFile)
                        .crossfade(true)
                        .build(),
                    contentDescription = AppConstants.ContentDescriptions.CARD_THUMBNAIL,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                return@Box
            }
        }
        
        // Fallback to card type icon
        Icon(
            imageVector = Icons.Default.CreditCard,
            contentDescription = card.type.getDisplayName(),
            tint = Color.White,
            modifier = Modifier.size(size * AppConstants.Defaults.CARD_ICON_SIZE_RATIO)
        )
    }
}

/**
 * Compact card list item for dense layouts
 */
@Composable
fun CompactCardListItem(
    card: Card,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    isSelected: Boolean = false
) {
    CardListItem(
        card = card,
        onClick = onClick,
        onLongClick = onLongClick,
        isSelected = isSelected,
        showMenu = false,
        showCategory = false,
        isCompact = true,
        modifier = modifier
    )
}

/**
 * Grid card item for grid layouts
 */
@Composable
fun GridCardItem(
    card: Card,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    isSelected: Boolean = false,
    aspectRatio: Float = AppConstants.Defaults.CREDIT_CARD_ASPECT_RATIO // Credit card ratio
) {
    val hapticFeedback = LocalHapticFeedback.current
    val context = LocalContext.current
    
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(200),
        label = "grid_card_selection"
    )
    
    Card(
        modifier = modifier
            .aspectRatio(aspectRatio)
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick?.invoke()
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) AppConstants.Dimensions.CARD_ELEVATION_SELECTED else AppConstants.Dimensions.SPACING_EXTRA_SMALL
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Background image or gradient
            if (card.frontImagePath.isNotBlank()) {
                val imageFile = File(card.frontImagePath)
                if (imageFile.exists()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(imageFile)
                            .crossfade(true)
                            .build(),
                        contentDescription = AppConstants.UIText.CARD_IMAGE,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(getCardTypeColor(card))
                    )
                } else {
                    // Fallback gradient
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(getCardTypeColor(card))
                    )
                }
            } else {
                // Default gradient background
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(getCardTypeColor(card))
                )
            }
            
            // Content overlay
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(AppConstants.Dimensions.PADDING_MEDIUM),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Card type
                Surface(
                    color = Color.White.copy(alpha = AppConstants.AnimationValues.ALPHA_ALMOST_OPAQUE),
                    shape = RoundedCornerShape(AppConstants.Dimensions.CORNER_RADIUS_COMPACT)
                ) {
                    Text(
                        text = card.type.getDisplayName(),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Black,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                
                // Card name
                Text(
                    text = card.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Get color for card type
 */
/**
 * Get color for card type with custom color support
 */
private fun getCardTypeColor(card: Card): Color {
    return try {
        Color(card.getDisplayColor().toColorInt())
    } catch (e: Exception) {
        // Fallback to type default if custom color is invalid
        Color(card.type.getDefaultColor().toColorInt())
    }
}