package com.technitedminds.wallet.presentation.screens.carddetail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Scanner
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.animation.core.EaseInCubic
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.technitedminds.wallet.domain.model.Card
import com.technitedminds.wallet.domain.model.CardGradient
import com.technitedminds.wallet.domain.model.GradientDirection
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import com.technitedminds.wallet.presentation.components.animation.EnhancedSlideInItem
import com.technitedminds.wallet.presentation.components.animation.FlippableCard
import com.technitedminds.wallet.presentation.components.common.AnimatedSectionHeader
import com.technitedminds.wallet.presentation.components.common.CardDeleteConfirmationDialog
import com.technitedminds.wallet.presentation.components.common.CardTypeDropdown
import com.technitedminds.wallet.presentation.components.common.CategoryDropdown
import com.technitedminds.wallet.presentation.components.common.CustomFieldsEditor
import com.technitedminds.wallet.presentation.components.common.ExtractedDataEditor
import com.technitedminds.wallet.presentation.components.common.GradientPickerDialog
import com.technitedminds.wallet.presentation.components.common.LoadingIndicatorWithText
import com.technitedminds.wallet.presentation.components.common.LoadingOverlay
import com.technitedminds.wallet.presentation.components.common.PremiumCard
import com.technitedminds.wallet.presentation.components.common.PremiumTextField
import com.technitedminds.wallet.presentation.components.common.resolveCategoryName
import com.technitedminds.wallet.presentation.components.sharing.CardSharingDialog
import com.technitedminds.wallet.presentation.components.sharing.CardSharingOption
import com.technitedminds.wallet.presentation.constants.AppConstants

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
    val showSharingDialog by viewModel.showSharingDialog.collectAsStateWithLifecycle()
    val showGradientPicker by viewModel.showGradientPicker.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    // Handle events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CardDetailEvent.CardDeleted -> onNavigateBack()
                is CardDetailEvent.CardSaved -> {
                    // Show success message or handle as needed
                }
                is CardDetailEvent.ShareSuccess -> {
                    // Card shared successfully
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
            LoadingIndicatorWithText(AppConstants.UIText.LOADING_CARD_DETAIL)
        }
        return
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CardDetailTopBar(
                card = card!!,
                isEditing = isEditing,
                onNavigateBack = onNavigateBack,
                onStartEdit = viewModel::startEditing,
                onSaveEdit = viewModel::saveCard,
                onCancelEdit = viewModel::cancelEditing,
                onDeleteCard = viewModel::showDeleteConfirmation,
                onShowSharingDialog = viewModel::showSharingDialog
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
            // Card display with staggered animation
            EnhancedSlideInItem(visible = true, index = 0) {
                CardDisplaySection(
                    card = if (isEditing) editedCard ?: card!! else card!!,
                    isFlipped = uiState.isCardFlipped,
                    onFlip = viewModel::toggleCardFlip,
                    onShare = { sharingOption ->
                        viewModel.quickShare(sharingOption)
                    },
                    onShowSharingDialog = viewModel::showSharingDialog,
                    modifier = Modifier.padding(AppConstants.Dimensions.PADDING_LARGE)
                )
            }

            // Card information with smooth fade transition between edit and view modes
            AnimatedVisibility(
                visible = isEditing,
                enter = fadeIn(
                    animationSpec = tween(
                        durationMillis = 300,
                        easing = EaseOutCubic
                    )
                ),
                exit = fadeOut(
                    animationSpec = tween(
                        durationMillis = 300,
                        easing = EaseInCubic
                    )
                )
            ) {
                EditCardSection(
                    card = editedCard ?: card!!,
                    categories = categories,
                    viewModel = viewModel,
                    onUpdateName = viewModel::updateCardName,
                    onUpdateCategory = viewModel::updateCardCategory,
                    onUpdateCardType = viewModel::updateCardType,
                    onUpdateExtractedData = viewModel::updateExtractedData,
                    onUpdateCustomField = viewModel::updateCustomField,
                    onAddCustomField = viewModel::addCustomField,
                    onRemoveCustomField = viewModel::removeCustomField,
                    onUpdateColor = viewModel::updateCardColor,
                    modifier = Modifier.padding(AppConstants.Dimensions.PADDING_LARGE)
                )
            }
            
            AnimatedVisibility(
                visible = !isEditing,
                enter = fadeIn(
                    animationSpec = tween(
                        durationMillis = 300,
                        easing = EaseOutCubic
                    )
                ),
                exit = fadeOut(
                    animationSpec = tween(
                        durationMillis = 300,
                        easing = EaseInCubic
                    )
                )
            ) {
                CardInfoSection(
                    card = card!!,
                    modifier = Modifier.padding(AppConstants.Dimensions.PADDING_LARGE)
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

    // Success feedback
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            viewModel.clearSuccessMessage()
        }
    }

    // Error feedback
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                actionLabel = "Dismiss",
                duration = SnackbarDuration.Long
            )
            viewModel.clearError()
        }
    }

    // Loading overlay
    LoadingOverlay(
        isVisible = uiState.isLoading,
        text = AppConstants.UIText.PROCESSING
    )
    
    // Sharing dialog
    CardSharingDialog(
        card = card!!,
        isVisible = showSharingDialog,
        onDismiss = viewModel::hideSharingDialog,
        onShare = { option, config ->
            viewModel.shareCardWithConfig(option, config)
        }
    )
    
    // Gradient Picker Dialog
    if (isEditing) {
        GradientPickerDialog(
            isVisible = showGradientPicker,
            cardType = (editedCard ?: card)!!.type,
            selectedGradient = (editedCard ?: card)!!.customGradient,
            onGradientSelected = { gradient ->
                viewModel.updateCardGradient(gradient)
                viewModel.hideGradientPicker()
            },
            onDismiss = viewModel::hideGradientPicker
        )
    }
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
    onShowSharingDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Text(
                text = if (isEditing) AppConstants.NavigationLabels.EDIT_CARD else card.name,
                maxLines = 1
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = AppConstants.ContentDescriptions.BACK
                )
            }
        },
        actions = {
            if (isEditing) {
                // Cancel edit
                IconButton(onClick = onCancelEdit) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = AppConstants.DialogText.CANCEL_BUTTON
                    )
                }
                // Save edit
                IconButton(onClick = onSaveEdit) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = AppConstants.ContentDescriptions.SAVE
                    )
                }
            } else {
                // Share with options
                IconButton(onClick = onShowSharingDialog) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = AppConstants.UIText.SHARE_OPTIONS
                    )
                }
                // Edit
                IconButton(onClick = onStartEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = AppConstants.ContentDescriptions.EDIT
                    )
                }
                // Delete
                IconButton(onClick = onDeleteCard) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = AppConstants.DialogText.DELETE_BUTTON
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
    onShare: (CardSharingOption) -> Unit,
    onShowSharingDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Card with flip animation - shows actual images for non-OCR cards
        // Let FlippableCard manage its own flip state for better UX
        FlippableCard(
            card = card,
            showShareButtons = true,
            onShare = onShare,
            onCardClick = null, // Let FlippableCard handle its own flipping
            onCardLongPress = onShowSharingDialog,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        )

        Spacer(modifier = Modifier.height(AppConstants.Dimensions.SPACING_LARGE))

        // Instructions
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppConstants.Dimensions.SPACING_EXTRA_SMALL)
        ) {
            Text(
                text = AppConstants.UIText.FLIP_INSTRUCTION,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = AppConstants.UIText.QUICK_SHARE_INSTRUCTION,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
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
        verticalArrangement = Arrangement.spacedBy(AppConstants.Dimensions.SPACING_LARGE)
    ) {
        // Basic information with staggered animation (delay: 100ms)
        StaggeredSlideInItem(delayMillis = 100) {
            InfoCard(
                title = AppConstants.UIText.CARD_INFORMATION,
                icon = Icons.Default.Info
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(AppConstants.Dimensions.SPACING_SMALL)
                ) {
                    InfoRow(AppConstants.UIText.NAME_LABEL, card.name)
                    InfoRow(AppConstants.UIText.TYPE_LABEL, card.type.getDisplayName())
                    InfoRow(AppConstants.UIText.CATEGORY_LABEL, resolveCategoryName(card.categoryId))
                    InfoRow(AppConstants.UIText.CREATED_LABEL, formatDate(card.createdAt))
                    InfoRow(AppConstants.UIText.UPDATED_LABEL, formatDate(card.updatedAt))
                }
            }
        }

        // Extracted data (OCR results) with staggered animation (delay: 200ms)
        if (card.extractedData.isNotEmpty()) {
            StaggeredSlideInItem(delayMillis = 200) {
                InfoCard(
                    title = AppConstants.UIText.EXTRACTED_INFORMATION,
                    icon = Icons.Default.Scanner
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(AppConstants.Dimensions.SPACING_SMALL)
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
            }
        }

        // Custom fields with staggered animation (delay: 300ms)
        if (card.customFields.isNotEmpty()) {
            StaggeredSlideInItem(delayMillis = 300) {
                InfoCard(
                    title = AppConstants.UIText.ADDITIONAL_INFORMATION,
                    icon = Icons.AutoMirrored.Default.Notes
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(AppConstants.Dimensions.SPACING_SMALL)
                    ) {
                        card.customFields.forEach { (key, value) ->
                            if (key != AppConstants.UIText.CUSTOM_COLOR_FIELD) { // Don't show internal fields
                                InfoRow(
                                    label = formatFieldName(key),
                                    value = if (key in listOf(AppConstants.UIText.PIN_FIELD, AppConstants.UIText.PASSWORD_FIELD)) {
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
    }
}

/**
 * Enhanced edit card section with comprehensive editing options
 */
@Composable
private fun EditCardSection(
    card: Card,
    categories: List<com.technitedminds.wallet.domain.model.Category>,
    viewModel: CardDetailViewModel,
    onUpdateName: (String) -> Unit,
    onUpdateCategory: (String) -> Unit,
    onUpdateCardType: (com.technitedminds.wallet.domain.model.CardType) -> Unit,
    onUpdateExtractedData: (String, String) -> Unit,
    onUpdateCustomField: (String, String) -> Unit,
    onAddCustomField: (String) -> Unit,
    onRemoveCustomField: (String) -> Unit,
    onUpdateColor: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AppConstants.Dimensions.SPACING_LARGE)
    ) {
        // Enhanced basic information editing
        EnhancedSlideInItem(visible = true, index = 0, baseDelay = 100) {
            PremiumCard(
                elevation = CardDefaults.cardElevation(defaultElevation = AppConstants.Dimensions.CARD_ELEVATION_DEFAULT)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(AppConstants.Dimensions.SPACING_LARGE)
                ) {
                    AnimatedSectionHeader(
                        title = AppConstants.UIText.BASIC_INFORMATION,
                        icon = Icons.Default.Edit,
                        subtitle = AppConstants.UIText.BASIC_INFORMATION_SUBTITLE
                    )
                    
                    // Card name with enhanced styling
                    PremiumTextField(
                        value = card.name,
                        onValueChange = onUpdateName,
                        label = AppConstants.UIText.CARD_NAME_LABEL,
                        leadingIcon = Icons.Default.CreditCard,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // Card type dropdown
                    CardTypeDropdown(
                        selectedCardType = card.type,
                        onCardTypeSelected = onUpdateCardType,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // Category dropdown
                    CategoryDropdown(
                        categories = categories,
                        selectedCategoryId = card.categoryId,
                        onCategorySelected = onUpdateCategory,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Enhanced extracted data editing (for OCR cards)
        if (card.type.supportsOCR() && (card.extractedData.isNotEmpty() || card.type.supportsOCR())) {
            EnhancedSlideInItem(visible = true, index = 1, baseDelay = 100) {
                PremiumCard(
                    elevation = CardDefaults.cardElevation(defaultElevation = AppConstants.Dimensions.CARD_ELEVATION_DEFAULT)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(AppConstants.Dimensions.SPACING_LARGE)
                    ) {
                        AnimatedSectionHeader(
                            title = AppConstants.UIText.CARD_INFORMATION,
                            icon = Icons.Default.Scanner,
                            subtitle = AppConstants.UIText.CARD_DETAILS_SUBTITLE
                        )
                        
                        ExtractedDataEditor(
                            card = card,
                            onUpdateExtractedData = onUpdateExtractedData,
                            onAddField = { fieldKey ->
                                onUpdateExtractedData(fieldKey, "")
                            }
                        )
                    }
                }
            }
        }

        // Enhanced custom fields editing
        EnhancedSlideInItem(visible = true, index = 2, baseDelay = 100) {
            PremiumCard(
                elevation = CardDefaults.cardElevation(defaultElevation = AppConstants.Dimensions.CARD_ELEVATION_DEFAULT)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(AppConstants.Dimensions.SPACING_LARGE)
                ) {
                    AnimatedSectionHeader(
                        title = AppConstants.UIText.ADDITIONAL_INFORMATION,
                        icon = Icons.AutoMirrored.Default.Notes,
                        subtitle = AppConstants.UIText.CUSTOM_FIELDS_SUBTITLE
                    )
                    
                    CustomFieldsEditor(
                        card = card,
                        onUpdateCustomField = onUpdateCustomField,
                        onRemoveCustomField = onRemoveCustomField,
                        onAddCustomField = onAddCustomField
                    )
                }
            }
        }

        // Enhanced appearance settings
        EnhancedSlideInItem(visible = true, index = 3, baseDelay = 100) {
            PremiumCard(
                elevation = CardDefaults.cardElevation(defaultElevation = AppConstants.Dimensions.CARD_ELEVATION_DEFAULT)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(AppConstants.Dimensions.SPACING_LARGE)
                ) {
                    AnimatedSectionHeader(
                        title = AppConstants.UIText.APPEARANCE_TITLE,
                        icon = Icons.Default.Palette,
                        subtitle = AppConstants.UIText.APPEARANCE_SUBTITLE
                    )
                    
                    // Gradient preview card that opens picker
                    GradientPreviewButton(
                        gradient = card.customGradient ?: Card.getDefaultGradientForType(card.type),
                        cardType = card.type,
                        onClick = viewModel::showGradientPicker
                    )
                }
            }
        }
        
        // Enhanced save reminder
        EnhancedSlideInItem(visible = true, index = 4, baseDelay = 100) {
            PremiumCard(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = AppConstants.Dimensions.CARD_ELEVATION_DEFAULT),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppConstants.Dimensions.SPACING_LARGE)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = AppConstants.AnimationValues.ALPHA_DISABLED),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = AppConstants.UIText.SAVE_CHANGES_REMINDER,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
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
        elevation = CardDefaults.cardElevation(defaultElevation = AppConstants.Dimensions.CARD_ELEVATION_DEFAULT)
    ) {
        Column(
            modifier = Modifier.padding(AppConstants.Dimensions.PADDING_LARGE)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = AppConstants.Dimensions.SPACING_MEDIUM)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(AppConstants.Dimensions.SPACING_SMALL))
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
        modifier = modifier.fillMaxWidth(),
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
 * Staggered slide-in animation with custom delay
 */
@Composable
private fun StaggeredSlideInItem(
    delayMillis: Int,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(delayMillis.toLong())
        visible = true
    }
    
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(
                durationMillis = 400,
                delayMillis = 0,
                easing = EaseOutCubic
            )
        ) + fadeIn(
            animationSpec = tween(
                durationMillis = 400,
                delayMillis = 0,
                easing = EaseOutCubic
            )
        )
    ) {
        content()
    }
}

/**
 * Gradient preview button that opens gradient picker
 */
@Composable
private fun GradientPreviewButton(
    gradient: CardGradient,
    cardType: com.technitedminds.wallet.domain.model.CardType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = createGradientBrush(gradient)
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = gradient.name ?: "Custom Gradient",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White
                    )
                    Text(
                        text = "Tap to change",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit gradient",
                    tint = Color.White
                )
            }
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
        GradientDirection.TopToBottom -> Brush.verticalGradient(listOf(startColor, endColor))
        GradientDirection.LeftToRight -> Brush.horizontalGradient(listOf(startColor, endColor))
        GradientDirection.DiagonalTopLeftToBottomRight -> Brush.linearGradient(
            colors = listOf(startColor, endColor),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        )
        GradientDirection.DiagonalTopRightToBottomLeft -> Brush.linearGradient(
            colors = listOf(startColor, endColor),
            start = Offset(Float.POSITIVE_INFINITY, 0f),
            end = Offset(0f, Float.POSITIVE_INFINITY)
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