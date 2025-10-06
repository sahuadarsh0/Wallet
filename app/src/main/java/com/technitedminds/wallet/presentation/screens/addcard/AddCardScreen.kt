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
                label = "step_content"
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
            viewModel.clearError()
        }
    }

    // Loading overlay
    LoadingOverlay(
        isVisible = uiState.isLoading,
        text = "Saving card..."
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
                    AddCardStep.TYPE_SELECTION -> "Select Card Type"
                    AddCardStep.CAMERA_CAPTURE -> "Capture Card Images"
                    AddCardStep.FORM_DETAILS -> "Card Details"
                }
            )
        },
        navigationIcon = {
            IconButton(
                onClick = if (canGoBack) onPreviousStep else onNavigateBack
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        actions = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cancel"
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
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
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
                        Text("Skip Camera")
                    }
                    Button(
                        onClick = onNextStep,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Continue")
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
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text("Save Card")
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
        "Type" to AddCardStep.TYPE_SELECTION,
        "Capture" to AddCardStep.CAMERA_CAPTURE,
        "Details" to AddCardStep.FORM_DETAILS
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
                    modifier = Modifier.size(32.dp),
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
                                modifier = Modifier.size(16.dp)
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

                Spacer(modifier = Modifier.height(4.dp))

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
                        .padding(horizontal = 8.dp),
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
            .padding(16.dp)
    ) {
        Text(
            text = "What type of card would you like to add?",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 24.dp)
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
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CameraAlt,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Capture Your ${cardType.getDisplayName()}",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Take photos of both sides of your card for the best experience. We'll automatically extract the card information.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { onCameraCapture(cardType) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Open Camera")
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
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Card Details",
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
                label = { Text("Card Name") },
                placeholder = { Text("My ${selectedCardType?.getDisplayName() ?: "Card"}") },
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
                onCategorySelected = { id -> id?.let(viewModel::selectCategory) },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Card Information Section (for textual cards only)
        if (isTextualCard) {
            item {
                Text(
                    text = "Card Information",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                // Card Number
                OutlinedTextField(
                    value = cardNumber,
                    onValueChange = { 
                        cardNumber = formatCardNumberInput(it)
                    },
                    label = { Text("Card Number") },
                    placeholder = { Text("1234 5678 9012 3456") },
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
                                contentDescription = "Auto-detected",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
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
                    label = { Text("Expiry Date") },
                    placeholder = { Text("MM/YY") },
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
                                contentDescription = "Auto-detected",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
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
                    label = { Text("Cardholder Name") },
                    placeholder = { Text("JOHN DOE") },
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
                                contentDescription = "Auto-detected",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
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
                    label = { Text("CVV") },
                    placeholder = { Text("123") },
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
                                contentDescription = "Auto-detected",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
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
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Clear All & Enter Manually")
                    }
                }
            }
        }

        item {
            // Custom fields header
            Text(
                text = "Additional Information",
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
                            contentDescription = "Remove field"
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
                    viewModel.addCustomField("notes", "")
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

        // Privacy notice
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Privacy & Security",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Text(
                        text = "• All data stays on your device\n• No information is sent to servers\n• You can edit or clear any field",
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
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Text Recognition Complete",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Text(
                    text = "Extracted $extractedFieldCount field${if (extractedFieldCount != 1) "s" else ""} from your ${cardType.getDisplayName().lowercase()}",
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
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Manual Entry",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                
                Text(
                    text = "Please enter your ${cardType.getDisplayName().lowercase()} details manually",
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
            label = { Text("Category") },
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