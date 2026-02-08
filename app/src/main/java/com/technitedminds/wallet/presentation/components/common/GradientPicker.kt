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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.technitedminds.wallet.domain.model.CardGradient
import com.technitedminds.wallet.domain.model.GradientDirection
import com.technitedminds.wallet.presentation.constants.AppConstants

/**
 * Gradient picker dialog for selecting card gradients
 */
//@Composable
//fun GradientPickerDialog(
//    isVisible: Boolean,
//    cardType: CardType,
//    selectedGradient: CardGradient?,
//    onGradientSelected: (CardGradient) -> Unit,
//    onDismiss: () -> Unit,
//    modifier: Modifier = Modifier
//) {
//    if (isVisible) {
//        Dialog(onDismissRequest = onDismiss) {
//            Card(
//                modifier = modifier
//                    .fillMaxWidth()
//                    .fillMaxHeight(0.8f)
//                    .padding(16.dp),
//                shape = RoundedCornerShape(16.dp)
//            ) {
//                Column(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .padding(24.dp)
//                ) {
//                    // Header
//                    Row(
//                        modifier = Modifier.fillMaxWidth(),
//                        horizontalArrangement = Arrangement.SpaceBetween,
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Text(
//                            text = "Choose Gradient",
//                            style = MaterialTheme.typography.titleLarge,
//                            fontWeight = FontWeight.Bold
//                        )
//
//                        IconButton(onClick = onDismiss) {
//                            Icon(
//                                imageVector = Icons.Default.Close,
//                                contentDescription = "Close"
//                            )
//                        }
//                    }
//
//                    Spacer(modifier = Modifier.height(16.dp))
//
//                    // Gradient options
//                    LazyColumn(
//                        verticalArrangement = Arrangement.spacedBy(16.dp)
//                    ) {
//                        // Card type specific gradients
//                        item {
//                            GradientSection(
//                                title = "${cardType.getDisplayName()} Gradients",
//                                gradients = CardGradient.getGradientsForCardType(cardType),
//                                selectedGradient = selectedGradient,
//                                onGradientSelected = onGradientSelected
//                            )
//                        }
//
//                        // All gradients
//                        item {
//                            GradientSection(
//                                title = "All Gradients",
//                                gradients = CardGradient.getAllPredefinedGradients(),
//                                selectedGradient = selectedGradient,
//                                onGradientSelected = onGradientSelected
//                            )
//                        }
//
//                        // Custom gradient option
//                        item {
//                            CustomGradientSection(
//                                selectedGradient = selectedGradient,
//                                onGradientSelected = onGradientSelected
//                            )
//                        }
//                    }
//                }
//            }
//        }
//    }
//}

/**
 * Section of gradients with title
 */
@Composable
private fun GradientSection(
    title: String,
    gradients: List<CardGradient>,
    selectedGradient: CardGradient?,
    onGradientSelected: (CardGradient) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        
        @OptIn(ExperimentalLayoutApi::class)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            maxItemsInEachRow = 3,
            modifier = Modifier.fillMaxWidth(),
        ) {
            gradients.forEach { gradient ->
                GradientOption(
                    gradient = gradient,
                    isSelected = selectedGradient == gradient,
                    onClick = { onGradientSelected(gradient) },
                )
            }
        }
    }
}

/**
 * Individual gradient option
 */
