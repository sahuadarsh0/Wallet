package com.technitedminds.wallet.presentation.screens.carddetail

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.technitedminds.wallet.domain.model.Card
import com.technitedminds.wallet.presentation.components.animation.FlippableCard
import com.technitedminds.wallet.presentation.components.common.*
import com.technitedminds.wallet.presentation.utils.resolveCategoryName
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes

/**
 * Card detail screen with full-screen card display, editing, and management options.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardDetailScreen(
    cardId: String,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: CardDetailViewModel = hiltViewModel()
) {
    val card by viewModel.card.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isEditing by viewModel.isEditing.collectAsStateWithLifecycle()
    val editedCard by viewModel.editedCard.collectAsStateWithLifecycle()


    // Handle events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CardDetailEvent.CardDeleted -> onNavigateBack()
                is CardDetailEvent.CardSaved -> {
                    // Show success message or handle as needed
                }
                is CardDetailEvent.ShareCard -> {
                    // Handle sharing - you can implement this based on your needs
                    // For now, we'll just show a snackbar
                }
            }
        }
    }

    // Show loading if card is null
    if (card == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            LoadingIndicatorWithText("Loading card...")
        }
        return
    }

    Scaffold(
        topBar = {
            CardDetailTopBar(
                card = card!!,
                isEditing = isEditing,
                onNavigateBack = onNavigateBack,
                onStartEdit = viewModel::startEditing,
                onSaveEdit = viewModel::saveCard,
                onCancelEdit = viewModel::cancelEditing,
                onDeleteCard = viewModel::showDeleteConfirmation,
                onShareCard = viewModel::shareCard
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Card display
            CardDisplaySection(
                card = if (isEditing) editedCard ?: card!! else card!!,
                isFlipped = uiState.isCardFlipped,
                onFlip = viewModel::toggleCardFlip,
                modifier = Modifier.padding(16.dp)
            )

            // Card information
            if (isEditing) {
                EditCardSection(
                    card = editedCard ?: card!!,
                    onUpdateName = viewModel::updateCardName,
                    onUpdateCustomField = viewModel::updateCustomField,
                    onUpdateColor = viewModel::updateCardColor,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                CardInfoSection(
                    card = card!!,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }

    // Delete confirmation dialog
    CardDeleteConfirmationDialog(
        isVisible = uiState.showDeleteDialog,
        cardName = card!!.name,
        onConfirm = {
            viewModel.hideDeleteConfirmation()
            viewModel.deleteCard()
        },
        onDismiss = viewModel::hideDeleteConfirmation
    )

    // Error handling
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // Show error snackbar or handle as needed
            viewModel.clearError()
        }
    }

    // Loading overlay
    LoadingOverlay(
        isVisible = uiState.isLoading,
        text = "Processing..."
    )
}

/**
 * Top app bar for card detail screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CardDetailTopBar(
    card: Card,
    isEditing: Boolean,
    onNavigateBack: () -> Unit,
    onStartEdit: () -> Unit,
    onSaveEdit: () -> Unit,
    onCancelEdit: () -> Unit,
    onDeleteCard: () -> Unit,
    onShareCard: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Text(
                text = if (isEditing) "Edit Card" else card.name,
                maxLines = 1
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        actions = {
            if (isEditing) {
                // Cancel edit
                IconButton(onClick = onCancelEdit) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancel"
                    )
                }
                // Save edit
                IconButton(onClick = onSaveEdit) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Save"
                    )
                }
            } else {
                // Share
                IconButton(onClick = onShareCard) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share"
                    )
                }
                // Edit
                IconButton(onClick = onStartEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit"
                    )
                }
                // Delete
                IconButton(onClick = onDeleteCard) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete"
                    )
                }
            }
        },
        modifier = modifier
    )
}

/**
 * Card display section with flip functionality
 */
@Composable
private fun CardDisplaySection(
    card: Card,
    isFlipped: Boolean,
    onFlip: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Card with flip animation
        FlippableCard(
            card = card,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            showShareButtons = true,
            onShare = { sharingOption ->
                // Handle card sharing
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Flip instruction
        Text(
            text = "Tap card to flip",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Card information display section
 */
@Composable
private fun CardInfoSection(
    card: Card,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Basic information
        InfoCard(
            title = "Card Information",
            icon = Icons.Default.Info
        ) {
            InfoRow("Name", card.name)
            InfoRow("Type", card.type.getDisplayName())
            InfoRow("Category", resolveCategoryName(card.categoryId))
            InfoRow("Created", formatDate(card.createdAt))
            InfoRow("Updated", formatDate(card.updatedAt))
        }

        // Extracted data (OCR results)
        if (card.extractedData.isNotEmpty()) {
            InfoCard(
                title = "Extracted Information",
                icon = Icons.Default.Scanner
            ) {
                card.extractedData.forEach { (key, value) ->
                    InfoRow(
                        label = formatFieldName(key),
                        value = if (key in listOf("cardNumber", "cvv")) {
                            maskSensitiveData(value)
                        } else {
                            value
                        }
                    )
                }
            }
        }

        // Custom fields
        if (card.customFields.isNotEmpty()) {
            InfoCard(
                title = "Additional Information",
                icon = Icons.AutoMirrored.Default.Notes
            ) {
                card.customFields.forEach { (key, value) ->
                    if (key != "customColor") { // Don't show internal fields
                        InfoRow(
                            label = formatFieldName(key),
                            value = if (key in listOf("pin", "password")) {
                                maskSensitiveData(value)
                            } else {
                                value
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Edit card section
 */
@Composable
private fun EditCardSection(
    card: Card,
    onUpdateName: (String) -> Unit,
    onUpdateCustomField: (String, String) -> Unit,
    onUpdateColor: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Basic information editing
        InfoCard(
            title = "Edit Card Information",
            icon = Icons.Default.Edit
        ) {
            // Card name
            OutlinedTextField(
                value = card.name,
                onValueChange = onUpdateName,
                label = { Text("Card Name") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Color picker
        InfoCard(
            title = "Card Color",
            icon = Icons.Default.Palette
        ) {
            ColorPicker(
                selectedColor = card.getDisplayColor(),
                onColorSelected = onUpdateColor,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Custom fields editing
        InfoCard(
            title = "Additional Information",
            icon = Icons.AutoMirrored.Default.Notes
        ) {
            // Show existing custom fields for editing
            card.customFields.forEach { (key, value) ->
                if (key != "customColor") {
                    OutlinedTextField(
                        value = value,
                        onValueChange = { onUpdateCustomField(key, it) },
                        label = { Text(formatFieldName(key)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    )
                }
            }
            
            // Add new field button
            OutlinedButton(
                onClick = { 
                    // For now, add a generic "notes" field
                    onUpdateCustomField("notes", "")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Field")
            }
        }
    }
}

/**
 * Information card container
 */
@Composable
private fun InfoCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            // Content
            content()
        }
    }
}

/**
 * Information row component
 */
@Composable
private fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Utility functions
 */
private fun formatDate(timestamp: Long): String {
    val date = java.util.Date(timestamp)
    val formatter = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
    return formatter.format(date)
}

private fun formatFieldName(fieldName: String): String {
    return fieldName
        .replace(Regex("([a-z])([A-Z])"), "$1 $2")
        .split(" ")
        .joinToString(" ") { word ->
            word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
}

private fun maskSensitiveData(value: String): String {
    return when {
        value.length <= 4 -> "****"
        else -> "*".repeat(value.length - 4) + value.takeLast(4)
    }
}