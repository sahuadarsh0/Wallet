package com.technitedminds.wallet.presentation.components.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditScore
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.technitedminds.wallet.domain.model.Card
import com.technitedminds.wallet.domain.model.CardType
import com.technitedminds.wallet.presentation.constants.AppConstants

/**
 * Editor for custom fields with predefined suggestions
 */
@Composable
fun CustomFieldsEditor(
    card: Card,
    onUpdateCustomField: (String, String) -> Unit,
    onRemoveCustomField: (String) -> Unit,
    onAddCustomField: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var newFieldName by remember { mutableStateOf("") }
    var showAddField by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Show existing custom fields (excluding internal fields)
        card.customFields.forEach { (key, value) ->
            if (key !in listOf("customColor")) {
                CustomFieldRow(
                    key = key,
                    value = value,
                    onValueChange = { newValue -> onUpdateCustomField(key, newValue) },
                    onRemove = { onRemoveCustomField(key) },
                    cardType = card.type
                )
            }
        }
        
        // Predefined field suggestions
        val suggestedFields = getSuggestedCustomFields(card.type) - card.customFields.keys
        if (suggestedFields.isNotEmpty()) {
            Text(
                text = "Quick add:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            androidx.compose.foundation.lazy.LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(suggestedFields.toList()) { fieldKey ->
                    SuggestionChip(
                        onClick = { onAddCustomField(fieldKey) },
                        label = { Text(formatCustomFieldName(fieldKey)) },
                        icon = {
                            Icon(
                                imageVector = getCustomFieldIcon(fieldKey),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }
        }
        
        // Add custom field section
        if (showAddField) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newFieldName,
                    onValueChange = { newFieldName = it },
                    label = { Text(AppConstants.UIText.FIELD_NAME_LABEL) },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                
                IconButton(
                    onClick = {
                        if (newFieldName.isNotBlank()) {
                            onAddCustomField(newFieldName.trim())
                            newFieldName = ""
                            showAddField = false
                        }
                    }
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Add")
                }
                
                IconButton(
                    onClick = {
                        newFieldName = ""
                        showAddField = false
                    }
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Cancel")
                }
            }
        } else {
            OutlinedButton(
                onClick = { showAddField = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Custom Field")
            }
        }
    }
}

/**
 * Individual custom field row
 */
@Composable
private fun CustomFieldRow(
    key: String,
    value: String,
    onValueChange: (String) -> Unit,
    onRemove: () -> Unit,
    cardType: CardType,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(formatCustomFieldName(key)) },
            leadingIcon = {
                Icon(
                    imageVector = getCustomFieldIcon(key),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = when (key) {
                    "phone", "phoneNumber", "contactNumber" -> KeyboardType.Phone
                    "email", "emailAddress" -> KeyboardType.Email
                    "website", "url" -> KeyboardType.Uri
                    "amount", "balance", "limit" -> KeyboardType.Decimal
                    else -> KeyboardType.Text
                }
            ),
            modifier = Modifier.weight(1f)
        )
        
        IconButton(onClick = onRemove) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Remove field",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

/**
 * Get suggested custom fields based on card type
 */
private fun getSuggestedCustomFields(cardType: CardType): Set<String> {
    val common = setOf("notes", "website", "phone", "email")
    
    return when (cardType) {
        is CardType.Credit, is CardType.Debit -> common + setOf(
            "creditLimit", "interestRate", "annualFee", "rewardsProgram"
        )
        is CardType.GiftCard -> common + setOf(
            "balance", "expiryDate", "purchaseDate", "giftFrom"
        )
        is CardType.LoyaltyCard -> common + setOf(
            "memberNumber", "points", "tier", "joinDate"
        )
        is CardType.MembershipCard -> common + setOf(
            "memberNumber", "memberSince", "renewalDate", "benefits"
        )
        is CardType.InsuranceCard -> common + setOf(
            "policyNumber", "groupNumber", "effectiveDate", "provider"
        )
        is CardType.IdentificationCard -> common + setOf(
            "idNumber", "issueDate", "expiryDate", "issuingAuthority"
        )
        else -> common
    }
}

/**
 * Get icon for custom field
 */
private fun getCustomFieldIcon(key: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (key.lowercase()) {
        "notes", "note", "description" -> Icons.AutoMirrored.Filled.Notes
        "website", "url", "link" -> Icons.Default.Language
        "phone", "phonenumber", "contactnumber" -> Icons.Default.Phone
        "email", "emailaddress" -> Icons.Default.Email
        "balance", "amount" -> Icons.Default.AccountBalance
        "points", "rewards" -> Icons.Default.Star
        "date", "expirydate", "renewaldate" -> Icons.Default.DateRange
        "number", "membernumber", "idnumber" -> Icons.Default.Numbers
        "limit", "creditlimit" -> Icons.Default.CreditScore
        "fee", "annualfee" -> Icons.Default.AttachMoney
        "rate", "interestrate" -> Icons.Default.Percent
        else -> Icons.Default.Info
    }
}

/**
 * Format custom field name for display
 */
private fun formatCustomFieldName(fieldName: String): String {
    return fieldName
        .replace(Regex("([a-z])([A-Z])"), "$1 $2")
        .split(" ")
        .joinToString(" ") { word ->
            word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
}

