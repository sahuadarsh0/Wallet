package com.technitedminds.wallet.presentation.components.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.technitedminds.wallet.domain.model.CardType
import com.technitedminds.wallet.domain.model.CardGradient
import com.technitedminds.wallet.presentation.constants.AppConstants

/**
 * Card type selector component with visual icons, descriptions, and gradient selection
 */
@Composable
fun CardTypeSelector(
    selectedType: CardType,
    onTypeSelected: (CardType) -> Unit,
    modifier: Modifier = Modifier,
    showCustomOption: Boolean = true,
    showGradientPicker: Boolean = false,
    selectedGradient: CardGradient? = null,
    onGradientSelected: ((CardGradient) -> Unit)? = null
) {
    val hapticFeedback = LocalHapticFeedback.current
    var showCustomDialog by remember { mutableStateOf(false) }
    var showGradientDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AppConstants.Dimensions.SPACING_MEDIUM)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = AppConstants.UIText.TYPE_LABEL,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            
            // Gradient picker button
            if (showGradientPicker && onGradientSelected != null) {
                OutlinedButton(
                    onClick = { showGradientDialog = true },
                    modifier = Modifier.height(AppConstants.Dimensions.CARD_TYPE_SELECTOR_BUTTON_HEIGHT)
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        modifier = Modifier.size(AppConstants.Dimensions.ICON_SIZE_SMALL)
                    )
                    Spacer(modifier = Modifier.width(AppConstants.Dimensions.SPACING_EXTRA_SMALL))
                    Text(
                        text = AppConstants.UIText.GRADIENT,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
        
        // Gradient preview (if gradient is selected)
        if (selectedGradient != null) {
            GradientPreview(
                gradient = selectedGradient,
                cardType = selectedType,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        // Predefined card types grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(AppConstants.Dimensions.SPACING_SMALL),
            verticalArrangement = Arrangement.spacedBy(AppConstants.Dimensions.SPACING_SMALL),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            items(CardType.getAllPredefinedTypes()) { cardType ->
                CardTypeOptionWithGradient(
                    cardType = cardType,
                    isSelected = selectedType::class == cardType::class,
                    gradient = if (selectedType::class == cardType::class) selectedGradient else null,
                    onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        onTypeSelected(cardType)
                    }
                )
            }
        }
        
        // Custom card type button
        if (showCustomOption) {
            OutlinedButton(
                onClick = { showCustomDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(AppConstants.Dimensions.EMPTY_STATE_BUTTON_ICON_SIZE)
                )
                Spacer(modifier = Modifier.width(AppConstants.Dimensions.SPACING_SMALL))
                Text(AppConstants.UIText.CUSTOM_CARD_TYPE)
            }
        }
    }
    
    // Custom card type dialog
    if (showCustomDialog) {
        CustomCardTypeDialog(
            onTypeCreated = { customType ->
                onTypeSelected(customType)
                showCustomDialog = false
            },
            onDismiss = { showCustomDialog = false }
        )
    }
    
    // Gradient picker dialog
    if (showGradientDialog && onGradientSelected != null) {
        GradientPickerDialog(
            isVisible = showGradientDialog,
            cardType = selectedType,
            selectedGradient = selectedGradient,
            onGradientSelected = { gradient ->
                onGradientSelected(gradient)
                showGradientDialog = false
            },
            onDismiss = { showGradientDialog = false }
        )
    }
}

/**
 * Individual card type option component
 */
