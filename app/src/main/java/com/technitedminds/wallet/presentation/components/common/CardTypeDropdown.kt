package com.technitedminds.wallet.presentation.components.common

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.technitedminds.wallet.domain.model.CardType
import com.technitedminds.wallet.presentation.constants.AppConstants

/**
 * Dropdown for selecting card type
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardTypeDropdown(
    selectedCardType: CardType,
    onCardTypeSelected: (CardType) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Card Type"
) {
    var expanded by remember { mutableStateOf(false) }
    val cardTypes = CardType.getAllPredefinedTypes()
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedCardType.getDisplayName(),
            onValueChange = { },
            readOnly = true,
            label = { Text(label) },
            leadingIcon = {
                Icon(
                    imageVector = getCardTypeIcon(selectedCardType),
                    contentDescription = null,
                    modifier = Modifier.size(AppConstants.Dimensions.DROPDOWN_ICON_SIZE)
                )
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
        )
        
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            cardTypes.forEach { cardType ->
                DropdownMenuItem(
                    text = { 
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(AppConstants.Dimensions.SPACING_SMALL),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = getCardTypeIcon(cardType),
                                contentDescription = null,
                                modifier = Modifier.size(AppConstants.Dimensions.DROPDOWN_ICON_SIZE)
                            )
                            Text(cardType.getDisplayName())
                        }
                    },
                    onClick = {
                        onCardTypeSelected(cardType)
                        expanded = false
                    }
                )
            }
        }
    }
}

/**
 * Get icon for card type
 */
private fun getCardTypeIcon(cardType: CardType): ImageVector {
    return when (cardType) {
        is CardType.Credit -> Icons.Default.CreditCard
        is CardType.Debit -> Icons.Default.Payment
        is CardType.GiftCard -> Icons.Default.Redeem
        is CardType.LoyaltyCard -> Icons.Default.Star
        is CardType.MembershipCard -> Icons.Default.Person
        is CardType.InsuranceCard -> Icons.Default.Security
        is CardType.IdentificationCard -> Icons.Default.Badge
        is CardType.Voucher -> Icons.Default.LocalOffer
        is CardType.Event -> Icons.Default.Event
        is CardType.TransportCard -> Icons.Default.Train
        is CardType.BusinessCard -> Icons.Default.Business
        is CardType.LibraryCard -> Icons.Default.Book
        is CardType.HotelCard -> Icons.Default.Hotel
        is CardType.StudentCard -> Icons.Default.School
        is CardType.AccessCard -> Icons.Default.VpnKey
        is CardType.Custom -> Icons.Default.CreditCard
    }
}