@Composable
private fun GradientOption(
    gradient: CardGradient,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = tween(200),
        label = "gradient_border"
    )
    
    Column(
        modifier = modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Gradient preview
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(
                    width = if (isSelected) 2.dp else 0.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(8.dp)
                )
                .background(
                    brush = createGradientBrush(gradient)
                )
        )
        
        // Gradient name
        gradient.name?.let { name ->
            Text(
                text = name,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}

/**
 * Custom gradient creation section
 */
@Composable
private fun CustomGradientSection(
    selectedGradient: CardGradient?,
    onGradientSelected: (CardGradient) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCustomDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Custom Gradient",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        
        OutlinedButton(
            onClick = { showCustomDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Create Custom Gradient")
        }
    }
    
    if (showCustomDialog) {
        CustomGradientDialog(
            onGradientCreated = { gradient ->
                onGradientSelected(gradient)
                showCustomDialog = false
            },
            onDismiss = { showCustomDialog = false }
        )
    }
}

/**
 * Custom gradient creation dialog
 */
@Composable
private fun CustomGradientDialog(
    onGradientCreated: (CardGradient) -> Unit,
    onDismiss: () -> Unit
) {
    var startColor by remember { mutableStateOf("#667eea") }
    var endColor by remember { mutableStateOf("#764ba2") }
    var direction by remember { mutableStateOf(GradientDirection.TopToBottom) }
    var gradientName by remember { mutableStateOf("") }
    
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
                    text = "Create Custom Gradient",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                // Gradient preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            brush = createGradientBrush(
                                CardGradient(startColor, endColor, direction)
                            )
                        )
                )
                
                // Gradient name
                OutlinedTextField(
                    value = gradientName,
                    onValueChange = { gradientName = it },
                    label = { Text(AppConstants.UIText.GRADIENT_NAME_OPTIONAL_LABEL) },
                    placeholder = { Text("My Custom Gradient") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                // Start color picker
                ColorPickerRow(
                    label = "Start Color",
                    selectedColor = startColor,
                    onColorSelected = { startColor = it }
                )
                
                // End color picker
                ColorPickerRow(
                    label = "End Color",
                    selectedColor = endColor,
                    onColorSelected = { endColor = it }
                )
                
                // Direction picker
                GradientDirectionPicker(
                    selectedDirection = direction,
                    onDirectionSelected = { direction = it }
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
                            val gradient = CardGradient(
                                startColor = startColor,
                                endColor = endColor,
                                direction = direction,
                                name = gradientName.takeIf { it.isNotBlank() }
                            )
                            onGradientCreated(gradient)
                        }
                    ) {
                        Text("Create")
                    }
                }
            }
        }
    }
}

/**
 * Color picker row for gradient colors
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
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Color preview
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(android.graphics.Color.parseColor(selectedColor)))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(8.dp)
                    )
            )
            
            // Color input
            OutlinedTextField(
                value = selectedColor,
                onValueChange = { color ->
                    if (isValidHexColor(color)) {
                        onColorSelected(color)
                    }
                },
                label = { Text(AppConstants.UIText.HEX_COLOR_LABEL) },
                placeholder = { Text("#667eea") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }
    }
}

/**
 * Gradient direction picker
 */
@Composable
private fun GradientDirectionPicker(
    selectedDirection: GradientDirection,
    onDirectionSelected: (GradientDirection) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Gradient Direction",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium
        )
        
        @OptIn(ExperimentalLayoutApi::class)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            maxItemsInEachRow = 2,
            modifier = Modifier.fillMaxWidth(),
        ) {
            GradientDirection.entries.forEach { direction ->
                DirectionOption(
                    direction = direction,
                    isSelected = selectedDirection == direction,
                    onClick = { onDirectionSelected(direction) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

/**
 * Individual direction option
 */
@Composable
private fun DirectionOption(
    direction: GradientDirection,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
        animationSpec = tween(200),
        label = "direction_border"
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = direction.displayName,
                style = MaterialTheme.typography.labelMedium,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}

/**
 * Creates a Compose Brush from CardGradient
 */
private fun createGradientBrush(gradient: CardGradient): Brush {
    val startColor = Color(android.graphics.Color.parseColor(gradient.startColor))
    val endColor = Color(android.graphics.Color.parseColor(gradient.endColor))
    
    return when (gradient.direction) {
        GradientDirection.TopToBottom -> Brush.verticalGradient(
            colors = listOf(startColor, endColor)
        )
        GradientDirection.LeftToRight -> Brush.horizontalGradient(
            colors = listOf(startColor, endColor)
        )
        GradientDirection.DiagonalTopLeftToBottomRight -> Brush.linearGradient(
            colors = listOf(startColor, endColor),
            start = androidx.compose.ui.geometry.Offset(0f, 0f),
            end = androidx.compose.ui.geometry.Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        )
        GradientDirection.DiagonalTopRightToBottomLeft -> Brush.linearGradient(
            colors = listOf(startColor, endColor),
            start = androidx.compose.ui.geometry.Offset(Float.POSITIVE_INFINITY, 0f),
            end = androidx.compose.ui.geometry.Offset(0f, Float.POSITIVE_INFINITY)
        )
    }
}

/**
 * Validates hex color format
 */
private fun isValidHexColor(color: String): Boolean {
    return try {
        android.graphics.Color.parseColor(color)
        true
    } catch (e: IllegalArgumentException) {
        false
    }
}