@Composable
private fun CardTypeOption(
    cardType: CardType,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
        animationSpec = tween(200),
        label = "border_color"
    )
    
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(200),
        label = "background_color"
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(AppConstants.Dimensions.CARD_TYPE_OPTION_HEIGHT)
            .border(
                width = if (isSelected) AppConstants.Dimensions.CARD_TYPE_OPTION_BORDER_WIDTH_SELECTED else AppConstants.Dimensions.CARD_TYPE_OPTION_BORDER_WIDTH,
                color = borderColor,
                shape = RoundedCornerShape(AppConstants.Dimensions.CORNER_RADIUS_NORMAL)
            )
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(AppConstants.Dimensions.CORNER_RADIUS_NORMAL)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(AppConstants.Dimensions.PADDING_MEDIUM),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppConstants.Dimensions.SPACING_MEDIUM)
        ) {
            // Card type icon with color
            Box(
                modifier = Modifier
                    .size(AppConstants.Dimensions.CARD_TYPE_OPTION_ICON_CONTAINER_SIZE)
                    .clip(RoundedCornerShape(AppConstants.Dimensions.CORNER_RADIUS_COMPACT))
                    .background(
                        Color(android.graphics.Color.parseColor(cardType.getDefaultColor()))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getCardTypeIcon(cardType),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(AppConstants.Dimensions.DROPDOWN_ICON_SIZE)
                )
            }
            
            // Card type name and description
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(AppConstants.Dimensions.CARD_ELEVATION_DEFAULT)
            ) {
                Text(
                    text = cardType.getDisplayName(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                
                Text(
                    text = getCardTypeDescription(cardType),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    },
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * Custom card type creation dialog
 */
@Composable
private fun CustomCardTypeDialog(
    onTypeCreated: (CardType.Custom) -> Unit,
    onDismiss: () -> Unit
) {
    var typeName by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf("#757575") }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppConstants.Dimensions.PADDING_LARGE),
            shape = RoundedCornerShape(AppConstants.Dimensions.CORNER_RADIUS_LARGE)
        ) {
            Column(
                modifier = Modifier.padding(AppConstants.Dimensions.PADDING_EXTRA_LARGE),
                verticalArrangement = Arrangement.spacedBy(AppConstants.Dimensions.SPACING_LARGE)
            ) {
                Text(
                    text = AppConstants.UIText.CREATE_CUSTOM_CARD_TYPE,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                // Type name input
                OutlinedTextField(
                    value = typeName,
                    onValueChange = { typeName = it },
                    label = { Text(AppConstants.UIText.CARD_TYPE_NAME) },
                    placeholder = { Text(AppConstants.UIText.CARD_TYPE_NAME_PLACEHOLDER) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                // Color picker
                ColorPicker(
                    selectedColor = selectedColor,
                    onColorSelected = { selectedColor = it },
                    showCustomColorOption = true
                )
                
                // Preview
                if (typeName.isNotBlank()) {
                    Text(
                        text = AppConstants.UIText.PREVIEW_TEXT,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(AppConstants.Dimensions.PADDING_MEDIUM),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(AppConstants.Dimensions.SPACING_MEDIUM)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(AppConstants.Dimensions.CARD_TYPE_SELECTOR_BUTTON_HEIGHT)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        Color(android.graphics.Color.parseColor(selectedColor))
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CreditCard,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(AppConstants.Dimensions.ICON_SIZE_SMALL)
                                )
                            }
                            
                            Text(
                                text = typeName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                
                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppConstants.Dimensions.SPACING_SMALL, Alignment.End)
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(AppConstants.DialogText.CANCEL_BUTTON)
                    }
                    
                    Button(
                        onClick = {
                            if (typeName.isNotBlank()) {
                                onTypeCreated(CardType.Custom(typeName.trim(), selectedColor))
                            }
                        },
                        enabled = typeName.isNotBlank()
                    ) {
                        Text(AppConstants.UIText.CREATE)
                    }
                }
            }
        }
    }
}

/**
 * Get icon for card type
 */
private fun getCardTypeIcon(cardType: CardType): ImageVector = when (cardType) {
    is CardType.Credit, is CardType.Debit -> Icons.Default.CreditCard
    is CardType.GiftCard -> Icons.Default.CardGiftcard
    is CardType.LoyaltyCard -> Icons.Default.Stars
    is CardType.MembershipCard -> Icons.Default.Badge
    is CardType.InsuranceCard -> Icons.Default.HealthAndSafety
    is CardType.IdentificationCard -> Icons.Default.Badge
    is CardType.Voucher -> Icons.Default.LocalOffer
    is CardType.Event -> Icons.Default.Event
    is CardType.TransportCard -> Icons.Default.DirectionsTransit
    is CardType.BusinessCard -> Icons.Default.Business
    is CardType.LibraryCard -> Icons.AutoMirrored.Filled.MenuBook
    is CardType.HotelCard -> Icons.Default.Hotel
    is CardType.StudentCard -> Icons.Default.School
    is CardType.AccessCard -> Icons.Default.Key
    is CardType.Custom -> Icons.Default.CreditCard
}

/**
 * Get description for card type
 */
private fun getCardTypeDescription(cardType: CardType): String = when (cardType) {
    is CardType.Credit -> AppConstants.UIText.CREDIT_CARD_DESC
    is CardType.Debit -> AppConstants.UIText.DEBIT_CARD_DESC
    is CardType.GiftCard -> AppConstants.UIText.GIFT_CARD_DESC
    is CardType.LoyaltyCard -> AppConstants.UIText.LOYALTY_CARD_DESC
    is CardType.MembershipCard -> AppConstants.UIText.MEMBERSHIP_CARD_DESC
    is CardType.InsuranceCard -> AppConstants.UIText.INSURANCE_CARD_DESC
    is CardType.IdentificationCard -> AppConstants.UIText.ID_CARD_DESC
    is CardType.Voucher -> AppConstants.UIText.VOUCHER_DESC
    is CardType.Event -> AppConstants.UIText.EVENT_TICKET_DESC
    is CardType.TransportCard -> AppConstants.UIText.TRANSPORT_CARD_DESC
    is CardType.BusinessCard -> AppConstants.UIText.BUSINESS_CARD_DESC
    is CardType.LibraryCard -> AppConstants.UIText.LIBRARY_CARD_DESC
    is CardType.HotelCard -> AppConstants.UIText.HOTEL_CARD_DESC
    is CardType.StudentCard -> AppConstants.UIText.STUDENT_CARD_DESC
    is CardType.AccessCard -> AppConstants.UIText.ACCESS_CARD_DESC
    is CardType.Custom -> AppConstants.UIText.CUSTOM_CARD_DESC
}

