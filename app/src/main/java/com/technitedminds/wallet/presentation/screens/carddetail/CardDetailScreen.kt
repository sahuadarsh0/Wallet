package com.technitedminds.wallet.presentation.screens.carddetail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInCubic
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.technitedminds.wallet.domain.model.Card
import com.technitedminds.wallet.domain.model.CardGradient
import com.technitedminds.wallet.domain.model.GradientDirection
import com.technitedminds.wallet.presentation.components.animation.EnhancedSlideInItem
import com.technitedminds.wallet.presentation.components.animation.FlippableCard
import com.technitedminds.wallet.presentation.components.animation.liquidDrag
import com.technitedminds.wallet.presentation.components.animation.liquidPress
import com.technitedminds.wallet.presentation.components.common.AnimatedSectionHeader
import com.technitedminds.wallet.presentation.components.common.CardDeleteConfirmationDialog
import com.technitedminds.wallet.presentation.components.common.CardTypeDropdown
import com.technitedminds.wallet.presentation.components.common.CategoryDropdown
import com.technitedminds.wallet.presentation.components.common.CustomFieldsEditor
import com.technitedminds.wallet.presentation.components.common.ExtractedDataEditor
import com.technitedminds.wallet.presentation.components.common.GlassPremiumCard
import com.technitedminds.wallet.presentation.components.common.GradientPickerDialog
import com.technitedminds.wallet.presentation.components.common.LoadingOverlay
import com.technitedminds.wallet.presentation.components.common.PremiumDivider
import com.technitedminds.wallet.presentation.components.common.PremiumTextField
import com.technitedminds.wallet.presentation.components.common.ScreenGradientBackground
import com.technitedminds.wallet.presentation.components.common.SecureAuthDialog
import com.technitedminds.wallet.presentation.components.common.getIcon
import com.technitedminds.wallet.presentation.components.common.gradientShadow
import com.technitedminds.wallet.presentation.components.common.resolveCategoryName
import com.technitedminds.wallet.presentation.components.sharing.CardSharingDialog
import com.technitedminds.wallet.presentation.constants.AppConstants
import com.technitedminds.wallet.ui.theme.Glass
import com.technitedminds.wallet.ui.theme.GlassSurface
import com.technitedminds.wallet.ui.theme.gradientContrastText
import kotlinx.coroutines.delay

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
    val clipboardManager = LocalClipboardManager.current

    // Re-auth dialog for copying decrypted card details.
    // Once the user authenticates, [unlockedForCopy] stays true for the rest
    // of the auto-lock timer so subsequent copies don't re-prompt.
    var showCopyAuthDialog by remember { mutableStateOf(false) }
    var unlockedForCopy by remember { mutableStateOf(false) }
    // When non-null, [SecureAuthDialog]'s success path copies just this single
    // (label, value) pair rather than the full sensitive payload. Used for
    // the long-press-to-copy gesture on individual sensitive rows.
    var pendingCopy by remember { mutableStateOf<Pair<String, String>?>(null) }

    // Security auto-exit timer (pauses during edit mode)
    var remainingSeconds by remember { mutableIntStateOf(180) }

    LaunchedEffect(isEditing) {
        if (!isEditing) {
            remainingSeconds = 180
            // Reset copy unlock whenever the auto-lock timer restarts
            unlockedForCopy = false
            while (remainingSeconds > 0) {
                delay(1000)
                remainingSeconds--
            }
            onNavigateBack()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CardDetailEvent.CardDeleted -> onNavigateBack()
                is CardDetailEvent.CardSaved -> {}
                is CardDetailEvent.ShareSuccess -> {}
            }
        }
    }

    if (card == null) {
        Box(modifier = Modifier.fillMaxSize())
        return
    }

    // Derive ambient background from card gradient
    val cardGradient = card!!.getGradient()
    val ambientStart = remember(cardGradient) {
        try { Color(cardGradient.startColor.toColorInt()) }
        catch (e: Exception) { Color.Transparent }
    }
    val ambientEnd = remember(cardGradient) {
        try { Color(android.graphics.Color.parseColor(cardGradient.endColor)) }
        catch (e: Exception) { Color.Transparent }
    }

    ScreenGradientBackground(
        accentColor = ambientStart.copy(alpha = 1f).takeIf { it != Color.Transparent }
            ?: MaterialTheme.colorScheme.primary,
        modifier = modifier,
    ) {
        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            containerColor = Color.Transparent,
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
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
                    .verticalScroll(rememberScrollState())
            ) {
                // Card hero
                EnhancedSlideInItem(visible = true, index = 0) {
                    CardDisplaySection(
                        card = if (isEditing) editedCard ?: card!! else card!!,
                        onShowSharingDialog = viewModel::showSharingDialog,
                        modifier = Modifier.padding(AppConstants.Dimensions.PADDING_LARGE)
                    )
                }

                // Edit mode
                AnimatedVisibility(
                    visible = isEditing,
                    enter = fadeIn(tween(300, easing = EaseOutCubic)),
                    exit = fadeOut(tween(300, easing = EaseInCubic))
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

                // View mode
                AnimatedVisibility(
                    visible = !isEditing,
                    enter = fadeIn(tween(300, easing = EaseOutCubic)),
                    exit = fadeOut(tween(300, easing = EaseInCubic))
                ) {
                    Column(
                        modifier = Modifier.padding(AppConstants.Dimensions.PADDING_LARGE),
                        verticalArrangement = Arrangement.spacedBy(AppConstants.Dimensions.SPACING_LARGE)
                    ) {
                        // Quick meta bar
                        EnhancedSlideInItem(visible = true, index = 1) {
                            QuickMetaBar(card = card!!)
                        }

                        // Sensitive data section (OCR cards)
                        if (card!!.extractedData.isNotEmpty()) {
                            EnhancedSlideInItem(visible = true, index = 2) {
                                SensitiveDataSection(
                                    card = card!!,
                                    onCopyClick = {
                                        if (unlockedForCopy) {
                                            val payload = buildSensitiveCopyText(card!!)
                                            if (payload.isNotBlank()) {
                                                clipboardManager.setText(AnnotatedString(payload))
                                                viewModel.onCardDetailsCopied()
                                            } else {
                                                viewModel.onNothingToCopy()
                                            }
                                        } else {
                                            pendingCopy = null
                                            showCopyAuthDialog = true
                                        }
                                    },
                                    onLongPressCopy = { label, value ->
                                        if (unlockedForCopy) {
                                            clipboardManager.setText(AnnotatedString(value))
                                            viewModel.onValueCopied(label)
                                        } else {
                                            pendingCopy = label to value
                                            showCopyAuthDialog = true
                                        }
                                    },
                                )
                            }
                        }

                        // Custom fields section
                        val visibleFields = card!!.customFields.filter {
                            it.key != AppConstants.UIText.CUSTOM_COLOR_FIELD
                        }
                        if (visibleFields.isNotEmpty()) {
                            EnhancedSlideInItem(visible = true, index = 3) {
                                DetailsSection(
                                    customFields = visibleFields,
                                    onLongPressCopy = { label, value ->
                                        clipboardManager.setText(AnnotatedString(value))
                                        viewModel.onValueCopied(label)
                                    },
                                )
                            }
                        }

                        // Security timer bar
                        EnhancedSlideInItem(visible = true, index = 4) {
                            SecurityTimerBar(remainingSeconds = remainingSeconds)
                        }
                    }
                }
            }
        }
    }
    }

    CardDeleteConfirmationDialog(
        isVisible = uiState.showDeleteDialog,
        cardName = card!!.name,
        onConfirm = {
            viewModel.hideDeleteConfirmation()
            viewModel.deleteCard()
        },
        onDismiss = viewModel::hideDeleteConfirmation
    )

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { message ->
            snackbarHostState.showSnackbar(message = message, duration = SnackbarDuration.Short)
            viewModel.clearSuccessMessage()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error, actionLabel = "Dismiss", duration = SnackbarDuration.Long
            )
            viewModel.clearError()
        }
    }

    LoadingOverlay(isVisible = uiState.isLoading, text = AppConstants.UIText.PROCESSING)

    CardSharingDialog(
        card = card!!,
        isVisible = showSharingDialog,
        onDismiss = viewModel::hideSharingDialog,
        onShare = { option, config -> viewModel.shareCardWithConfig(option, config) }
    )

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

    SecureAuthDialog(
        isVisible = showCopyAuthDialog,
        title = "Verify it's you",
        message = if (pendingCopy != null) {
            "Authenticate with your fingerprint or PIN to copy ${pendingCopy!!.first} to the clipboard."
        } else {
            "Authenticate with your fingerprint or PIN to copy the card number, CVV, and expiry to the clipboard."
        },
        onDismiss = {
            showCopyAuthDialog = false
            pendingCopy = null
        },
        onAuthenticated = {
            showCopyAuthDialog = false
            unlockedForCopy = true
            val pending = pendingCopy
            pendingCopy = null
            if (pending != null) {
                clipboardManager.setText(AnnotatedString(pending.second))
                viewModel.onValueCopied(pending.first)
            } else {
                val payload = buildSensitiveCopyText(card!!)
                if (payload.isNotBlank()) {
                    clipboardManager.setText(AnnotatedString(payload))
                    viewModel.onCardDetailsCopied()
                } else {
                    viewModel.onNothingToCopy()
                }
            }
        },
    )
}

