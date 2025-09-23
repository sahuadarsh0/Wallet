package com.technitedminds.wallet.presentation.screens.addcard

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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.technitedminds.wallet.domain.model.Card
import com.technitedminds.wallet.domain.model.CardType
import com.technitedminds.wallet.presentation.components.common.*
import com.technitedminds.wallet.presentation.screens.addcard.AddCardStep
import com.technitedminds.wallet.presentation.screens.addcard.AddCardEvent

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
    viewModel: AddCardViewModel = hiltViewModel()
) {
    val currentStep by viewModel.currentStep.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isFormValid by viewModel.isFormValid.collectAsStateWithLifecycle()

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
                        onClick = onNextStep,
                        enabled = isFormValid && !isLoading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isLoading) {
                            CompactLoadingIndicator(
                                modifier = Modifier.size(16.dp)
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
                Divider(
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
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "What type of card would you like to add?",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        CardTypeSelector(
            selectedType = CardType.Credit, // Dummy selection for UI
            onTypeSelected = onCardTypeSelected,
            showCustomOption = true
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
 * Form details step
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Card Details",
            style = MaterialTheme.typography.headlineSmall
        )

        // Card name
        OutlinedTextField(
            value = cardName,
            onValueChange = viewModel::updateCardName,
            label = { Text("Card Name") },
            placeholder = { Text("My Credit Card") },
            modifier = Modifier.fillMaxWidth()
        )

        // Category selection
        CategoryDropdown(
            categories = categories,
            selectedCategoryId = selectedCategory,
            onCategorySelected = { id -> id?.let(viewModel::selectCategory) },
            modifier = Modifier.fillMaxWidth()
        )

        // Extracted data (if available)
        if (extractedData.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Extracted Information",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    extractedData.forEach { (key, value) ->
                        Text(
                            text = "$key: $value",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }

        // Custom fields
        Text(
            text = "Additional Information",
            style = MaterialTheme.typography.titleMedium
        )

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
                            contentDescription = "Remove field"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

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
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
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