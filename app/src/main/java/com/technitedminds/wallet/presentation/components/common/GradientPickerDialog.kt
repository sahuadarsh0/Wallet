package com.technitedminds.wallet.presentation.components.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Train
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.technitedminds.wallet.domain.model.CardGradient
import com.technitedminds.wallet.domain.model.CardType
import com.technitedminds.wallet.domain.model.GradientDirection
import com.technitedminds.wallet.presentation.constants.AppConstants

/**
 * Gradient picker dialog for customizing card gradients
 */
@Composable
fun GradientPickerDialog(
    isVisible: Boolean,
    cardType: CardType,
    selectedGradient: CardGradient?,
    onGradientSelected: (CardGradient) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isVisible) return
    
    var startColor by remember { 
        mutableStateOf(selectedGradient?.startColor ?: cardType.getDefaultGradient().first) 
    }
    var endColor by remember { 
        mutableStateOf(selectedGradient?.endColor ?: cardType.getDefaultGradient().second) 
    }
    var direction by remember { 
        mutableStateOf(selectedGradient?.direction ?: GradientDirection.TopToBottom) 
    }
    var gradientName by remember {
        mutableStateOf(selectedGradient?.name ?: AppConstants.UIText.PRESET_GRADIENT)
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header
                Text(
                    text = AppConstants.UIText.CUSTOMIZE_CARD_GRADIENT,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                // Gradient name input
                OutlinedTextField(
                    value = gradientName,
                    onValueChange = { gradientName = it },
                    label = { Text(AppConstants.UIText.GRADIENT_NAME) },
                    placeholder = { Text(AppConstants.UIText.GRADIENT_NAME_PLACEHOLDER) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                // Color pickers
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ColorPickerRow(
                        label = AppConstants.UIText.START_COLOR,
                        selectedColor = startColor,
                        onColorSelected = { startColor = it }
                    )
                    
                    ColorPickerRow(
                        label = AppConstants.UIText.END_COLOR,
                        selectedColor = endColor,
                        onColorSelected = { endColor = it }
                    )
                }
                
                // Direction selector
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = AppConstants.UIText.GRADIENT_DIRECTION,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(GradientDirection.values()) { dir ->
                            DirectionChip(
                                direction = dir,
                                isSelected = direction == dir,
                                onClick = { direction = dir }
                            )
                        }
                    }
                }
                
                // Preview
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = AppConstants.UIText.PREVIEW_TEXT,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    
                    GradientPreviewCard(
                        startColor = startColor,
                        endColor = endColor,
                        direction = direction,
                        cardType = cardType,
                        gradientName = gradientName
                    )
                }
                
                // Preset gradients
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = AppConstants.UIText.PRESET_GRADIENTS,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(getPresetGradients()) { preset ->
                            PresetGradientChip(
                                gradient = preset,
                                isSelected = startColor == preset.startColor && 
                                           endColor == preset.endColor &&
                                           direction == preset.direction,
                                onClick = {
                                    startColor = preset.startColor
                                    endColor = preset.endColor
                                    direction = preset.direction
                                    gradientName = preset.name ?: AppConstants.UIText.PRESET_GRADIENT
                                }
                            )
                        }
                    }
                }
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(AppConstants.DialogText.CANCEL_BUTTON)
                    }
                    
                    Button(
                        onClick = {
                            onGradientSelected(
                                CardGradient(
                                    startColor = startColor,
                                    endColor = endColor,
                                    direction = direction,
                                    name = gradientName.takeIf { it.isNotBlank() }
                                )
                            )
                        }
                    ) {
                        Text(AppConstants.UIText.APPLY)
                    }
                }
            }
        }
    }
}

/**
 * Color picker row component
 */
@Composable
private fun ColorPickerRow(
    label: String,
    selectedColor: String,
    onColorSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(getColorPalette()) { color ->
                ColorChip(
                    color = color,
                    isSelected = selectedColor == color,
                    onClick = { onColorSelected(color) }
                )
            }
        }
    }
}

/**
 * Individual color chip
 */
@Composable
private fun ColorChip(
    color: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = tween(200),
        label = "border_color"
    )
    
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Color(android.graphics.Color.parseColor(color)))
            .border(
                width = if (isSelected) 3.dp else 0.dp,
                color = borderColor,
                shape = CircleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Direction selection chip
 */
@Composable
private fun DirectionChip(
    direction: GradientDirection,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { 
            Text(
                text = when (direction) {
                    GradientDirection.TopToBottom -> "↓"
                    GradientDirection.LeftToRight -> "→"
                    GradientDirection.DiagonalTopLeftToBottomRight -> "↘"
                    GradientDirection.DiagonalTopRightToBottomLeft -> "↙"
                },
                style = MaterialTheme.typography.titleMedium
            )
        },
        modifier = modifier
    )
}

/**
 * Gradient preview card
 */
