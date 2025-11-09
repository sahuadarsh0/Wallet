package com.technitedminds.wallet.presentation.components.common

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Enhanced color picker with premium animations and predefined color palettes
 */
@Composable
fun EnhancedColorPicker(
    selectedColor: String,
    onColorSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Choose Color"
) {
    val haptic = LocalHapticFeedback.current
    var showCustomColorDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header with current color preview
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // Current color preview
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        color = try {
                            Color(android.graphics.Color.parseColor(selectedColor))
                        } catch (e: Exception) {
                            MaterialTheme.colorScheme.primary
                        }
                    )
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        shape = CircleShape
                    )
            )
        }

        // Predefined color palettes
        ColorPalette(
            title = "Card Colors",
            colors = cardColors,
            selectedColor = selectedColor,
            onColorSelected = { color ->
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onColorSelected(color)
            }
        )

        ColorPalette(
            title = "Material Colors",
            colors = materialColors,
            selectedColor = selectedColor,
            onColorSelected = { color ->
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onColorSelected(color)
            }
        )

        ColorPalette(
            title = "Gradient Colors",
            colors = gradientColors,
            selectedColor = selectedColor,
            onColorSelected = { color ->
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onColorSelected(color)
            }
        )

        // Custom color button
        PremiumButton(
            onClick = { showCustomColorDialog = true },
            text = "Custom Color",
            icon = Icons.Default.Palette,
            variant = PremiumButtonVariant.Secondary,
            modifier = Modifier.fillMaxWidth()
        )
    }

    // Custom color dialog (placeholder for now)
    if (showCustomColorDialog) {
        AlertDialog(
            onDismissRequest = { showCustomColorDialog = false },
            title = { Text("Custom Color") },
            text = { Text("Custom color picker coming soon!") },
            confirmButton = {
                TextButton(onClick = { showCustomColorDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
private fun ColorPalette(
    title: String,
    colors: List<String>,
    selectedColor: String,
    onColorSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(colors) { colorHex ->
                ColorItem(
                    colorHex = colorHex,
                    isSelected = selectedColor == colorHex,
                    onClick = { onColorSelected(colorHex) }
                )
            }
        }
    }
}

@Composable
private fun ColorItem(
    colorHex: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else if (isSelected) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "color_item_scale"
    )

    val borderWidth by animateDpAsState(
        targetValue = if (isSelected) 3.dp else 1.dp,
        animationSpec = tween(200),
        label = "color_item_border"
    )

    Box(
        modifier = modifier
            .size(48.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(
                color = try {
                    Color(android.graphics.Color.parseColor(colorHex))
                } catch (e: Exception) {
                    MaterialTheme.colorScheme.primary
                }
            )
            .border(
                width = borderWidth,
                color = if (isSelected) MaterialTheme.colorScheme.primary
                       else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = CircleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = isSelected,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut()
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// Predefined color palettes
private val cardColors = listOf(
    "#1976D2", // Blue
    "#388E3C", // Green
    "#F57C00", // Orange
    "#7B1FA2", // Purple
    "#C62828", // Red
    "#00796B", // Teal
    "#5D4037", // Brown
    "#455A64", // Blue Grey
    "#E91E63", // Pink
    "#FF5722", // Deep Orange
    "#607D8B", // Blue Grey
    "#795548"  // Brown
)

private val materialColors = listOf(
    "#F44336", // Red 500
    "#E91E63", // Pink 500
    "#9C27B0", // Purple 500
    "#673AB7", // Deep Purple 500
    "#3F51B5", // Indigo 500
    "#2196F3", // Blue 500
    "#03A9F4", // Light Blue 500
    "#00BCD4", // Cyan 500
    "#009688", // Teal 500
    "#4CAF50", // Green 500
    "#8BC34A", // Light Green 500
    "#CDDC39", // Lime 500
    "#FFEB3B", // Yellow 500
    "#FFC107", // Amber 500
    "#FF9800", // Orange 500
    "#FF5722"  // Deep Orange 500
)

private val gradientColors = listOf(
    "#FF6B6B", // Coral
    "#4ECDC4", // Turquoise
    "#45B7D1", // Sky Blue
    "#96CEB4", // Mint
    "#FFEAA7", // Peach
    "#DDA0DD", // Plum
    "#98D8C8", // Seafoam
    "#F7DC6F", // Banana
    "#BB8FCE", // Lavender
    "#85C1E9", // Light Blue
    "#F8C471", // Apricot
    "#82E0AA"  // Light Green
)