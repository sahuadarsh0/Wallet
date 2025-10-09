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
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Card Color",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        
        // Predefined colors grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(6),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.height(120.dp)
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
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Custom Color")
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
            .size(40.dp)
            .clip(CircleShape)
            .border(
                width = if (isSelected) 3.dp else 0.dp,
                color = borderColor,
                shape = CircleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(if (isSelected) 32.dp else 36.dp)
                .clip(CircleShape)
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = if (isLightColor(color)) Color.Black else Color.White,
                    modifier = Modifier.size(16.dp)
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
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Custom Color",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                // Color preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(previewColor)
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline,
                            RoundedCornerShape(8.dp)
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
                    label = { Text("Hex Color Code") },
                    placeholder = { Text("FF5722") },
                    prefix = { Text("#") },
                    isError = !isValidColor && colorInput.isNotEmpty(),
                    supportingText = {
                        if (!isValidColor && colorInput.isNotEmpty()) {
                            Text("Invalid hex color code")
                        } else {
                            Text("Enter 6-digit hex code (e.g., FF5722)")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
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
                        Text("Apply")
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