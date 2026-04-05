package com.technitedminds.wallet.presentation.components.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
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
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
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
import com.technitedminds.wallet.ui.theme.GlassSurface
import com.technitedminds.wallet.ui.theme.WalletSpring
import com.technitedminds.wallet.ui.theme.contrastText

// ─── Type Category Grouping ─────────────────────────────────────────────────

private data class TypeCategory(
    val label: String,
    val types: List<CardType>,
)

private val smartCards = TypeCategory(
    label = "Smart Cards",
    types = listOf(CardType.Credit, CardType.Debit),
)
private val shoppingRewards = TypeCategory(
    label = "Shopping & Rewards",
    types = listOf(CardType.GiftCard, CardType.LoyaltyCard, CardType.Voucher),
)
private val identityAccess = TypeCategory(
    label = "Identity & Access",
    types = listOf(CardType.MembershipCard, CardType.InsuranceCard, CardType.IdentificationCard, CardType.StudentCard, CardType.AccessCard),
)
private val travelLifestyle = TypeCategory(
    label = "Travel & Lifestyle",
    types = listOf(CardType.TransportCard, CardType.HotelCard, CardType.Event, CardType.BusinessCard, CardType.LibraryCard),
)

private val typeCategories = listOf(smartCards, shoppingRewards, identityAccess, travelLifestyle)

// ─── Public API ──────────────────────────────────────────────────────────────

@Composable
fun CardTypeSelector(
    onTypeSelected: (CardType) -> Unit,
    modifier: Modifier = Modifier,
    showCustomOption: Boolean = true,
    selectedType: CardType = CardType.Credit,
    showGradientPicker: Boolean = false,
    selectedGradient: CardGradient? = null,
    onGradientSelected: ((CardGradient) -> Unit)? = null,
) {
    val hapticFeedback = LocalHapticFeedback.current
    var showCustomDialog by remember { mutableStateOf(false) }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(AppConstants.Dimensions.SPACING_SMALL),
        verticalArrangement = Arrangement.spacedBy(AppConstants.Dimensions.SPACING_SMALL),
        modifier = modifier.fillMaxWidth(),
    ) {
        typeCategories.forEachIndexed { catIndex, category ->
            // Category section header (full span)
            item(
                key = "header_${category.label}",
                span = { GridItemSpan(2) },
            ) {
                CategorySectionHeader(
                    label = category.label,
                    showDivider = catIndex > 0,
                )
            }

            if (category == smartCards) {
                // Featured full-width cards for smart (OCR) types
                category.types.forEach { cardType ->
                    item(
                        key = "featured_${cardType.getSerializableName()}",
                        span = { GridItemSpan(2) },
                    ) {
                        FeaturedCardTypeOption(
                            cardType = cardType,
                            onClick = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                onTypeSelected(cardType)
                            },
                        )
                    }
                }
            } else {
                // Regular 2-column items for other categories
                items(
                    items = category.types,
                    key = { it.getSerializableName() },
                ) { cardType ->
                    CardTypeOptionWithGradient(
                        cardType = cardType,
                        isSelected = false,
                        gradient = null,
                        onClick = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            onTypeSelected(cardType)
                        },
                    )
                }
            }
        }

        // Custom type card at bottom (full span, dashed border)
        if (showCustomOption) {
            item(
                key = "custom_type",
                span = { GridItemSpan(2) },
            ) {
                Spacer(modifier = Modifier.height(4.dp))
                CustomTypeCard(onClick = { showCustomDialog = true })
            }
        }
    }

    if (showCustomDialog) {
        CustomCardTypeDialog(
            onTypeCreated = { customType ->
                onTypeSelected(customType)
                showCustomDialog = false
            },
            onDismiss = { showCustomDialog = false },
        )
    }
}

// ─── Category Section Header ─────────────────────────────────────────────────

@Composable
private fun CategorySectionHeader(
    label: String,
    showDivider: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(top = if (showDivider) 12.dp else 0.dp)) {
        if (showDivider) {
            PremiumDivider(modifier = Modifier.padding(bottom = 10.dp))
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ─── Featured Card Type (Credit/Debit -- full-width hero) ────────────────────

@Composable
private fun FeaturedCardTypeOption(
    cardType: CardType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isPressed by remember { mutableStateOf(false) }
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = WalletSpring.bouncy(),
        label = "featured_press",
    )

    val typeColor = remember(cardType) {
        Color(android.graphics.Color.parseColor(cardType.getDefaultColor()))
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(96.dp)
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
            .clickable { onClick() },
        shape = RoundedCornerShape(AppConstants.Dimensions.CORNER_RADIUS_NORMAL),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            typeColor.copy(alpha = 0.15f),
                            typeColor.copy(alpha = 0.06f),
                        ),
                    ),
                )
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            typeColor.copy(alpha = 0.4f),
                            typeColor.copy(alpha = 0.15f),
                        ),
                    ),
                    shape = RoundedCornerShape(AppConstants.Dimensions.CORNER_RADIUS_NORMAL),
                ),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                // Gradient icon container (larger than regular)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(typeColor, typeColor.copy(alpha = 0.7f)),
                            ),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = getCardTypeIcon(cardType),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(26.dp),
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = cardType.getDisplayName(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = getCardTypeDescription(cardType),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }

                // AI extraction badge
                GlassSurface(
                    shape = RoundedCornerShape(Glass.PillCornerRadius),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(12.dp),
                        )
                        Text(
                            text = "AI Extract",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }
        }
    }
}

// ─── Custom Type Card (dashed border) ────────────────────────────────────────

