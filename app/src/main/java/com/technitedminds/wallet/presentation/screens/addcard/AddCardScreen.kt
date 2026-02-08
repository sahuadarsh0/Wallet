package com.technitedminds.wallet.presentation.screens.addcard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import com.technitedminds.wallet.ui.theme.WalletSpring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Scanner
import androidx.compose.material.icons.automirrored.filled.Notes
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
import com.technitedminds.wallet.domain.model.CardGradient
import com.technitedminds.wallet.domain.model.CardType
import com.technitedminds.wallet.domain.model.GradientDirection
import com.technitedminds.wallet.presentation.components.common.CardTypeSelector
import com.technitedminds.wallet.presentation.components.common.GradientPickerDialog
import com.technitedminds.wallet.presentation.components.common.PrivacyNoticeCard
import com.technitedminds.wallet.presentation.components.common.StepProgressIndicator
import com.technitedminds.wallet.presentation.components.common.OCRStatusCard
import com.technitedminds.wallet.presentation.components.common.ManualEntryCard
import com.technitedminds.wallet.presentation.components.common.PremiumTextField
import com.technitedminds.wallet.presentation.components.common.AnimatedSectionHeader
import com.technitedminds.wallet.presentation.components.common.PremiumCard
import com.technitedminds.wallet.presentation.constants.AppConstants
import com.technitedminds.wallet.presentation.components.common.CompactLoadingIndicator
import com.technitedminds.wallet.presentation.components.common.LoadingOverlay
import com.technitedminds.wallet.presentation.components.animation.AnimatedSection

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
            val currentState = viewModel.captureState.value
            val currentFrontPath = viewModel.uiState.value.frontImagePath
            
            // Handle front image capture
            if (capturedFrontImagePath != currentFrontPath) {
                viewModel.setFrontImagePath(capturedFrontImagePath)
                // If extracted data is available, set it
                if (capturedExtractedData.isNotEmpty()) {
                    viewModel.setExtractedData(capturedExtractedData)
                }
            }
            
            // Handle back image capture
            if (capturedBackImagePath != null && capturedBackImagePath != viewModel.uiState.value.backImagePath) {
                viewModel.setBackImagePath(capturedBackImagePath)
                // Update extracted data if available
                if (capturedExtractedData.isNotEmpty()) {
                    viewModel.setExtractedData(capturedExtractedData)
                }
            }
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
                isLoading = uiState.isLoading || uiState.isSaving,
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
                currentStep = currentStep.ordinal,
                modifier = Modifier.padding(16.dp)
            )

            // Step content with direction-aware animations
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    // Determine direction by comparing step ordinals
                    val isForward = targetState.ordinal > initialState.ordinal
                    
                    if (isForward) {
                        // Forward: spring slide in from right with overshoot
                        slideInHorizontally(
                            initialOffsetX = { it },
                            animationSpec = WalletSpring.gentle()
                        ) + fadeIn(
                            animationSpec = tween(300)
                        ) togetherWith slideOutHorizontally(
                            targetOffsetX = { -it / 3 },
                            animationSpec = WalletSpring.gentle()
                        ) + fadeOut(
                            animationSpec = tween(200)
                        )
                    } else {
                        // Backward: spring slide in from left with overshoot
                        slideInHorizontally(
                            initialOffsetX = { -it },
                            animationSpec = WalletSpring.gentle()
                        ) + fadeIn(
                            animationSpec = tween(300)
                        ) togetherWith slideOutHorizontally(
                            targetOffsetX = { it / 3 },
                            animationSpec = WalletSpring.gentle()
                        ) + fadeOut(
                            animationSpec = tween(200)
                        )
                    }
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
                            captureState = viewModel.captureState.collectAsStateWithLifecycle().value,
                            onCameraCapture = onCameraCapture,
                            onExtractedData = viewModel::setExtractedData,
                            onFrontImagePath = viewModel::setFrontImagePath,
                            onBackImagePath = viewModel::setBackImagePath,
                            onOpenCameraForFront = viewModel::openCameraForFront,
                            onOpenCameraForBack = viewModel::openCameraForBack,
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

    // Error dialog
    uiState.error?.let { error ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("Error") },
            text = { Text(error) },
            confirmButton = {
                Button(onClick = { viewModel.clearError() }) {
                    Text("OK")
                }
            }
        )
    }

    // Loading overlay during save operation
    LoadingOverlay(
        isVisible = uiState.isSaving,
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
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
 * Camera capture step with front-first capture flow
 */
@Composable
private fun CameraCaptureStep(
    cardType: CardType,
    captureState: CaptureState,
    onCameraCapture: (CardType) -> Unit,
    onExtractedData: (Map<String, String>) -> Unit,
    onFrontImagePath: (String) -> Unit,
    onBackImagePath: (String) -> Unit,
    onOpenCameraForFront: () -> Unit,
    onOpenCameraForBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(AppConstants.Dimensions.PADDING_LARGE),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Large camera icon (120dp) with primary color
        Icon(
            imageVector = Icons.Default.CameraAlt,
            contentDescription = null,
            modifier = Modifier.size(AppConstants.Dimensions.CAMERA_ICON_SIZE_LARGE),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(AppConstants.Dimensions.SPACING_EXTRA_LARGE))

        // Contextual title based on capture state and card type
        Text(
            text = when (captureState) {
                CaptureState.AWAITING_FRONT -> {
                    String.format(AppConstants.UIText.POSITION_FRONT_OF_CARD_PROMPT, cardType.getDisplayName())
                }
                CaptureState.FRONT_CAPTURED -> {
                    String.format(AppConstants.UIText.POSITION_BACK_OF_CARD_PROMPT, cardType.getDisplayName())
                }
                CaptureState.BACK_CAPTURED -> {
                    String.format(AppConstants.UIText.CAPTURE_CARD_PROMPT_TITLE, cardType.getDisplayName())
                }
            },
            style = MaterialTheme.typography.headlineSmall,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(AppConstants.Dimensions.SPACING_LARGE))

        // Contextual instructions based on card type
        Text(
            text = when {
                captureState == CaptureState.AWAITING_FRONT && cardType.supportsOCR() -> {
                    "We'll automatically extract card details from the front image."
                }
                captureState == CaptureState.AWAITING_FRONT -> {
                    "Take a clear photo of the front of your ${cardType.getDisplayName().lowercase()}."
                }
                captureState == CaptureState.FRONT_CAPTURED && cardType.supportsOCR() -> {
                    "Now capture the back side. We'll extract the CVV and other details."
                }
                captureState == CaptureState.FRONT_CAPTURED -> {
                    "Now capture the back side of your ${cardType.getDisplayName().lowercase()}."
                }
                else -> {
                    AppConstants.UIText.CAPTURE_CARD_PROMPT_SUBTITLE
                }
            },
            style = MaterialTheme.typography.bodyLarge,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Additional instructions for textual cards
        if (cardType.supportsOCR() && captureState == CaptureState.AWAITING_FRONT) {
            Spacer(modifier = Modifier.height(AppConstants.Dimensions.SPACING_MEDIUM))
            Text(
                text = AppConstants.UIText.CAPTURE_INSTRUCTIONS,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(AppConstants.Dimensions.SPACING_EXTRA_LARGE))

        // Open Camera Button
        Button(
            onClick = { onCameraCapture(cardType) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppConstants.Dimensions.PADDING_EXTRA_LARGE)
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = null,
                modifier = Modifier.size(AppConstants.Dimensions.ICON_SIZE_MEDIUM)
            )
            Spacer(modifier = Modifier.width(AppConstants.Dimensions.SPACING_SMALL))
            Text(
                text = when (captureState) {
                    CaptureState.AWAITING_FRONT -> "Capture Front"
                    CaptureState.FRONT_CAPTURED -> "Capture Back"
                    CaptureState.BACK_CAPTURED -> "Recapture"
                }
            )
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
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val cardName by viewModel.cardName.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val customFields by viewModel.customFields.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val extractedData by viewModel.extractedData.collectAsStateWithLifecycle()
    val selectedCardType by viewModel.selectedCardType.collectAsStateWithLifecycle()
    val selectedGradient by viewModel.selectedGradient.collectAsStateWithLifecycle()
    val showGradientPicker by viewModel.showGradientPicker.collectAsStateWithLifecycle()

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

    // Note: ViewModel is updated directly via updateCardNumber, updateExpiryDate, etc.
    // in the PremiumTextField onValueChange callbacks below

    val hasOCRData = extractedData.isNotEmpty()
    val isTextualCard = selectedCardType?.supportsOCR() == true

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(AppConstants.Dimensions.PADDING_LARGE),
        verticalArrangement = Arrangement.spacedBy(AppConstants.Dimensions.SPACING_LARGE)
    ) {
        // OCR Status Card (for textual cards)
        if (isTextualCard) {
            item {
                AnimatedSection(index = 0) {
                    if (hasOCRData) {
                        OCRStatusCard(
                            extractedFieldCount = extractedData.size
                        )
                    } else {
                        ManualEntryCard()
                    }
                }
            }
        }

        // Card Details Section: Name and Category
        item {
            AnimatedSection(index = if (isTextualCard) 1 else 0) {
                PremiumCard(
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        AnimatedSectionHeader(
                            title = "Card Details",
                            icon = Icons.Default.CreditCard,
                            subtitle = "Name and category for your card"
                        )
                        
                        // Card name field
                        PremiumTextField(
                            value = cardName,
                            onValueChange = viewModel::updateCardName,
                            label = AppConstants.UIText.CARD_NAME_LABEL,
                            leadingIcon = Icons.Default.Badge,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        // Category selection
                        CategoryDropdown(
                            categories = categories,
                            selectedCategoryId = selectedCategory,
                            onCategorySelected = { id -> id?.let(viewModel::updateCategory) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Card Information Section (for textual cards only)
        if (isTextualCard) {
            item {
                AnimatedSection(index = 2) {
                    PremiumCard(
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            AnimatedSectionHeader(
                                title = AppConstants.UIText.CARD_INFORMATION,
                                icon = Icons.Default.Scanner,
                                subtitle = "Card number, expiry and security details"
                            )
                            
                            // Card Number
                            PremiumTextField(
                                value = cardNumber,
                                onValueChange = { 
                                    val formatted = formatCardNumberInput(it)
                                    cardNumber = formatted
                                    viewModel.updateCardNumber(formatted)
                                },
                                label = AppConstants.UIText.CARD_NUMBER_LABEL,
                                keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                                leadingIcon = Icons.Default.Payment,
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
                                isError = uiState.cardNumberError != null,
                                errorMessage = uiState.cardNumberError,
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            // Expiry Date
                            PremiumTextField(
                                value = expiryDate,
                                onValueChange = { 
                                    val formatted = formatExpiryInput(it)
                                    expiryDate = formatted
                                    viewModel.updateExpiryDate(formatted)
                                },
                                label = AppConstants.UIText.EXPIRY_DATE_LABEL,
                                keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                                leadingIcon = Icons.Default.DateRange,
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
                                isError = uiState.expiryDateError != null,
                                errorMessage = uiState.expiryDateError,
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            // Cardholder Name
                            PremiumTextField(
                                value = cardholderName,
                                onValueChange = { 
                                    val uppercased = it.uppercase()
                                    cardholderName = uppercased
                                    viewModel.updateCardholderName(uppercased)
                                },
                                label = AppConstants.UIText.CARDHOLDER_NAME_LABEL,
                                keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Text),
                                leadingIcon = Icons.Default.Person,
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
                                isError = uiState.cardholderNameError != null,
                                errorMessage = uiState.cardholderNameError,
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            // CVV
                            PremiumTextField(
                                value = cvv,
                                onValueChange = { 
                                    if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                                        cvv = it
                                        viewModel.updateCvv(it)
                                    }
                                },
                                label = AppConstants.UIText.CVV_LABEL,
                                keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                                leadingIcon = Icons.Default.Security,
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
                                isError = uiState.cvvError != null,
                                errorMessage = uiState.cvvError,
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            // Clear all OCR data button
                            if (hasOCRData) {
                                OutlinedButton(
                                    onClick = {
                                        cardNumber = ""
                                        expiryDate = ""
                                        cardholderName = ""
                                        cvv = ""
                                        viewModel.clearOCRData()
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
                }
            }
        }

        // Appearance Section (for ALL card types)
        item {
            val appearanceIndex = if (isTextualCard) 3 else 2
            AnimatedSection(index = appearanceIndex) {
                PremiumCard(
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AnimatedSectionHeader(
                            title = "Appearance",
                            icon = Icons.Default.Palette,
                            subtitle = "Customize your card's visual style"
                        )
                        
                        AppearanceSection(
                            cardType = selectedCardType ?: CardType.Credit,
                            selectedGradient = selectedGradient,
                            onEditGradient = { viewModel.showGradientPicker() },
                            modifier = Modifier.fillMaxWidth(),
                            showHeader = false // Header is now in PremiumCard
                        )
                    }
                }
            }
        }

        // Additional Information Section
        item {
            val additionalInfoIndex = if (isTextualCard) 4 else 3
            AnimatedSection(index = additionalInfoIndex) {
                PremiumCard(
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AnimatedSectionHeader(
                            title = AppConstants.UIText.ADDITIONAL_INFORMATION,
                            icon = Icons.AutoMirrored.Filled.Notes,
                            subtitle = "Add custom fields and notes"
                        )
                        
                        // Custom fields
                        customFields.forEach { (key, value) ->
                            OutlinedTextField(
                                value = value,
                                onValueChange = { viewModel.updateCustomField(key, it) },
                                label = { Text(key) },
                                trailingIcon = {
                                    IconButton(
                                        onClick = { viewModel.removeCustomField(key) }
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
                }
            }
        }

        // Privacy notice
        item {
            val privacyIndex = if (isTextualCard) 5 else 4
            AnimatedSection(index = privacyIndex) {
                PrivacyNoticeCard()
            }
        }
    }

    // Gradient Picker Dialog
    GradientPickerDialog(
        isVisible = showGradientPicker,
        cardType = selectedCardType ?: CardType.Credit,
        selectedGradient = selectedGradient,
        onGradientSelected = { gradient ->
            viewModel.updateGradient(gradient)
            viewModel.hideGradientPicker()
        },
        onDismiss = { viewModel.hideGradientPicker() }
    )
}

/**
 * Appearance section for customizing card gradient
 */
@Composable
private fun AppearanceSection(
    cardType: CardType,
    selectedGradient: CardGradient?,
    onEditGradient: () -> Unit,
    modifier: Modifier = Modifier,
    showHeader: Boolean = true
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AppConstants.Dimensions.SPACING_MEDIUM)
    ) {
        if (showHeader) {
            Text(
                text = "Appearance",
                style = MaterialTheme.typography.titleMedium
            )
        }
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .clickable { onEditGradient() },
            shape = RoundedCornerShape(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = createGradientBrush(selectedGradient ?: Card.getDefaultGradientForType(cardType))
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
                            text = selectedGradient?.name ?: "Default Gradient",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White
                        )
                        Text(
                            text = "Tap to customize",
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