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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.technitedminds.wallet.presentation.constants.AppConstants

/**
 * Color picker component for selecting card colors
 */
@Composable
fun ColorPicker(
    selectedColor: String,
    onColorSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    showCustomColorOption: Boolean = true
) {
    val hapticFeedback = LocalHapticFeedback.current
    var showColorDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AppConstants.Dimensions.SPACING_MEDIUM)
    ) {
        Text(
            text = AppConstants.UIText.CARD_COLOR_LABEL,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        
        // Predefined colors grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(8),
            horizontalArrangement = Arrangement.spacedBy(AppConstants.Dimensions.SPACING_SMALL),
            verticalArrangement = Arrangement.spacedBy(AppConstants.Dimensions.SPACING_SMALL),
            modifier = Modifier.height(AppConstants.Dimensions.CARD_TYPE_OPTION_HEIGHT)
        ) {
            items(predefinedColors) { colorHex ->
                ColorOption(
                    colorHex = colorHex,
                    isSelected = selectedColor == colorHex,
                    onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        onColorSelected(colorHex)
                    }
                )
            }
        }
        
        // Custom color button
        if (showCustomColorOption) {
            OutlinedButton(
                onClick = { showColorDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = null,
                    modifier = Modifier.size(AppConstants.Dimensions.EMPTY_STATE_BUTTON_ICON_SIZE)
                )
                Spacer(modifier = Modifier.width(AppConstants.Dimensions.SPACING_SMALL))
                Text(AppConstants.UIText.CUSTOM_COLOR)
            }
        }
    }
    
    // Custom color dialog
    if (showColorDialog) {
        CustomColorDialog(
            currentColor = selectedColor,
            onColorSelected = { color ->
                onColorSelected(color)
                showColorDialog = false
            },
            onDismiss = { showColorDialog = false }
        )
    }
}

/**
 * Individual color option component
 */
@Composable
private fun ColorOption(
    colorHex: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = try {
        Color(android.graphics.Color.parseColor(colorHex))
    } catch (e: Exception) {
        Color.Gray
    }
    
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = tween(200),
        label = "border_color"
    )
    
    Box(
        modifier = modifier
            .size(AppConstants.Dimensions.COLOR_PICKER_ITEM_SIZE_LARGE)
            .clip(CircleShape)
            .border(
                width = if (isSelected) AppConstants.Dimensions.COLOR_PICKER_BORDER_WIDTH else 0.dp,
                color = borderColor,
                shape = CircleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(if (isSelected) AppConstants.Dimensions.COLOR_PICKER_ITEM_SIZE_SMALL else AppConstants.Dimensions.COLOR_PICKER_ITEM_SIZE_SELECTED)
                .clip(CircleShape)
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = AppConstants.ContentDescriptions.SELECTED,
                    tint = if (isLightColor(color)) Color.Black else Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

/**
 * Custom color dialog with hex input
 */
@Composable
private fun CustomColorDialog(
    currentColor: String,
    onColorSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var colorInput by remember { mutableStateOf(currentColor.removePrefix("#")) }
    var isValidColor by remember { mutableStateOf(true) }
    
    val previewColor = try {
        Color(android.graphics.Color.parseColor("#$colorInput"))
    } catch (e: Exception) {
        Color.Gray
    }
    
    LaunchedEffect(colorInput) {
        isValidColor = try {
            android.graphics.Color.parseColor("#$colorInput")
            true
        } catch (e: Exception) {
            false
        }
    }
    
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
                    text = AppConstants.UIText.CUSTOM_COLOR,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                // Color preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(AppConstants.Dimensions.CUSTOM_COLOR_PREVIEW_HEIGHT)
                        .clip(RoundedCornerShape(AppConstants.Dimensions.CORNER_RADIUS_COMPACT))
                        .background(previewColor)
                        .border(
                            AppConstants.Dimensions.CARD_TYPE_OPTION_BORDER_WIDTH,
                            MaterialTheme.colorScheme.outline,
                            RoundedCornerShape(AppConstants.Dimensions.CORNER_RADIUS_COMPACT)
                        )
                )
                
                // Hex input
                OutlinedTextField(
                    value = colorInput,
                    onValueChange = { input ->
                        // Only allow hex characters and limit length
                        val filtered = input.filter { it.isDigit() || it.lowercaseChar() in 'a'..'f' }
                            .take(6)
                        colorInput = filtered
                    },
                    label = { Text(AppConstants.UIText.HEX_COLOR_CODE) },
                    placeholder = { Text(AppConstants.UIText.HEX_COLOR_CODE_PLACEHOLDER) },
                    prefix = { Text("#") },
                    isError = !isValidColor && colorInput.isNotEmpty(),
                    supportingText = {
                        if (!isValidColor && colorInput.isNotEmpty()) {
                            Text(AppConstants.UIText.INVALID_HEX_COLOR)
                        } else {
                            Text(AppConstants.UIText.HEX_CODE_INSTRUCTION)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
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
                            if (isValidColor && colorInput.length == 6) {
                                onColorSelected("#$colorInput")
                            }
                        },
                        enabled = isValidColor && colorInput.length == 6
                    ) {
                        Text(AppConstants.UIText.APPLY)
                    }
                }
            }
        }
    }
}

/**
 * Check if a color is light (for determining text color)
 */
private fun isLightColor(color: Color): Boolean {
    val luminance = 0.299 * color.red + 0.587 * color.green + 0.114 * color.blue
    return luminance > 0.5
}

/**
 * Predefined color palette
 */
private val predefinedColors = listOf(
    // Material Design Colors
    "#F44336", // Red
    "#E91E63", // Pink
    "#9C27B0", // Purple
    "#673AB7", // Deep Purple
    "#3F51B5", // Indigo
    "#2196F3", // Blue
    "#03A9F4", // Light Blue
    "#00BCD4", // Cyan
    "#009688", // Teal
    "#4CAF50", // Green
    "#8BC34A", // Light Green
    "#CDDC39", // Lime
    "#FFEB3B", // Yellow
    "#FFC107", // Amber
    "#FF9800", // Orange
    "#FF5722", // Deep Orange
    "#795548", // Brown
    "#9E9E9E", // Grey
    "#607D8B", // Blue Grey
    "#424242", // Dark Grey
    
    // Additional colors
    "#1976D2", // Credit Card Blue
    "#388E3C", // Debit Card Green
    "#37474F", // Business Card Dark
    "#6A1B9A", // Membership Purple
    "#D32F2F", // Emergency Red
    "#1565C0", // Professional Blue
    "#2E7D32", // Success Green
    "#F57C00", // Warning Orange
    "#5D4037", // Premium Brown
    "#455A64"  // Neutral Blue Grey
)