@Composable
private fun CustomTypeCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isPressed by remember { mutableStateOf(false) }
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = WalletSpring.bouncy(),
        label = "custom_press",
    )

    val outlineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(AppConstants.Dimensions.CARD_TYPE_OPTION_HEIGHT)
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .clip(RoundedCornerShape(AppConstants.Dimensions.CORNER_RADIUS_NORMAL))
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                )
            }
            .clickable { onClick() }
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center,
    ) {
        // Dashed border drawn on canvas
        val dashColor = outlineColor
        androidx.compose.foundation.Canvas(
            modifier = Modifier.fillMaxSize(),
        ) {
            val cornerRadius = AppConstants.Dimensions.CORNER_RADIUS_NORMAL.toPx()
            drawRoundRect(
                color = dashColor,
                style = Stroke(
                    width = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f),
                ),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius),
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp),
            )
            Text(
                text = AppConstants.UIText.CUSTOM_CARD_TYPE,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

// ─── Regular Card Type Option (existing, with spring press) ──────────────────

@Composable
private fun CardTypeOptionWithGradient(
    cardType: CardType,
    isSelected: Boolean,
    gradient: CardGradient?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isPressed by remember { mutableStateOf(false) }
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = WalletSpring.bouncy(),
        label = "type_press_scale",
    )

    val borderBrush = if (isSelected) Glass.colors.border else null
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
        animationSpec = WalletSpring.snappy(),
        label = "border_color",
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        animationSpec = WalletSpring.snappy(),
        label = "background_color",
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(AppConstants.Dimensions.CARD_TYPE_OPTION_HEIGHT)
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
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
        shape = RoundedCornerShape(AppConstants.Dimensions.CORNER_RADIUS_NORMAL),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(AppConstants.Dimensions.PADDING_MEDIUM),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppConstants.Dimensions.SPACING_MEDIUM),
        ) {
            Box(
                modifier = Modifier
                    .size(AppConstants.Dimensions.CARD_TYPE_OPTION_ICON_CONTAINER_SIZE)
                    .clip(RoundedCornerShape(AppConstants.Dimensions.CORNER_RADIUS_COMPACT))
                    .then(
                        if (gradient != null) {
                            Modifier.background(createGradientBrush(gradient))
                        } else {
                            Modifier.background(
                                Color(android.graphics.Color.parseColor(cardType.getDefaultColor())),
                            )
                        },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = getCardTypeIcon(cardType),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(AppConstants.Dimensions.DROPDOWN_ICON_SIZE),
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(AppConstants.Dimensions.CARD_ELEVATION_DEFAULT),
            ) {
                Text(
                    text = cardType.getDisplayName(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = getCardTypeDescription(cardType),
                    style = MaterialTheme.typography.bodySmall,
                    color = (if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface).copy(alpha = 0.7f),
                    maxLines = 1,
                )
            }
        }
    }
}

// ─── Custom Card Type Dialog (unchanged) ─────────────────────────────────────

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
        runCatching { Color(selectedColor.toColorInt()) }.getOrElse { Color(0xFF6366F1) }
    }
    val textOnColor = remember(parsedColor) { parsedColor.contrastText() }

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
                        Icon(Icons.Default.Close, "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(androidx.compose.foundation.rememberScrollState())
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
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
                                    Icons.Default.CreditCard, null,
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
                                        "Custom card type",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = textOnColor.copy(alpha = 0.7f),
                                    )
                                }
                            }
                        }
                    }

                    PremiumTextField(
                        value = typeName,
                        onValueChange = { if (it.length <= nameMaxLength) typeName = it },
                        label = AppConstants.UIText.CARD_TYPE_NAME,
                        leadingIcon = Icons.AutoMirrored.Filled.Label,
                        trailingIcon = {
                            Text(
                                "${typeName.length}/$nameMaxLength",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (typeName.length > nameMaxLength) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        },
                        isError = typeName.length > nameMaxLength,
                        errorMessage = if (typeName.length > nameMaxLength) "Name is too long" else null,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    ColorPicker(
                        selectedColor = selectedColor,
                        onColorSelected = { selectedColor = it },
                        showCustomColorOption = true,
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

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
                            if (isValid) onTypeCreated(CardType.Custom(typeName.trim(), selectedColor))
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

// ─── Helpers ─────────────────────────────────────────────────────────────────

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

private fun createGradientBrush(gradient: CardGradient): Brush {
    val startColor = Color(gradient.startColor.toColorInt())
    val endColor = Color(gradient.endColor.toColorInt())
    return when (gradient.direction) {
        com.technitedminds.wallet.domain.model.GradientDirection.TopToBottom -> Brush.verticalGradient(listOf(startColor, endColor))
        com.technitedminds.wallet.domain.model.GradientDirection.LeftToRight -> Brush.horizontalGradient(listOf(startColor, endColor))
        com.technitedminds.wallet.domain.model.GradientDirection.DiagonalTopLeftToBottomRight -> Brush.linearGradient(
            colors = listOf(startColor, endColor),
            start = androidx.compose.ui.geometry.Offset(0f, 0f),
            end = androidx.compose.ui.geometry.Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
        )
        com.technitedminds.wallet.domain.model.GradientDirection.DiagonalTopRightToBottomLeft -> Brush.linearGradient(
            colors = listOf(startColor, endColor),
            start = androidx.compose.ui.geometry.Offset(Float.POSITIVE_INFINITY, 0f),
            end = androidx.compose.ui.geometry.Offset(0f, Float.POSITIVE_INFINITY),
        )
    }
}
