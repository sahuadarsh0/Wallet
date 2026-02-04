package com.technitedminds.wallet.presentation.components.sharing

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.technitedminds.wallet.presentation.constants.AppConstants
import com.technitedminds.wallet.domain.model.Card

/**
 * Dialog for configuring card sharing options
 */
@Composable
fun CardSharingDialog(
    card: Card,
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onShare: (CardSharingOption, CardSharingConfig) -> Unit,
    initialOption: CardSharingOption? = null,
    modifier: Modifier = Modifier
) {
    if (!isVisible) return
    
    var selectedOption by remember(isVisible, initialOption) { 
        mutableStateOf(initialOption ?: CardSharingOption.FrontOnly) 
    }
    var includeSensitiveInfo by remember { mutableStateOf(false) }
    var imageQuality by remember { mutableFloatStateOf(0.8f) }
    var addWatermark by remember { mutableStateOf(true) }
    var watermarkText by remember { mutableStateOf("CardVault") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text("Share ${card.name}")
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Sharing option selection
            Text(
                text = AppConstants.UIText.WHAT_TO_SHARE_LABEL,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
                
                Column(
                    modifier = Modifier.selectableGroup(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SharingOptionItem(
                        option = CardSharingOption.FrontOnly,
                        selectedOption = selectedOption,
                        onOptionSelected = { selectedOption = it },
                        icon = Icons.Default.CreditCard,
                        title = "Front Only",
                        description = "Share only the front side"
                    )
                    
                    if (card.backImagePath.isNotBlank() || card.type.supportsOCR()) {
                        SharingOptionItem(
                            option = CardSharingOption.BackOnly,
                            selectedOption = selectedOption,
                            onOptionSelected = { selectedOption = it },
                            icon = Icons.Default.Security,
                            title = "Back Only",
                            description = "Share only the back side"
                        )
                        
                        SharingOptionItem(
                            option = CardSharingOption.BothSides,
                            selectedOption = selectedOption,
                            onOptionSelected = { selectedOption = it },
                            icon = Icons.Default.ViewCarousel,
                            title = "Both Sides",
                            description = "Share front and back"
                        )
                    }
                }
                
                HorizontalDivider()
                
                // Sharing configuration
            Text(
                text = AppConstants.UIText.SHARING_OPTIONS_LABEL,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
                
                // Sensitive information toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(AppConstants.UIText.INCLUDE_SENSITIVE_INFO_LABEL)
                        Text(
                            text = AppConstants.UIText.INCLUDE_SENSITIVE_INFO_SUBTITLE,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = includeSensitiveInfo,
                        onCheckedChange = { includeSensitiveInfo = it }
                    )
                }

                // Image quality slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = AppConstants.UIText.IMAGE_QUALITY_LABEL,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "${(imageQuality * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Slider(
                        value = imageQuality,
                        onValueChange = { imageQuality = it },
                        valueRange = 0.3f..1.0f,
                        steps = 6
                    )
                }

                // Watermark options
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(AppConstants.UIText.ADD_WATERMARK_LABEL)
                        Text(
                            text = AppConstants.UIText.PROTECT_SHARED_IMAGES_SUBTITLE,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = addWatermark,
                        onCheckedChange = { addWatermark = it }
                    )
                }
                
                // Watermark text input
                if (addWatermark) {
                    OutlinedTextField(
                        value = watermarkText,
                        onValueChange = { watermarkText = it },
                        label = { Text(AppConstants.UIText.WATERMARK_TEXT_LABEL) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val config = CardSharingConfig(
                        includeSensitiveInfo = includeSensitiveInfo,
                        imageQuality = imageQuality,
                        maxImageWidth = 1200,
                        maxImageHeight = 800,
                        addWatermark = addWatermark,
                        watermarkText = watermarkText
                    )
                    onShare(selectedOption, config)
                    onDismiss()
                }
            ) {
                Text("Share")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        modifier = modifier
    )
}

/**
 * Individual sharing option item
 */
@Composable
private fun SharingOptionItem(
    option: CardSharingOption,
    selectedOption: CardSharingOption,
    onOptionSelected: (CardSharingOption) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .selectable(
                selected = selectedOption == option,
                onClick = { onOptionSelected(option) },
                role = Role.RadioButton
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        RadioButton(
            selected = selectedOption == option,
            onClick = null
        )
        
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}