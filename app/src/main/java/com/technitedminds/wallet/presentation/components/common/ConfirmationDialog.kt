package com.technitedminds.wallet.presentation.components.common

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.technitedminds.wallet.presentation.constants.AppConstants

/**
 * Types of confirmation dialogs with different styling
 */
enum class ConfirmationType {
    DELETE,
    WARNING,
    INFO
}

/**
 * Reusable confirmation dialog for delete operations and critical actions.
 */
@Composable
fun ConfirmationDialog(
    isVisible: Boolean,
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    type: ConfirmationType = ConfirmationType.INFO,
    confirmText: String = "Confirm",
    dismissText: String = AppConstants.DialogText.CANCEL_BUTTON,
    icon: ImageVector? = null
) {
    if (isVisible) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            ConfirmationDialogContent(
                title = title,
                message = message,
                onConfirm = onConfirm,
                onDismiss = onDismiss,
                type = type,
                confirmText = confirmText,
                dismissText = dismissText,
                icon = icon,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun ConfirmationDialogContent(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    type: ConfirmationType,
    confirmText: String,
    dismissText: String,
    icon: ImageVector?,
    modifier: Modifier = Modifier
) {
    val (containerColor, contentColor, defaultIcon, confirmButtonColor) = when (type) {
        ConfirmationType.DELETE -> {
            val errorColor = MaterialTheme.colorScheme.error
            val errorContainer = MaterialTheme.colorScheme.errorContainer
            val onErrorContainer = MaterialTheme.colorScheme.onErrorContainer
            Tuple4(errorContainer, onErrorContainer, Icons.Default.Delete, errorColor)
        }
        ConfirmationType.WARNING -> {
            val warningColor = MaterialTheme.colorScheme.secondary
            val warningContainer = MaterialTheme.colorScheme.secondaryContainer
            val onWarningContainer = MaterialTheme.colorScheme.onSecondaryContainer
            Tuple4(warningContainer, onWarningContainer, Icons.Default.Warning, warningColor)
        }
        ConfirmationType.INFO -> {
            val infoColor = MaterialTheme.colorScheme.primary
            val infoContainer = MaterialTheme.colorScheme.primaryContainer
            val onInfoContainer = MaterialTheme.colorScheme.onPrimaryContainer
            Tuple4(infoContainer, onInfoContainer, Icons.Default.Info, infoColor)
        }
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon
            Icon(
                imageVector = icon ?: defaultIcon,
                contentDescription = null,
                tint = confirmButtonColor,
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Title
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Message
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Cancel button
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text(dismissText)
                }
                
                // Confirm button
                Button(
                    onClick = {
                        onConfirm()
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = confirmButtonColor,
                        contentColor = Color.White
                    )
                ) {
                    Text(confirmText)
                }
            }
        }
    }
}

/**
 * Specialized delete confirmation dialog
 */
@Composable
fun DeleteConfirmationDialog(
    isVisible: Boolean,
    itemName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    ConfirmationDialog(
        isVisible = isVisible,
        title = "Delete $itemName?",
        message = "This action cannot be undone. Are you sure you want to delete this $itemName?",
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        type = ConfirmationType.DELETE,
        confirmText = AppConstants.DialogText.DELETE_BUTTON,
        dismissText = AppConstants.DialogText.CANCEL_BUTTON,
        modifier = modifier
    )
}

/**
 * Specialized card delete confirmation dialog
 */
@Composable
fun CardDeleteConfirmationDialog(
    isVisible: Boolean,
    cardName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    ConfirmationDialog(
        isVisible = isVisible,
        title = "Delete Card",
        message = "Are you sure you want to delete \"$cardName\"? This will permanently remove the card and all its associated data.",
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        type = ConfirmationType.DELETE,
        confirmText = "Delete Card",
        dismissText = "Keep Card",
        modifier = modifier
    )
}

/**
 * Specialized category delete confirmation dialog
 */
@Composable
fun CategoryDeleteConfirmationDialog(
    isVisible: Boolean,
    categoryName: String,
    cardCount: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val message = if (cardCount > 0) {
        "Are you sure you want to delete the \"$categoryName\" category? This will affect $cardCount card(s) in this category."
    } else {
        "Are you sure you want to delete the \"$categoryName\" category?"
    }
    
    ConfirmationDialog(
        isVisible = isVisible,
        title = "Delete Category",
        message = message,
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        type = ConfirmationType.DELETE,
        confirmText = AppConstants.DialogText.DELETE_BUTTON,
        dismissText = AppConstants.DialogText.CANCEL_BUTTON,
        modifier = modifier
    )
}

/**
 * Data export confirmation dialog
 */
@Composable
fun ExportConfirmationDialog(
    isVisible: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    ConfirmationDialog(
        isVisible = isVisible,
        title = "Export Data",
        message = "This will export all your cards and categories to a file. Card images will not be included for security reasons.",
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        type = ConfirmationType.INFO,
        confirmText = "Export",
        dismissText = AppConstants.DialogText.CANCEL_BUTTON,
        modifier = modifier
    )
}

/**
 * Data import confirmation dialog
 */
@Composable
fun ImportConfirmationDialog(
    isVisible: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    ConfirmationDialog(
        isVisible = isVisible,
        title = "Import Data",
        message = "This will import cards and categories from the selected file. Existing data will not be affected.",
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        type = ConfirmationType.WARNING,
        confirmText = "Import",
        dismissText = AppConstants.DialogText.CANCEL_BUTTON,
        modifier = modifier
    )
}

// Helper data class for multiple return values
private data class Tuple4<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)