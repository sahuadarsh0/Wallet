package com.technitedminds.wallet.presentation.screens.addcard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.technitedminds.wallet.domain.model.Card
import com.technitedminds.wallet.domain.model.CardType
import com.technitedminds.wallet.presentation.components.common.CardTypeSelector
import com.technitedminds.wallet.presentation.constants.AppConstants
import com.technitedminds.wallet.presentation.components.common.CompactLoadingIndicator
import com.technitedminds.wallet.presentation.components.common.LoadingOverlay

/**
 * Add card screen with step-by-step workflow for card creation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCardScreen(
    onNavigateBack: () -> Unit,
    onCardSaved: (Card) -> Unit,
    onCameraCapture: (CardType) -> Unit,
    modifier: Modifier = Modifier,
    capturedFrontImagePath: String? = null,
    capturedBackImagePath: String? = null,
    capturedExtractedData: Map<String, String> = emptyMap(),
    viewModel: AddCardViewModel = hiltViewModel()
) {
    val currentStep by viewModel.currentStep.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isFormValid by viewModel.isFormValid.collectAsStateWithLifecycle()

    // Handle captured images from camera
    LaunchedEffect(capturedFrontImagePath, capturedBackImagePath, capturedExtractedData) {
        if (capturedFrontImagePath != null) {
            viewModel.setCapturedImages(
                frontPath = capturedFrontImagePath,
                backPath = capturedBackImagePath,
                extractedData = capturedExtractedData
            )
        }
    }

    // Handle events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is AddCardEvent.CardSaved -> onCardSaved(event.card)
            }
        }
    }

    Scaffold(
        topBar = {
            AddCardTopBar(
                currentStep = currentStep,
                onNavigateBack = onNavigateBack,
                onPreviousStep = viewModel::previousStep,
                canGoBack = currentStep != AddCardStep.TYPE_SELECTION
            )
        },
        bottomBar = {
            AddCardBottomBar(
                currentStep = currentStep,
                isFormValid = isFormValid,
                isLoading = uiState.isLoading,
                onNextStep = viewModel::nextStep,
                onSaveCard = viewModel::saveCard,
                onSkipCamera = viewModel::skipCameraCapture
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Progress indicator
            StepProgressIndicator(
                currentStep = currentStep,
                modifier = Modifier.padding(16.dp)
            )

            // Step content
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    slideInHorizontally { it } + fadeIn() togetherWith
                    slideOutHorizontally { -it } + fadeOut()
                },
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                label = AppConstants.UIText.STEP_CONTENT_LABEL
            ) { step ->
                when (step) {
                    AddCardStep.TYPE_SELECTION -> {
                        TypeSelectionStep(
                            onCardTypeSelected = viewModel::selectCardType,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    AddCardStep.CAMERA_CAPTURE -> {
                        CameraCaptureStep(
                            cardType = viewModel.selectedCardType.collectAsStateWithLifecycle().value ?: CardType.Credit,
                            onCameraCapture = onCameraCapture,
                            onExtractedData = viewModel::setExtractedData,
                            onFrontImagePath = viewModel::setFrontImagePath,
                            onBackImagePath = viewModel::setBackImagePath,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    AddCardStep.FORM_DETAILS -> {
                        FormDetailsStep(
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }

    // Error handling
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // Show error snackbar
            // Clear error - method doesn't exist, remove this
        }
    }

    // Loading overlay
    LoadingOverlay(
        isVisible = uiState.isLoading,
        text = AppConstants.UIText.SAVING_CARD
    )
}

/**
 * Top app bar for add card screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddCardTopBar(
    currentStep: AddCardStep,
    onNavigateBack: () -> Unit,
    onPreviousStep: () -> Unit,
    canGoBack: Boolean,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Text(
                text = when (currentStep) {
                    AddCardStep.TYPE_SELECTION -> AppConstants.UIText.SELECT_CARD_TYPE_TITLE
                    AddCardStep.CAMERA_CAPTURE -> AppConstants.UIText.CAPTURE_CARD_IMAGES_TITLE
                    AddCardStep.FORM_DETAILS -> AppConstants.UIText.CARD_DETAILS_TITLE
                }
            )
        },
        navigationIcon = {
            IconButton(
                onClick = if (canGoBack) onPreviousStep else onNavigateBack
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = AppConstants.ContentDescriptions.BACK
                )
            }
        },
        actions = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = AppConstants.DialogText.CANCEL_BUTTON
                )
            }
        },
        modifier = modifier
    )
}

/**
 * Bottom bar with navigation controls
 */