// ─── Top Bar ─────────────────────────────────────────────────────────────────

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
                Icon(Icons.AutoMirrored.Default.ArrowBack, AppConstants.ContentDescriptions.BACK)
            }
        },
        actions = {
            if (isEditing) {
                IconButton(onClick = onCancelEdit) {
                    Icon(Icons.Default.Close, AppConstants.DialogText.CANCEL_BUTTON)
                }
                IconButton(onClick = onSaveEdit) {
                    Icon(Icons.Default.Check, AppConstants.ContentDescriptions.SAVE)
                }
            } else {
                IconButton(onClick = onShowSharingDialog) {
                    Icon(Icons.Default.Share, AppConstants.UIText.SHARE_OPTIONS)
                }
                IconButton(onClick = onStartEdit) {
                    Icon(Icons.Default.Edit, AppConstants.ContentDescriptions.EDIT)
                }
                IconButton(onClick = onDeleteCard) {
                    Icon(Icons.Default.Delete, AppConstants.DialogText.DELETE_BUTTON)
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
        ),
        modifier = modifier,
    )
}

// ─── Card Display (Hero) ─────────────────────────────────────────────────────

@Composable
private fun CardDisplaySection(
    card: Card,
    onShowSharingDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    FlippableCard(
        card = card,
        onCardClick = null,
        onCardLongPress = onShowSharingDialog,
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp)
            .liquidDrag(maxOffset = 16.dp),
    )
}

