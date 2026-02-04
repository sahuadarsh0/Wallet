package com.technitedminds.wallet.presentation.components.common

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.technitedminds.wallet.presentation.constants.AppConstants

/**
 * Card component displaying OCR completion status with extracted field count.
 * Uses primaryContainer background with Material Design 3 styling.
 *
 * @param extractedFieldCount The number of fields extracted by OCR
 * @param modifier Modifier to be applied to the card
 */
@Composable
fun OCRStatusCard(
    extractedFieldCount: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = AppConstants.Dimensions.CARD_ELEVATION_DEFAULT
        ),
        shape = RoundedCornerShape(AppConstants.Dimensions.CORNER_RADIUS_NORMAL)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppConstants.Dimensions.PADDING_LARGE),
            horizontalArrangement = Arrangement.spacedBy(AppConstants.Dimensions.SPACING_MEDIUM),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(AppConstants.Dimensions.ICON_SIZE_LARGE)
            )
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(AppConstants.Dimensions.SPACING_EXTRA_SMALL)
            ) {
                Text(
                    text = AppConstants.UIText.TEXT_RECOGNITION_COMPLETE,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Text(
                    text = "$extractedFieldCount ${if (extractedFieldCount == 1) "field" else "fields"} extracted",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/**
 * Card component displaying manual entry requirement message.
 * Uses secondaryContainer background with Material Design 3 styling.
 *
 * @param modifier Modifier to be applied to the card
 */
@Composable
fun ManualEntryCard(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = AppConstants.Dimensions.CARD_ELEVATION_DEFAULT
        ),
        shape = RoundedCornerShape(AppConstants.Dimensions.CORNER_RADIUS_NORMAL)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppConstants.Dimensions.PADDING_LARGE),
            horizontalArrangement = Arrangement.spacedBy(AppConstants.Dimensions.SPACING_MEDIUM),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(AppConstants.Dimensions.ICON_SIZE_LARGE)
            )
            
            Text(
                text = AppConstants.UIText.MANUAL_ENTRY_REQUIRED,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