@Composable
private fun AddCardBottomBar(
    currentStep: AddCardStep,
    isFormValid: Boolean,
    isLoading: Boolean,
    onNextStep: () -> Unit,
    onSaveCard: () -> Unit,
    onSkipCamera: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shadowElevation = AppConstants.Dimensions.SPACING_SMALL
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppConstants.Dimensions.PADDING_LARGE),
            horizontalArrangement = Arrangement.spacedBy(AppConstants.Dimensions.SPACING_MEDIUM)
        ) {
            when (currentStep) {
                AddCardStep.TYPE_SELECTION -> {
                    // No bottom bar actions needed
                }
                AddCardStep.CAMERA_CAPTURE -> {
                    OutlinedButton(
                        onClick = onSkipCamera,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(AppConstants.UIText.SKIP_CAMERA)
                    }
                    Button(
                        onClick = onNextStep,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(AppConstants.UIText.CONTINUE)
                    }
                }
                AddCardStep.FORM_DETAILS -> {
                    Button(
                        onClick = onSaveCard,
                        enabled = isFormValid && !isLoading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(AppConstants.Dimensions.ICON_SIZE_SMALL),
                                strokeWidth = AppConstants.Dimensions.CARD_ELEVATION_DEFAULT
                            )
                            Spacer(modifier = Modifier.width(AppConstants.Dimensions.SPACING_SMALL))
                        }
                        Text(AppConstants.UIText.SAVE_CARD)
                    }
                }
            }
        }
    }
}

/**
 * Step progress indicator
 */
@Composable
private fun StepProgressIndicator(
    currentStep: AddCardStep,
    modifier: Modifier = Modifier
) {
    val steps = listOf(
        AppConstants.UIText.STEP_TYPE to AddCardStep.TYPE_SELECTION,
        AppConstants.UIText.STEP_CAPTURE to AddCardStep.CAMERA_CAPTURE,
        AppConstants.UIText.STEP_DETAILS to AddCardStep.FORM_DETAILS
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, (label, step) ->
            val isActive = step == currentStep
            val isCompleted = step.ordinal < currentStep.ordinal

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Step circle
                Surface(
                    modifier = Modifier.size(AppConstants.Dimensions.ICON_SIZE_EXTRA_LARGE),
                    shape = androidx.compose.foundation.shape.CircleShape,
                    color = when {
                        isCompleted -> MaterialTheme.colorScheme.primary
                        isActive -> MaterialTheme.colorScheme.primaryContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (isCompleted) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(AppConstants.Dimensions.ICON_SIZE_SMALL)
                            )
                        } else {
                            Text(
                                text = (index + 1).toString(),
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isActive) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(AppConstants.Dimensions.SPACING_EXTRA_SMALL))

                // Step label
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isActive || isCompleted) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            // Connector line (except for last step)
            if (index < steps.size - 1) {
                HorizontalDivider(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = AppConstants.Dimensions.SPACING_SMALL),
                    color = if (isCompleted) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                )
            }
        }
    }
}

/**
 * Type selection step
 */
@Composable
private fun TypeSelectionStep(
    onCardTypeSelected: (CardType) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(AppConstants.Dimensions.PADDING_LARGE)
    ) {
        Text(
            text = AppConstants.UIText.ADD_CARD_TYPE_PROMPT,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = AppConstants.Dimensions.PADDING_EXTRA_LARGE)
        )

        CardTypeSelector(
            selectedType = CardType.Credit, // Dummy selection for UI
            onTypeSelected = onCardTypeSelected,
            showCustomOption = true,
            showGradientPicker = true,
            selectedGradient = null,
            onGradientSelected = { gradient ->
                // Handle gradient selection
                // This would be passed to the ViewModel
            }
        )
    }
}

