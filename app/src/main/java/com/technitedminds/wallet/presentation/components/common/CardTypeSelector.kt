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

/**
 * Card type selector component with visual icons and descriptions
 */
@Composable
fun CardTypeSelector(
    selectedType: CardType,
    onTypeSelected: (CardType) -> Unit,
    modifier: Modifier = Modifier,
    showCustomOption: Boolean = true
) {
    val hapticFeedback = LocalHapticFeedback.current
    var showCustomDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Card Type",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        
        // Predefined card types grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            items(CardType.getAllPredefinedTypes()) { cardType ->
                CardTypeOption(
                    cardType = cardType,
                    isSelected = selectedType::class == cardType::class,
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
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Custom Card Type")
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
            .height(80.dp)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Card type icon with color
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Color(android.graphics.Color.parseColor(cardType.getDefaultColor()))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getCardTypeIcon(cardType),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            // Card type name and description
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
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
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Create Custom Card Type",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                // Type name input
                OutlinedTextField(
                    value = typeName,
                    onValueChange = { typeName = it },
                    label = { Text("Card Type Name") },
                    placeholder = { Text("e.g., Gym Card, Voucher") },
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
                        text = "Preview",
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
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
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
                                    modifier = Modifier.size(16.dp)
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
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = {
                            if (typeName.isNotBlank()) {
                                onTypeCreated(CardType.Custom(typeName.trim(), selectedColor))
                            }
                        },
                        enabled = typeName.isNotBlank()
                    ) {
                        Text("Create")
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
    is CardType.ATM -> Icons.Default.CreditCard
    is CardType.GiftCard -> Icons.Default.CardGiftcard
    is CardType.LoyaltyCard -> Icons.Default.Stars
    is CardType.MembershipCard -> Icons.Default.Badge
    is CardType.InsuranceCard -> Icons.Default.HealthAndSafety
    is CardType.IdentificationCard -> Icons.Default.Badge
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
    is CardType.Credit -> "Credit and charge cards"
    is CardType.Debit -> "Debit and bank cards"
    is CardType.ATM -> "ATM and cash cards"
    is CardType.GiftCard -> "Gift cards and vouchers"
    is CardType.LoyaltyCard -> "Loyalty and rewards cards"
    is CardType.MembershipCard -> "Gym and club memberships"
    is CardType.InsuranceCard -> "Health and insurance cards"
    is CardType.IdentificationCard -> "ID cards and licenses"
    is CardType.TransportCard -> "Metro and transit cards"
    is CardType.BusinessCard -> "Business and corporate cards"
    is CardType.LibraryCard -> "Library and education cards"
    is CardType.HotelCard -> "Hotel and travel cards"
    is CardType.StudentCard -> "Student ID cards"
    is CardType.AccessCard -> "Access and security cards"
    is CardType.Custom -> "Custom card type"
}