@Composable
private fun GradientPreviewCard(
    startColor: String,
    endColor: String,
    direction: GradientDirection,
    cardType: CardType,
    gradientName: String,
    modifier: Modifier = Modifier
) {
    val gradient = CardGradient(
        startColor = startColor,
        endColor = endColor,
        direction = direction,
        name = gradientName
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(createGradientBrush(gradient))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
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
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Text(
                    text = gradientName,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

/**
 * Preset gradient chip
 */
@Composable
private fun PresetGradientChip(
    gradient: CardGradient,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = tween(200),
        label = "border_color"
    )
    
    Box(
        modifier = modifier
            .size(60.dp, 40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(createGradientBrush(gradient))
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * Get color palette for picker
 */
private fun getColorPalette(): List<String> = listOf(
    "#F44336", "#E91E63", "#9C27B0", "#673AB7", "#3F51B5", "#2196F3",
    "#03A9F4", "#00BCD4", "#009688", "#4CAF50", "#8BC34A", "#CDDC39",
    "#FFEB3B", "#FFC107", "#FF9800", "#FF5722", "#795548", "#9E9E9E",
    "#607D8B", "#000000", "#FFFFFF", "#667eea", "#764ba2", "#f093fb",
    "#f5576c", "#4facfe", "#00f2fe", "#a8edea", "#fed6e3", "#ffecd2",
    "#fcb69f", "#43e97b", "#38f9d7", "#d299c2", "#fef9d7", "#89f7fe",
    "#66a6ff", "#fa709a", "#fee140"
)

/**
 * Get preset gradients
 */
private fun getPresetGradients(): List<CardGradient> = listOf(
    CardGradient("#667eea", "#764ba2", GradientDirection.TopToBottom, "Purple Blue"),
    CardGradient("#f093fb", "#f5576c", GradientDirection.TopToBottom, "Pink Red"),
    CardGradient("#4facfe", "#00f2fe", GradientDirection.TopToBottom, "Blue Cyan"),
    CardGradient("#a8edea", "#fed6e3", GradientDirection.TopToBottom, "Teal Pink"),
    CardGradient("#ffecd2", "#fcb69f", GradientDirection.TopToBottom, "Cream Peach"),
    CardGradient("#43e97b", "#38f9d7", GradientDirection.TopToBottom, "Green Teal"),
    CardGradient("#d299c2", "#fef9d7", GradientDirection.TopToBottom, "Purple Cream"),
    CardGradient("#89f7fe", "#66a6ff", GradientDirection.TopToBottom, "Light Blue"),
    CardGradient("#fa709a", "#fee140", GradientDirection.TopToBottom, "Pink Yellow"),
    CardGradient("#ff9a9e", "#fecfef", GradientDirection.LeftToRight, "Soft Pink"),
    CardGradient("#a18cd1", "#fbc2eb", GradientDirection.DiagonalTopLeftToBottomRight, "Purple Pink"),
    CardGradient("#fad0c4", "#ffd1ff", GradientDirection.DiagonalTopRightToBottomLeft, "Peach Pink")
)

/**
 * Creates a Compose Brush from CardGradient
 */
private fun createGradientBrush(gradient: CardGradient): androidx.compose.ui.graphics.Brush {
    val startColor = Color(android.graphics.Color.parseColor(gradient.startColor))
    val endColor = Color(android.graphics.Color.parseColor(gradient.endColor))
    
    return when (gradient.direction) {
        GradientDirection.TopToBottom -> androidx.compose.ui.graphics.Brush.verticalGradient(
            colors = listOf(startColor, endColor)
        )
        GradientDirection.LeftToRight -> androidx.compose.ui.graphics.Brush.horizontalGradient(
            colors = listOf(startColor, endColor)
        )
        GradientDirection.DiagonalTopLeftToBottomRight -> androidx.compose.ui.graphics.Brush.linearGradient(
            colors = listOf(startColor, endColor),
            start = androidx.compose.ui.geometry.Offset(0f, 0f),
            end = androidx.compose.ui.geometry.Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        )
        GradientDirection.DiagonalTopRightToBottomLeft -> androidx.compose.ui.graphics.Brush.linearGradient(
            colors = listOf(startColor, endColor),
            start = androidx.compose.ui.geometry.Offset(Float.POSITIVE_INFINITY, 0f),
            end = androidx.compose.ui.geometry.Offset(0f, Float.POSITIVE_INFINITY)
        )
    }
}

/**
 * Get icon for card type (reused from CardTypeSelector)
 */
private fun getCardTypeIcon(cardType: CardType): androidx.compose.ui.graphics.vector.ImageVector = when (cardType) {
    is CardType.Credit, is CardType.Debit -> Icons.Default.Payment
    is CardType.GiftCard -> Icons.Default.Redeem
    is CardType.LoyaltyCard -> Icons.Default.Star
    is CardType.MembershipCard -> Icons.Default.Person
    is CardType.InsuranceCard -> Icons.Default.Security
    is CardType.IdentificationCard -> Icons.Default.Person
    is CardType.Voucher -> Icons.Default.Redeem
    is CardType.Event -> Icons.Default.Event
    is CardType.TransportCard -> Icons.Default.Train
    is CardType.BusinessCard -> Icons.Default.Business
    is CardType.LibraryCard -> Icons.Default.Book
    is CardType.HotelCard -> Icons.Default.Hotel
    is CardType.StudentCard -> Icons.Default.School
    is CardType.AccessCard -> Icons.Default.VpnKey
    is CardType.Custom -> Icons.Default.Payment
}