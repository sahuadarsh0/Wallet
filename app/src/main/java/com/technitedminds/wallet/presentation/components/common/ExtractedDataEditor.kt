package com.technitedminds.wallet.presentation.components.common

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.technitedminds.wallet.domain.model.Card
import com.technitedminds.wallet.domain.model.CardType

/**
 * Editor for extracted data fields with predefined suggestions
 */
@Composable
fun ExtractedDataEditor(
    card: Card,
    onUpdateExtractedData: (String, String) -> Unit,
    onAddField: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Show existing extracted data fields
        card.extractedData.forEach { (key, value) ->
            ExtractedDataField(
                key = key,
                value = value,
                onValueChange = { newValue -> onUpdateExtractedData(key, newValue) },
                cardType = card.type
            )
        }
        
        // Add new field suggestions
        if (card.type.supportsOCR()) {
            val missingFields = getCommonOCRFields(card.type) - card.extractedData.keys
            if (missingFields.isNotEmpty()) {
                Text(
                    text = "Add missing fields:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    missingFields.take(3).forEach { fieldKey ->
                        AssistChip(
                            onClick = { onAddField(fieldKey) },
                            label = { Text(formatFieldName(fieldKey)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Individual extracted data field editor
 */
@Composable
private fun ExtractedDataField(
    key: String,
    value: String,
    onValueChange: (String) -> Unit,
    cardType: CardType,
    modifier: Modifier = Modifier
) {
    val isSensitive = key in listOf("cardNumber", "cvv", "pin", "password")
    var showSensitive by remember { mutableStateOf(false) }
    
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(formatFieldName(key)) },
        leadingIcon = {
            Icon(
                imageVector = getFieldIcon(key),
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
        },
        trailingIcon = if (isSensitive) {
            {
                IconButton(onClick = { showSensitive = !showSensitive }) {
                    Icon(
                        imageVector = if (showSensitive) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (showSensitive) "Hide" else "Show"
                    )
                }
            }
        } else null,
        visualTransformation = if (isSensitive && !showSensitive) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = when (key) {
                "cardNumber", "cvv", "pin" -> KeyboardType.Number
                "expiryDate" -> KeyboardType.Number
                else -> KeyboardType.Text
            }
        ),
        modifier = modifier.fillMaxWidth()
    )
}

/**
 * Get common OCR fields for card type
 */
private fun getCommonOCRFields(cardType: CardType): Set<String> {
    return when (cardType) {
        is CardType.Credit, is CardType.Debit -> setOf(
            "cardNumber",
            "expiryDate", 
            "cardholderName",
            "cvv",
            "bankName"
        )
        else -> emptySet()
    }
}

/**
 * Get icon for field type
 */
private fun getFieldIcon(key: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (key) {
        "cardNumber" -> Icons.Default.CreditCard
        "expiryDate" -> Icons.Default.DateRange
        "cardholderName" -> Icons.Default.Person
        "cvv" -> Icons.Default.Security
        "bankName" -> Icons.Default.AccountBalance
        "pin" -> Icons.Default.Lock
        else -> Icons.Default.Info
    }
}

/**
 * Format field name for display
 */
private fun formatFieldName(fieldName: String): String {
    return when (fieldName) {
        "cardNumber" -> "Card Number"
        "expiryDate" -> "Expiry Date"
        "cardholderName" -> "Cardholder Name"
        "cvv" -> "CVV"
        "bankName" -> "Bank Name"
        "pin" -> "PIN"
        else -> fieldName
            .replace(Regex("([a-z])([A-Z])"), "$1 $2")
            .split(" ")
            .joinToString(" ") { word ->
                word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            }
    }
}