/**
 * Camera capture step
 */
@Composable
private fun CameraCaptureStep(
    cardType: CardType,
    onCameraCapture: (CardType) -> Unit,
    onExtractedData: (Map<String, String>) -> Unit,
    onFrontImagePath: (String) -> Unit,
    onBackImagePath: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(AppConstants.Dimensions.PADDING_LARGE),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CameraAlt,
            contentDescription = null,
            modifier = Modifier.size(AppConstants.Dimensions.EMPTY_STATE_ICON_SIZE),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(AppConstants.Dimensions.SPACING_EXTRA_LARGE))

        Text(
            text = String.format(AppConstants.UIText.CAPTURE_CARD_PROMPT_TITLE, cardType.getDisplayName()),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(AppConstants.Dimensions.SPACING_LARGE))

        Text(
            text = AppConstants.UIText.CAPTURE_CARD_PROMPT_SUBTITLE,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(AppConstants.Dimensions.SPACING_EXTRA_LARGE))

        Button(
            onClick = { onCameraCapture(cardType) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = null,
                modifier = Modifier.size(AppConstants.Dimensions.EMPTY_STATE_BUTTON_ICON_SIZE)
            )
            Spacer(modifier = Modifier.width(AppConstants.Dimensions.SPACING_SMALL))
            Text(AppConstants.UIText.OPEN_CAMERA)
        }
    }
}

/**
 * Form details step with OCR result handling and manual entry
 */