/**
 * Enhanced card type option with gradient support
 */
@Composable
private fun CardTypeOptionWithGradient(
    cardType: CardType,
    isSelected: Boolean,
    gradient: CardGradient?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
        animationSpec = tween(200),
        label = "border_color"
    )
    
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(200),
        label = "background_color"
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(AppConstants.Dimensions.CARD_TYPE_OPTION_HEIGHT)
            .border(
                width = if (isSelected) AppConstants.Dimensions.CARD_TYPE_OPTION_BORDER_WIDTH_SELECTED else AppConstants.Dimensions.CARD_TYPE_OPTION_BORDER_WIDTH,
                color = borderColor,
                shape = RoundedCornerShape(AppConstants.Dimensions.CORNER_RADIUS_NORMAL)
            )
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(AppConstants.Dimensions.CORNER_RADIUS_NORMAL)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(AppConstants.Dimensions.PADDING_MEDIUM),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppConstants.Dimensions.SPACING_MEDIUM)
        ) {
            // Card type icon with gradient or color
            Box(
                modifier = Modifier
                    .size(AppConstants.Dimensions.CARD_TYPE_OPTION_ICON_CONTAINER_SIZE)
                    .clip(RoundedCornerShape(AppConstants.Dimensions.CORNER_RADIUS_COMPACT))
                    .then(
                        if (gradient != null) {
                            Modifier.background(createGradientBrush(gradient))
                        } else {
                            Modifier.background(
                                Color(android.graphics.Color.parseColor(cardType.getDefaultColor()))
                            )
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getCardTypeIcon(cardType),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(AppConstants.Dimensions.DROPDOWN_ICON_SIZE)
                )
            }
            
            // Card type name and description
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(AppConstants.Dimensions.CARD_ELEVATION_DEFAULT)
            ) {
                Text(
                    text = cardType.getDisplayName(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                
                Text(
                    text = getCardTypeDescription(cardType),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    },
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * Gradient preview component
 */
@Composable
private fun GradientPreview(
    gradient: CardGradient,
    cardType: CardType,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(AppConstants.Dimensions.GRADIENT_PREVIEW_HEIGHT),
        shape = RoundedCornerShape(AppConstants.Dimensions.CORNER_RADIUS_NORMAL)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(createGradientBrush(gradient))
        ) {
            // Card preview content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(AppConstants.Dimensions.PADDING_LARGE),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = cardType.getDisplayName(),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Icon(
                        imageVector = getCardTypeIcon(cardType),
                        contentDescription = null,
                        tint = Color.White.copy(alpha = AppConstants.AnimationValues.ALPHA_NEAR_OPAQUE),
                        modifier = Modifier.size(AppConstants.Dimensions.ICON_SIZE_LARGE)
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    gradient.name?.let { name ->
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = AppConstants.AnimationValues.ALPHA_ALMOST_OPAQUE)
                        )
                    }
                    
                    Text(
                        text = AppConstants.UIText.PREVIEW_TEXT,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

/**
 * Creates a Compose Brush from CardGradient
 */
private fun createGradientBrush(gradient: CardGradient): androidx.compose.ui.graphics.Brush {
    val startColor = Color(android.graphics.Color.parseColor(gradient.startColor))
    val endColor = Color(android.graphics.Color.parseColor(gradient.endColor))
    
    return when (gradient.direction) {
        com.technitedminds.wallet.domain.model.GradientDirection.TopToBottom -> androidx.compose.ui.graphics.Brush.verticalGradient(
            colors = listOf(startColor, endColor)
        )
        com.technitedminds.wallet.domain.model.GradientDirection.LeftToRight -> androidx.compose.ui.graphics.Brush.horizontalGradient(
            colors = listOf(startColor, endColor)
        )
        com.technitedminds.wallet.domain.model.GradientDirection.DiagonalTopLeftToBottomRight -> androidx.compose.ui.graphics.Brush.linearGradient(
            colors = listOf(startColor, endColor),
            start = androidx.compose.ui.geometry.Offset(0f, 0f),
            end = androidx.compose.ui.geometry.Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        )
        com.technitedminds.wallet.domain.model.GradientDirection.DiagonalTopRightToBottomLeft -> androidx.compose.ui.graphics.Brush.linearGradient(
            colors = listOf(startColor, endColor),
            start = androidx.compose.ui.geometry.Offset(Float.POSITIVE_INFINITY, 0f),
            end = androidx.compose.ui.geometry.Offset(0f, Float.POSITIVE_INFINITY)
        )
    }
}