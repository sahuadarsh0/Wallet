package com.technitedminds.wallet.presentation.components.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsTransit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.graphics.toColorInt
import com.technitedminds.wallet.domain.model.CardGradient
import com.technitedminds.wallet.domain.model.CardType
import com.technitedminds.wallet.presentation.constants.AppConstants
import com.technitedminds.wallet.ui.theme.Glass
import com.technitedminds.wallet.ui.theme.WalletSpring
import com.technitedminds.wallet.ui.theme.contrastText

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
 * Modern custom card type creation dialog with:
 * - Scrollable content (colors fully accessible)
 * - Live preview card with auto text color
 * - Character counter on name field
 * - PremiumButton actions with spring physics
 */
@Composable
private fun CustomCardTypeDialog(
    onTypeCreated: (CardType.Custom) -> Unit,
    onDismiss: () -> Unit,
) {
    var typeName by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf("#6366F1") }

    val nameMaxLength = 25
    val isValid = typeName.isNotBlank() && typeName.length <= nameMaxLength

    val parsedColor = remember(selectedColor) {
        runCatching { Color(selectedColor.toColorInt()) }
            .getOrElse { Color(0xFF6366F1) }
    }
    val textOnColor = remember(parsedColor) {
        parsedColor.contrastText()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
        ),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 24.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            shadowElevation = 12.dp,
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // -- Header --
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = AppConstants.UIText.CREATE_CUSTOM_CARD_TYPE,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                )

                // -- Scrollable Content --
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(androidx.compose.foundation.rememberScrollState())
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    // Live preview card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(parsedColor)
                                .padding(20.dp),
                        ) {
                            Row(
                                modifier = Modifier.align(Alignment.CenterStart),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CreditCard,
                                    contentDescription = null,
                                    tint = textOnColor.copy(alpha = 0.8f),
                                    modifier = Modifier.size(32.dp),
                                )
                                Column {
                                    Text(
                                        text = typeName.ifBlank { "Card Type" },
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = textOnColor,
                                    )
                                    Text(
                                        text = "Custom card type",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = textOnColor.copy(alpha = 0.7f),
                                    )
                                }
                            }
                        }
                    }

                    // Name field with character counter
                    PremiumTextField(
                        value = typeName,
                        onValueChange = { if (it.length <= nameMaxLength) typeName = it },
                        label = AppConstants.UIText.CARD_TYPE_NAME,
                        leadingIcon = Icons.Default.Label,
                        trailingIcon = {
                            Text(
                                text = "${typeName.length}/$nameMaxLength",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (typeName.length > nameMaxLength) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                            )
                        },
                        isError = typeName.length > nameMaxLength,
                        errorMessage = if (typeName.length > nameMaxLength) "Name is too long" else null,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    // Color picker (has its own header label inside)
                    ColorPicker(
                        selectedColor = selectedColor,
                        onColorSelected = { selectedColor = it },
                        showCustomColorOption = true,
                    )
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                )

                // -- Actions --
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    PremiumButton(
                        onClick = onDismiss,
                        text = "Cancel",
                        variant = PremiumButtonVariant.Secondary,
                        modifier = Modifier.weight(1f),
                    )
                    PremiumButton(
                        onClick = {
                            if (isValid) {
                                onTypeCreated(CardType.Custom(typeName.trim(), selectedColor))
                            }
                        },
                        text = AppConstants.UIText.CREATE,
                        icon = Icons.Default.Add,
                        enabled = isValid,
                        variant = PremiumButtonVariant.Primary,
                        modifier = Modifier.weight(1f),
                    )
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
    modifier: Modifier = Modifier,
) {
    // Spring bounce on press
    var isPressed by remember { mutableStateOf(false) }
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = WalletSpring.bouncy(),
        label = "type_press_scale",
    )

    // Selection overshoot scale
    val selectionScale by animateFloatAsState(
        targetValue = if (isSelected) 1f else 1f,
        animationSpec = WalletSpring.bouncy(),
        label = "type_selection_scale",
    )

    val borderBrush = if (isSelected) Glass.colors.border else null
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
        animationSpec = WalletSpring.snappy(),
        label = "border_color",
    )
    
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        animationSpec = WalletSpring.snappy(),
        label = "background_color",
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(AppConstants.Dimensions.CARD_TYPE_OPTION_HEIGHT)
            .graphicsLayer {
                scaleX = pressScale * selectionScale
                scaleY = pressScale * selectionScale
            }
            .then(
                if (borderBrush != null) {
                    Modifier.border(
                        width = AppConstants.Dimensions.CARD_TYPE_OPTION_BORDER_WIDTH_SELECTED,
                        brush = borderBrush,
                        shape = RoundedCornerShape(AppConstants.Dimensions.CORNER_RADIUS_NORMAL),
                    )
                } else {
                    Modifier.border(
                        width = AppConstants.Dimensions.CARD_TYPE_OPTION_BORDER_WIDTH,
                        color = borderColor,
                        shape = RoundedCornerShape(AppConstants.Dimensions.CORNER_RADIUS_NORMAL),
                    )
                },
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                )
            }
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
    val startColor = Color(gradient.startColor.toColorInt())
    val endColor = Color(gradient.endColor.toColorInt())
    
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