@Composable
private fun FormDetailsStep(
    viewModel: AddCardViewModel,
    modifier: Modifier = Modifier
) {
    val cardName by viewModel.cardName.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val customFields by viewModel.customFields.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val extractedData by viewModel.extractedData.collectAsStateWithLifecycle()
    val selectedCardType by viewModel.selectedCardType.collectAsStateWithLifecycle()

    // OCR extracted fields state (editable)
    var cardNumber by remember { mutableStateOf(extractedData["cardNumber"] ?: "") }
    var expiryDate by remember { mutableStateOf(extractedData["expiryDate"] ?: "") }
    var cardholderName by remember { mutableStateOf(extractedData["cardholderName"] ?: "") }
    var cvv by remember { mutableStateOf(extractedData["cvv"] ?: "") }

    // Update local state when extracted data changes
    LaunchedEffect(extractedData) {
        cardNumber = extractedData["cardNumber"] ?: cardNumber
        expiryDate = extractedData["expiryDate"] ?: expiryDate
        cardholderName = extractedData["cardholderName"] ?: cardholderName
        cvv = extractedData["cvv"] ?: cvv
    }

    // Update viewModel when local state changes
    LaunchedEffect(cardNumber, expiryDate, cardholderName, cvv) {
        val updatedData = mutableMapOf<String, String>()
        if (cardNumber.isNotBlank()) updatedData["cardNumber"] = cardNumber
        if (expiryDate.isNotBlank()) updatedData["expiryDate"] = expiryDate
        if (cardholderName.isNotBlank()) updatedData["cardholderName"] = cardholderName
        if (cvv.isNotBlank()) updatedData["cvv"] = cvv
        viewModel.setExtractedData(updatedData)
    }

    val hasOCRData = extractedData.isNotEmpty()
    val isTextualCard = selectedCardType?.supportsOCR() == true

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(AppConstants.Dimensions.PADDING_LARGE),
        verticalArrangement = Arrangement.spacedBy(AppConstants.Dimensions.SPACING_LARGE)
    ) {
        item {
            Text(
                text = AppConstants.UIText.CARD_DETAILS_TITLE,
                style = MaterialTheme.typography.headlineSmall
            )
        }

        // OCR Status Card (for textual cards)
        if (isTextualCard) {
            item {
                if (hasOCRData) {
                    OCRStatusCard(
                        extractedFieldCount = extractedData.size,
                        cardType = selectedCardType ?: CardType.Credit
                    )
                } else {
                    ManualEntryCard(cardType = selectedCardType ?: CardType.Credit)
                }
            }
        }

        item {
            // Card name
            OutlinedTextField(
                value = cardName,
                onValueChange = viewModel::updateCardName,
                label = { Text(AppConstants.UIText.CARD_NAME_LABEL) },
                placeholder = { Text(String.format(AppConstants.UIText.CARD_NAME_PLACEHOLDER, selectedCardType?.getDisplayName() ?: "Card")) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Badge,
                        contentDescription = null
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            // Category selection
            CategoryDropdown(
                categories = categories,
                selectedCategoryId = selectedCategory,
                onCategorySelected = { id -> id?.let(viewModel::updateCategory) },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Card Information Section (for textual cards only)
        if (isTextualCard) {
            item {
                Text(
                    text = AppConstants.UIText.CARD_INFORMATION,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = AppConstants.Dimensions.SPACING_SMALL)
                )
            }

            item {
                // Card Number
                OutlinedTextField(
                    value = cardNumber,
                    onValueChange = { 
                        cardNumber = formatCardNumberInput(it)
                    },
                    label = { Text(AppConstants.UIText.CARD_NUMBER_LABEL) },
                    placeholder = { Text(AppConstants.UIText.CARD_NUMBER_PLACEHOLDER) },
                    keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Payment,
                            contentDescription = null
                        )
                    },
                    trailingIcon = if (hasOCRData && extractedData.containsKey("cardNumber")) {
                        {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = AppConstants.UIText.AUTO_DETECTED,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(AppConstants.Dimensions.ICON_SIZE_MEDIUM)
                            )
                        }
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                // Expiry Date
                OutlinedTextField(
                    value = expiryDate,
                    onValueChange = { 
                        expiryDate = formatExpiryInput(it)
                    },
                    label = { Text(AppConstants.UIText.EXPIRY_DATE_LABEL) },
                    placeholder = { Text(AppConstants.UIText.EXPIRY_DATE_PLACEHOLDER) },
                    keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null
                        )
                    },
                    trailingIcon = if (hasOCRData && extractedData.containsKey("expiryDate")) {
                        {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = AppConstants.UIText.AUTO_DETECTED,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(AppConstants.Dimensions.ICON_SIZE_MEDIUM)
                            )
                        }
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                // Cardholder Name
                OutlinedTextField(
                    value = cardholderName,
                    onValueChange = { 
                        cardholderName = it.uppercase()
                    },
                    label = { Text(AppConstants.UIText.CARDHOLDER_NAME_LABEL) },
                    placeholder = { Text(AppConstants.UIText.CARDHOLDER_NAME_PLACEHOLDER) },
                    keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Text),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null
                        )
                    },
                    trailingIcon = if (hasOCRData && extractedData.containsKey("cardholderName")) {
                        {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = AppConstants.UIText.AUTO_DETECTED,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(AppConstants.Dimensions.ICON_SIZE_MEDIUM)
                            )
                        }
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                // CVV
                OutlinedTextField(
                    value = cvv,
                    onValueChange = { 
                        if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                            cvv = it
                        }
                    },
                    label = { Text(AppConstants.UIText.CVV_LABEL) },
                    placeholder = { Text(AppConstants.UIText.CVV_PLACEHOLDER) },
                    keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null
                        )
                    },
                    trailingIcon = if (hasOCRData && extractedData.containsKey("cvv")) {
                        {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = AppConstants.UIText.AUTO_DETECTED,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(AppConstants.Dimensions.ICON_SIZE_MEDIUM)
                            )
                        }
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Clear all OCR data button
            if (hasOCRData) {
                item {
                    OutlinedButton(
                        onClick = {
                            cardNumber = ""
                            expiryDate = ""
                            cardholderName = ""
                            cvv = ""
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = null,
                            modifier = Modifier.size(AppConstants.Dimensions.EMPTY_STATE_BUTTON_ICON_SIZE)
                        )
                        Spacer(modifier = Modifier.width(AppConstants.Dimensions.SPACING_SMALL))
                        Text(AppConstants.UIText.CLEAR_ALL_AND_ENTER_MANUALLY)
                    }
                }
            }
        }

        item {
            // Custom fields header
            Text(
                text = AppConstants.UIText.ADDITIONAL_INFORMATION,
                style = MaterialTheme.typography.titleMedium
            )
        }

        // Custom fields
        items(
            items = customFields.toList(),
            key = { it.first }
        ) { entry ->
            OutlinedTextField(
                value = entry.second,
                onValueChange = { viewModel.updateCustomField(entry.first, it) },
                label = { Text(entry.first) },
                trailingIcon = {
                    IconButton(
                        onClick = { viewModel.removeCustomField(entry.first) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = AppConstants.UIText.REMOVE_FIELD
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            // Add custom field button
            OutlinedButton(
                onClick = {
                    viewModel.addCustomField(AppConstants.UIText.NOTES_FIELD_LABEL, AppConstants.UIText.EMPTY_STRING)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(AppConstants.Dimensions.EMPTY_STATE_BUTTON_ICON_SIZE)
                )
                Spacer(modifier = Modifier.width(AppConstants.Dimensions.SPACING_SMALL))
                Text(AppConstants.UIText.ADD_FIELD)
            }
        }

        // Privacy notice
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(AppConstants.Dimensions.PADDING_LARGE),
                    verticalArrangement = Arrangement.spacedBy(AppConstants.Dimensions.SPACING_SMALL)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AppConstants.Dimensions.SPACING_SMALL)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            modifier = Modifier.size(AppConstants.Dimensions.ICON_SIZE_MEDIUM),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = AppConstants.UIText.PRIVACY_AND_SECURITY,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Text(
                        text = AppConstants.UIText.PRIVACY_NOTICE,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * OCR status card showing extraction results
 */
@Composable
private fun OCRStatusCard(
    extractedFieldCount: Int,
    cardType: CardType,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppConstants.Dimensions.PADDING_LARGE),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppConstants.Dimensions.SPACING_MEDIUM)
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                modifier = Modifier.size(AppConstants.Dimensions.ICON_SIZE_EXTRA_LARGE),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = AppConstants.UIText.TEXT_RECOGNITION_COMPLETE,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Text(
                    text = String.format(AppConstants.UIText.TEXT_RECOGNITION_SUBTITLE, extractedFieldCount, if (extractedFieldCount != 1) "s" else "", cardType.getDisplayName().lowercase()),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

/**
 * Manual entry card for when OCR fails
 */
@Composable
private fun ManualEntryCard(
    cardType: CardType,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppConstants.Dimensions.PADDING_LARGE),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppConstants.Dimensions.SPACING_MEDIUM)
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null,
                modifier = Modifier.size(AppConstants.Dimensions.ICON_SIZE_EXTRA_LARGE),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = AppConstants.UIText.MANUAL_ENTRY_TITLE,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                
                Text(
                    text = String.format(AppConstants.UIText.MANUAL_ENTRY_SUBTITLE, cardType.getDisplayName().lowercase()),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

/**
 * Formats card number input as user types (adds spaces every 4 digits)
 */
private fun formatCardNumberInput(input: String): String {
    val digitsOnly = input.filter { it.isDigit() }
    return digitsOnly.chunked(4).joinToString(" ").take(19) // Max 16 digits + 3 spaces
}

/**
 * Formats expiry date input as user types (MM/YY)
 */
private fun formatExpiryInput(input: String): String {
    val digitsOnly = input.filter { it.isDigit() }
    return when {
        digitsOnly.length <= 2 -> digitsOnly
        digitsOnly.length <= 4 -> "${digitsOnly.take(2)}/${digitsOnly.drop(2)}"
        else -> "${digitsOnly.take(2)}/${digitsOnly.drop(2).take(2)}"
    }
}

/**
 * Category dropdown component
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDropdown(
    categories: List<com.technitedminds.wallet.domain.model.Category>,
    selectedCategoryId: String?,
    onCategorySelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedCategory = categories.find { it.id == selectedCategoryId }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedCategory?.name ?: "",
            onValueChange = { },
            readOnly = true,
            label = { Text(AppConstants.UIText.CATEGORY_LABEL) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Category,
                    contentDescription = null
                )
            },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable,true)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category.name) },
                    onClick = {
                        onCategorySelected(category.id)
                        expanded = false
                    }
                )
            }
        }
    }
}