// ─── View Mode: QuickMetaBar ─────────────────────────────────────────────────

@Composable
private fun QuickMetaBar(
    card: Card,
    modifier: Modifier = Modifier
) {
    GlassSurface(
        shape = RoundedCornerShape(Glass.PillCornerRadius),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Type
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = card.type.getIcon(),
                        contentDescription = "Card type",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = card.type.getDisplayName(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Vertical divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(16.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            )

            // Category
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Category,
                    contentDescription = "Category",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = resolveCategoryName(card.categoryId),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Vertical divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(16.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            )

            // Date
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "Date created",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = formatDate(card.updatedAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

// ─── View Mode: RevealableFieldRow ───────────────────────────────────────────

@Composable
private fun RevealableFieldRow(
    label: String,
    value: String,
    icon: ImageVector,
    isSensitive: Boolean = false,
    onLongPress: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var isRevealed by remember { mutableStateOf(!isSensitive) }
    val displayValue = if (isRevealed) value else maskSensitiveData(value)
    val hapticFeedback = LocalHapticFeedback.current

    val rowModifier = if (onLongPress != null) {
        modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {},
                onLongClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongPress()
                },
            )
            .padding(vertical = 6.dp)
    } else {
        modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    }

    Row(
        modifier = rowModifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.size(18.dp)
            )
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = displayValue,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        if (isSensitive) {
            IconButton(
                onClick = { isRevealed = !isRevealed },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = if (isRevealed) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = if (isRevealed) "Hide" else "Reveal",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ─── View Mode: SensitiveDataSection ─────────────────────────────────────────

@Composable
private fun SensitiveDataSection(
    card: Card,
    onCopyClick: () -> Unit,
    onLongPressCopy: (label: String, value: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val sensitiveKeys = listOf("cardNumber", "cvv")

    GlassPremiumCard(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            AnimatedSectionHeader(
                title = AppConstants.UIText.CARD_DETAILS_TITLE,
                icon = Icons.Default.Shield,
                action = {
                    IconButton(
                        onClick = onCopyClick,
                        modifier = Modifier.size(40.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy sensitive details",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                },
            )

            Text(
                text = "Tip: long-press a field to copy just its value",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp),
            )

            Spacer(modifier = Modifier.height(4.dp))

            card.extractedData.entries.forEachIndexed { index, (key, value) ->
                if (index > 0) {
                    PremiumDivider(modifier = Modifier.padding(vertical = 2.dp))
                }
                val label = formatFieldName(key)
                RevealableFieldRow(
                    label = label,
                    value = value,
                    icon = getFieldIcon(key),
                    isSensitive = key in sensitiveKeys,
                    onLongPress = if (value.isNotBlank()) {
                        { onLongPressCopy(label, value) }
                    } else null,
                )
            }
        }
    }
}

// ─── View Mode: DetailsSection ───────────────────────────────────────────────

@Composable
private fun DetailsSection(
    customFields: Map<String, String>,
    onLongPressCopy: (label: String, value: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val sensitiveKeys = listOf(AppConstants.UIText.PIN_FIELD, AppConstants.UIText.PASSWORD_FIELD)

    GlassPremiumCard(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            AnimatedSectionHeader(
                title = "Additional Details",
                icon = Icons.Default.Description,
            )

            Text(
                text = "Tip: long-press a field to copy just its value",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp),
            )

            Spacer(modifier = Modifier.height(4.dp))

            customFields.entries.forEachIndexed { index, (key, value) ->
                if (index > 0) {
                    PremiumDivider(modifier = Modifier.padding(vertical = 2.dp))
                }
                val label = formatFieldName(key)
                RevealableFieldRow(
                    label = label,
                    value = value,
                    icon = getFieldIcon(key),
                    isSensitive = key in sensitiveKeys,
                    onLongPress = if (value.isNotBlank()) {
                        { onLongPressCopy(label, value) }
                    } else null,
                )
            }
        }
    }
}

// ─── View Mode: SecurityTimerBar ─────────────────────────────────────────────

@Composable
private fun SecurityTimerBar(
    remainingSeconds: Int,
    modifier: Modifier = Modifier
) {
    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val isWarning = remainingSeconds < 30

    val warningAlpha by animateFloatAsState(
        targetValue = if (isWarning) 0.8f else 0f,
        animationSpec = tween(300),
        label = "warning_alpha"
    )

    val pulseTransition = rememberInfiniteTransition(label = "timer_pulse")
    val pulseAlpha by pulseTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val timerColor = if (isWarning) {
        MaterialTheme.colorScheme.error.copy(alpha = pulseAlpha)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    GlassSurface(
        shape = RoundedCornerShape(Glass.PillCornerRadius),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Auto-lock timer",
                tint = timerColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Auto-locking in %d:%02d".format(minutes, seconds),
                style = MaterialTheme.typography.bodySmall,
                color = timerColor
            )
        }
    }
}

// ─── Edit Mode ───────────────────────────────────────────────────────────────

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
        // Section 1: Basic Information
        EnhancedSlideInItem(visible = true, index = 0, baseDelay = 100) {
            GlassPremiumCard(
                modifier = Modifier.fillMaxWidth(),
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

                    PremiumTextField(
                        value = card.name,
                        onValueChange = onUpdateName,
                        label = AppConstants.UIText.CARD_NAME_LABEL,
                        leadingIcon = Icons.Default.CreditCard,
                        modifier = Modifier.fillMaxWidth()
                    )

                    CardTypeDropdown(
                        selectedCardType = card.type,
                        onCardTypeSelected = onUpdateCardType,
                        modifier = Modifier.fillMaxWidth()
                    )

                    CategoryDropdown(
                        categories = categories,
                        selectedCategoryId = card.categoryId,
                        onCategorySelected = onUpdateCategory,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Section 2: Card Details (OCR) -- PremiumCard with Shield icon
        if (card.type.supportsOCR() && (card.extractedData.isNotEmpty() || card.type.supportsOCR())) {
            EnhancedSlideInItem(visible = true, index = 1, baseDelay = 100) {
                GlassPremiumCard(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(AppConstants.Dimensions.SPACING_LARGE)
                    ) {
                        AnimatedSectionHeader(
                            title = AppConstants.UIText.CARD_DETAILS_TITLE,
                            icon = Icons.Default.Shield,
                            subtitle = AppConstants.UIText.CARD_DETAILS_SUBTITLE
                        )

                        ExtractedDataEditor(
                            card = card,
                            onUpdateExtractedData = onUpdateExtractedData,
                            onAddField = { fieldKey -> onUpdateExtractedData(fieldKey, "") }
                        )
                    }
                }
            }
        }

        // Section 3: Additional Details
        EnhancedSlideInItem(visible = true, index = 2, baseDelay = 100) {
            GlassPremiumCard(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(AppConstants.Dimensions.SPACING_LARGE)
                ) {
                    AnimatedSectionHeader(
                        title = "Additional Details",
                        icon = Icons.Default.Description,
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

        // Section 4: Appearance -- Unwrapped (GradientPreviewButton is its own visual)
        EnhancedSlideInItem(visible = true, index = 3, baseDelay = 100) {
            Column(
                verticalArrangement = Arrangement.spacedBy(AppConstants.Dimensions.SPACING_MEDIUM)
            ) {
                AnimatedSectionHeader(
                    title = AppConstants.UIText.APPEARANCE_TITLE,
                    icon = Icons.Default.Palette,
                    subtitle = AppConstants.UIText.APPEARANCE_SUBTITLE
                )

                GradientPreviewButton(
                    gradient = card.customGradient ?: Card.getDefaultGradientForType(card.type),
                    cardType = card.type,
                    onClick = viewModel::showGradientPicker
                )
            }
        }

        // Section 5: Save Reminder -- Lightweight GlassSurface banner
        EnhancedSlideInItem(visible = true, index = 4, baseDelay = 100) {
            GlassSurface(
                shape = RoundedCornerShape(Glass.PillCornerRadius),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Information",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = AppConstants.UIText.SAVE_CHANGES_REMINDER,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ─── GradientPreviewButton ───────────────────────────────────────────────────

@Composable
private fun GradientPreviewButton(
    gradient: CardGradient,
    cardType: com.technitedminds.wallet.domain.model.CardType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val textColor = gradientContrastText(gradient.startColor, gradient.endColor)
    val shadowColors = listOf(
        Color(android.graphics.Color.parseColor(gradient.startColor)),
        Color(android.graphics.Color.parseColor(gradient.endColor)),
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .gradientShadow(colors = shadowColors, shadowElevation = 8.dp, cornerRadius = 12.dp)
            .liquidPress()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = createGradientBrush(gradient)),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = gradient.name ?: "Custom Gradient",
                        style = MaterialTheme.typography.titleSmall,
                        color = textColor,
                    )
                    Text(
                        text = "Tap to change",
                        style = MaterialTheme.typography.bodySmall,
                        color = textColor.copy(alpha = 0.8f),
                    )
                }
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit gradient",
                    tint = textColor,
                )
            }
        }
    }
}

// ─── Utilities ───────────────────────────────────────────────────────────────

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
        value.length <= 4 -> "••••"
        else -> "•".repeat(value.length - 4) + value.takeLast(4)
    }
}

private fun getFieldIcon(key: String): ImageVector {
    return when (key) {
        "cardNumber" -> Icons.Default.CreditCard
        "cvv" -> Icons.Default.Shield
        "expiryDate" -> Icons.Default.CalendarToday
        "cardholderName" -> Icons.Default.CreditCard
        "bankName" -> Icons.Default.CreditCard
        "pin", "password" -> Icons.Default.Lock
        else -> Icons.Default.Description
    }
}

/**
 * Build a clipboard payload containing ONLY the three sensitive fields
 * (card number, CVV, expiry date) — never the full card record.
 *
 * Called only after the user has passed biometric / PIN re-auth via
 * [SecureAuthDialog], so values are emitted unmasked. Blank fields are
 * omitted; if no relevant fields exist, an empty string is returned and
 * the caller surfaces a "nothing to copy" message instead.
 */
private fun buildSensitiveCopyText(card: Card): String {
    val keys = listOf("cardNumber", "cvv", "expiryDate")
    val lines = keys.mapNotNull { key ->
        val value = card.extractedData[key]?.trim().orEmpty()
        if (value.isBlank()) null else "${formatFieldName(key)}: $value"
    }
    return lines.joinToString(separator = "\n")
}
