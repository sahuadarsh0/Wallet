package com.technitedminds.wallet.presentation.screens.addcard

import android.app.Activity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Contactless
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Scanner
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.technitedminds.wallet.domain.model.Card
import com.technitedminds.wallet.domain.model.CardGradient
import com.technitedminds.wallet.domain.model.CardType
import com.technitedminds.wallet.domain.model.GradientDirection
import com.technitedminds.wallet.presentation.components.animation.CardFront
import com.technitedminds.wallet.presentation.components.animation.EnhancedSlideInItem
import com.technitedminds.wallet.presentation.components.animation.liquidPress
import com.technitedminds.wallet.presentation.components.common.AnimatedSectionHeader
import com.technitedminds.wallet.presentation.components.common.CardTypeSelector
import com.technitedminds.wallet.presentation.components.common.CvvEntryDialog
import com.technitedminds.wallet.presentation.components.common.GlassPremiumCard
import com.technitedminds.wallet.presentation.components.common.GradientPickerDialog
import com.technitedminds.wallet.presentation.components.common.LoadingOverlay
import com.technitedminds.wallet.presentation.components.common.NfcScanningSheet
import com.technitedminds.wallet.presentation.components.common.PremiumButton
import com.technitedminds.wallet.presentation.components.common.PremiumButtonVariant
import com.technitedminds.wallet.presentation.components.common.PremiumCard
import com.technitedminds.wallet.presentation.components.common.PremiumTextField
import com.technitedminds.wallet.presentation.components.common.StepProgressIndicator
import com.technitedminds.wallet.presentation.components.common.gradientShadow
import com.technitedminds.wallet.presentation.constants.AppConstants
import com.technitedminds.wallet.ui.theme.Glass
import com.technitedminds.wallet.ui.theme.GlassSurface
import com.technitedminds.wallet.ui.theme.WalletSpring
import com.technitedminds.wallet.ui.theme.gradientContrastText
import java.io.File

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
    nfcCardReaderManager: NfcCardReaderManager? = null,
    viewModel: AddCardViewModel = hiltViewModel()
) {
    val currentStep by viewModel.currentStep.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isFormValid by viewModel.isFormValid.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context as? Activity
    val nfcReadState by nfcCardReaderManager?.state?.collectAsStateWithLifecycle()
        ?: remember { mutableStateOf(NfcReadState.Idle as NfcReadState) }

    LaunchedEffect(nfcReadState) {
        when (val state = nfcReadState) {
            is NfcReadState.Success -> viewModel.onNfcCardRead(state.data)
            is NfcReadState.Error -> viewModel.onNfcError(state.message)
            else -> {}
        }
    }

    DisposableEffect(Unit) {
        onDispose { activity?.let { nfcCardReaderManager?.stopReading(it) } }
    }

    LaunchedEffect(capturedFrontImagePath, capturedBackImagePath, capturedExtractedData) {
        if (capturedFrontImagePath != null) {
            val currentFrontPath = viewModel.uiState.value.frontImagePath
            if (capturedFrontImagePath != currentFrontPath) {
                viewModel.setFrontImagePath(capturedFrontImagePath)
                if (capturedExtractedData.isNotEmpty()) {
                    viewModel.setExtractedData(capturedExtractedData)
                }
            }
            if (capturedBackImagePath != null && capturedBackImagePath != viewModel.uiState.value.backImagePath) {
                viewModel.setBackImagePath(capturedBackImagePath)
                if (capturedExtractedData.isNotEmpty()) {
                    viewModel.setExtractedData(capturedExtractedData)
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is AddCardEvent.CardSaved -> onCardSaved(event.card)
            }
        }
    }

    // Derive ambient gradient from selected card type
    val selectedCardType by viewModel.selectedCardType.collectAsStateWithLifecycle()
    val selectedGradientForBg by viewModel.selectedGradient.collectAsStateWithLifecycle()
    val typeGradient = remember(selectedCardType, selectedGradientForBg) {
        selectedGradientForBg ?: selectedCardType?.let { Card.getDefaultGradientForType(it) }
    }
    val ambientStart = remember(typeGradient) {
        try { typeGradient?.let { Color(android.graphics.Color.parseColor(it.startColor)) } ?: Color.Transparent }
        catch (e: Exception) { Color.Transparent }
    }
    val ambientEnd = remember(typeGradient) {
        try { typeGradient?.let { Color(android.graphics.Color.parseColor(it.endColor)) } ?: Color.Transparent }
        catch (e: Exception) { Color.Transparent }
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            ambientStart.copy(alpha = 0.05f),
                            Color.Transparent,
                            ambientEnd.copy(alpha = 0.03f),
                        )
                    )
                )
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                StepProgressIndicator(
                    currentStep = currentStep.ordinal,
                    modifier = Modifier.padding(16.dp)
                )

                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = {
                        val isForward = targetState.ordinal > initialState.ordinal
                        if (isForward) {
                            slideInHorizontally(
                                initialOffsetX = { it },
                                animationSpec = WalletSpring.gentle()
                            ) + fadeIn(tween(300)) togetherWith slideOutHorizontally(
                                targetOffsetX = { -it / 3 },
                                animationSpec = WalletSpring.gentle()
                            ) + fadeOut(tween(200))
                        } else {
                            slideInHorizontally(
                                initialOffsetX = { -it },
                                animationSpec = WalletSpring.gentle()
                            ) + fadeIn(tween(300)) togetherWith slideOutHorizontally(
                                targetOffsetX = { it / 3 },
                                animationSpec = WalletSpring.gentle()
                            ) + fadeOut(tween(200))
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    label = AppConstants.UIText.STEP_CONTENT_LABEL
                ) { step ->
                    when (step) {
                        AddCardStep.TYPE_SELECTION -> TypeSelectionStep(
                            onCardTypeSelected = viewModel::selectCardType,
                            modifier = Modifier.fillMaxSize()
                        )
                        AddCardStep.CAMERA_CAPTURE -> CameraCaptureStep(
                            cardType = viewModel.selectedCardType.collectAsStateWithLifecycle().value ?: CardType.Credit,
                            captureState = viewModel.captureState.collectAsStateWithLifecycle().value,
                            frontImagePath = uiState.frontImagePath,
                            backImagePath = uiState.backImagePath,
                            onCameraCapture = onCameraCapture,
                            onExtractedData = viewModel::setExtractedData,
                            onFrontImagePath = viewModel::setFrontImagePath,
                            onBackImagePath = viewModel::setBackImagePath,
                            onOpenCameraForFront = viewModel::openCameraForFront,
                            onOpenCameraForBack = viewModel::openCameraForBack,
                            isNfcAvailable = activity?.let { nfcCardReaderManager?.isNfcAvailable(it) } == true,
                            onNfcScan = {
                                viewModel.startNfcScan()
                                activity?.let { nfcCardReaderManager?.startReading(it) }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                        AddCardStep.FORM_DETAILS -> FormDetailsStep(
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }

    uiState.error?.let { error ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("Error") },
            text = { Text(error) },
            confirmButton = {
                Button(onClick = { viewModel.clearError() }) { Text("OK") }
            }
        )
    }

    LoadingOverlay(isVisible = uiState.isSaving, text = AppConstants.UIText.SAVING_CARD)

    if (uiState.showNfcScanning) {
        NfcScanningSheet(
            nfcReadState = nfcReadState,
            onCancel = {
                viewModel.cancelNfcScan()
                activity?.let { nfcCardReaderManager?.stopReading(it) }
            },
            onRetry = {
                nfcCardReaderManager?.resetState()
                activity?.let { nfcCardReaderManager?.startReading(it) }
            },
        )
    }

    if (uiState.showCvvDialog) {
        CvvEntryDialog(
            cardScheme = uiState.nfcCardScheme,
            maskedCardNumber = uiState.nfcMaskedNumber,
            onCvvConfirmed = viewModel::onCvvEntered,
            onSkip = viewModel::skipCvvEntry,
        )
    }
}

// ─── Top Bar ─────────────────────────────────────────────────────────────────

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
            IconButton(onClick = if (canGoBack) onPreviousStep else onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, AppConstants.ContentDescriptions.BACK)
            }
        },
        actions = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.Close, AppConstants.DialogText.CANCEL_BUTTON)
            }
        },
        modifier = modifier
    )
}

// ─── Bottom Bar (GlassSurface + PremiumButton) ──────────────────────────────

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
    if (currentStep == AddCardStep.TYPE_SELECTION) return

    GlassSurface(
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppConstants.Dimensions.PADDING_LARGE),
            horizontalArrangement = Arrangement.spacedBy(AppConstants.Dimensions.SPACING_MEDIUM)
        ) {
            when (currentStep) {
                AddCardStep.TYPE_SELECTION -> {}
                AddCardStep.CAMERA_CAPTURE -> {
                    PremiumButton(
                        onClick = onSkipCamera,
                        text = AppConstants.UIText.SKIP_CAMERA,
                        variant = PremiumButtonVariant.Secondary,
                        modifier = Modifier.weight(1f)
                    )
                    PremiumButton(
                        onClick = onNextStep,
                        text = AppConstants.UIText.CONTINUE,
                        variant = PremiumButtonVariant.Primary,
                        modifier = Modifier.weight(1f)
                    )
                }
                AddCardStep.FORM_DETAILS -> {
                    PremiumButton(
                        onClick = onSaveCard,
                        text = AppConstants.UIText.SAVE_CARD,
                        enabled = isFormValid && !isLoading,
                        icon = if (isLoading) null else Icons.Default.Check,
                        variant = PremiumButtonVariant.Primary,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

// ─── Step 1: Type Selection ──────────────────────────────────────────────────

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
        EnhancedSlideInItem(visible = true, index = 0, baseDelay = 80) {
            AnimatedSectionHeader(
                title = AppConstants.UIText.ADD_CARD_TYPE_PROMPT,
                icon = Icons.Default.CreditCard,
                subtitle = "Choose the type of card you want to store"
            )
        }

        Spacer(modifier = Modifier.height(AppConstants.Dimensions.SPACING_LARGE))

        EnhancedSlideInItem(visible = true, index = 1, baseDelay = 80) {
            CardTypeSelector(
                onTypeSelected = onCardTypeSelected,
                showCustomOption = true,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

// ─── Step 2: Camera Capture ──────────────────────────────────────────────────

@Composable
private fun CameraCaptureStep(
    cardType: CardType,
    captureState: CaptureState,
    frontImagePath: String?,
    backImagePath: String?,
    onCameraCapture: (CardType) -> Unit,
    onExtractedData: (Map<String, String>) -> Unit,
    onFrontImagePath: (String) -> Unit,
    onBackImagePath: (String) -> Unit,
    onOpenCameraForFront: () -> Unit,
    onOpenCameraForBack: () -> Unit,
    isNfcAvailable: Boolean = false,
    onNfcScan: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val showNfcOption = isNfcAvailable && cardType.supportsNfc()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(AppConstants.Dimensions.PADDING_LARGE),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppConstants.Dimensions.SPACING_LARGE)
    ) {
        Spacer(modifier = Modifier.height(AppConstants.Dimensions.SPACING_LARGE))

        // Capture progress panel
        EnhancedSlideInItem(visible = true, index = 0, baseDelay = 80) {
            CaptureProgressPanel(
                captureState = captureState,
                frontImagePath = frontImagePath,
                backImagePath = backImagePath,
                isOcrCapable = cardType.supportsOCR(),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Contextual instruction
        EnhancedSlideInItem(visible = true, index = 1, baseDelay = 80) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = when (captureState) {
                        CaptureState.AWAITING_FRONT -> String.format(AppConstants.UIText.POSITION_FRONT_OF_CARD_PROMPT, cardType.getDisplayName())
                        CaptureState.FRONT_CAPTURED -> String.format(AppConstants.UIText.POSITION_BACK_OF_CARD_PROMPT, cardType.getDisplayName())
                        CaptureState.BACK_CAPTURED -> String.format(AppConstants.UIText.CAPTURE_CARD_PROMPT_TITLE, cardType.getDisplayName())
                    },
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(AppConstants.Dimensions.SPACING_SMALL))

                Text(
                    text = when {
                        captureState == CaptureState.AWAITING_FRONT && cardType.supportsOCR() ->
                            "We'll automatically extract card details from the front image."
                        captureState == CaptureState.AWAITING_FRONT ->
                            "Take a clear photo of the front of your ${cardType.getDisplayName().lowercase()}."
                        captureState == CaptureState.FRONT_CAPTURED && cardType.supportsOCR() ->
                            "Now capture the back side. We'll extract the CVV and other details."
                        captureState == CaptureState.FRONT_CAPTURED ->
                            "Now capture the back side of your ${cardType.getDisplayName().lowercase()}."
                        else -> AppConstants.UIText.CAPTURE_CARD_PROMPT_SUBTITLE
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Action buttons
        EnhancedSlideInItem(visible = true, index = 2, baseDelay = 80) {
            if (showNfcOption) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppConstants.Dimensions.SPACING_MEDIUM),
                ) {
                    PremiumButton(
                        onClick = { onCameraCapture(cardType) },
                        text = when (captureState) {
                            CaptureState.AWAITING_FRONT -> AppConstants.UIText.OPEN_CAMERA
                            CaptureState.FRONT_CAPTURED -> AppConstants.UIText.CAPTURE_BACK
                            CaptureState.BACK_CAPTURED -> AppConstants.UIText.OPEN_CAMERA
                        },
                        icon = Icons.Default.CameraAlt,
                        variant = PremiumButtonVariant.Primary,
                        modifier = Modifier.weight(1f),
                    )
                    PremiumButton(
                        onClick = onNfcScan,
                        text = AppConstants.NfcText.SCAN_WITH_NFC,
                        icon = Icons.Default.Contactless,
                        variant = PremiumButtonVariant.Secondary,
                        modifier = Modifier.weight(1f),
                    )
                }
            } else {
                PremiumButton(
                    onClick = { onCameraCapture(cardType) },
                    text = when (captureState) {
                        CaptureState.AWAITING_FRONT -> "Capture Front"
                        CaptureState.FRONT_CAPTURED -> "Capture Back"
                        CaptureState.BACK_CAPTURED -> "Recapture"
                    },
                    icon = Icons.Default.CameraAlt,
                    variant = PremiumButtonVariant.Primary,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        Spacer(modifier = Modifier.height(AppConstants.Dimensions.SPACING_MEDIUM))
    }
}

// ─── Capture Progress Panel ──────────────────────────────────────────────────

@Composable
private fun CaptureProgressPanel(
    captureState: CaptureState,
    frontImagePath: String?,
    backImagePath: String?,
    isOcrCapable: Boolean = false,
    modifier: Modifier = Modifier
) {
    GlassPremiumCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // OCR indicator for smart cards
            if (isOcrCapable) {
                GlassSurface(
                    shape = RoundedCornerShape(Glass.PillCornerRadius),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "OCR available",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "AI will extract card details",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CaptureSlot(
                    label = "Front",
                    imagePath = frontImagePath,
                    isCaptured = captureState != CaptureState.AWAITING_FRONT,
                    isActive = captureState == CaptureState.AWAITING_FRONT,
                    modifier = Modifier.weight(1f)
                )
                CaptureSlot(
                    label = "Back",
                    imagePath = backImagePath,
                    isCaptured = captureState == CaptureState.BACK_CAPTURED,
                    isActive = captureState == CaptureState.FRONT_CAPTURED,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun CaptureSlot(
    label: String,
    imagePath: String?,
    isCaptured: Boolean,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val borderColor = when {
        isCaptured -> MaterialTheme.colorScheme.primary
        isActive -> MaterialTheme.colorScheme.outline
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    }
    val contentAlpha = if (isCaptured || isActive) 1f else 0.4f

    Card(
        modifier = modifier.height(120.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = if (isCaptured) 2.dp else 1.5.dp,
            color = borderColor
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (isCaptured && imagePath != null) {
                val imageFile = File(imagePath)
                if (imageFile.exists()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(imageFile)
                            .crossfade(true)
                            .build(),
                        contentDescription = "$label captured",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    // Green check overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Captured",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Capture photo",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha)
                    )
                }
            }
        }
    }
}

// ─── Step 3: Form Details ────────────────────────────────────────────────────

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

    var cardNumber by remember { mutableStateOf(extractedData["cardNumber"] ?: "") }
    var expiryDate by remember { mutableStateOf(extractedData["expiryDate"] ?: "") }
    var cardholderName by remember { mutableStateOf(extractedData["cardholderName"] ?: "") }
    var cvv by remember { mutableStateOf(extractedData["cvv"] ?: "") }

    LaunchedEffect(extractedData) {
        cardNumber = extractedData["cardNumber"] ?: cardNumber
        expiryDate = extractedData["expiryDate"] ?: expiryDate
        cardholderName = extractedData["cardholderName"] ?: cardholderName
        cvv = extractedData["cvv"] ?: cvv
    }

    val hasOCRData = extractedData.isNotEmpty()
    val isTextualCard = selectedCardType?.supportsOCR() == true

    // Build live preview card from current form state
    val previewCard = remember(cardName, selectedCardType, cardNumber, expiryDate, cardholderName, selectedGradient, selectedCategory) {
        Card(
            id = "preview",
            name = cardName.ifBlank { "Your Card" },
            type = selectedCardType ?: CardType.Credit,
            categoryId = selectedCategory ?: "",
            frontImagePath = "",
            backImagePath = "",
            extractedData = buildMap {
                if (cardNumber.isNotBlank()) put("cardNumber", cardNumber)
                if (expiryDate.isNotBlank()) put("expiryDate", expiryDate)
                if (cardholderName.isNotBlank()) put("cardholderName", cardholderName)
            },
            customGradient = selectedGradient,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .padding(AppConstants.Dimensions.PADDING_LARGE),
        verticalArrangement = Arrangement.spacedBy(AppConstants.Dimensions.SPACING_LARGE)
    ) {
        // Live card preview (hero element)
        item {
            EnhancedSlideInItem(visible = true, index = 0, baseDelay = 80) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CreditCard,
                            contentDescription = "Live preview",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Live Preview",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    val gradientForShadow = selectedGradient ?: Card.getDefaultGradientForType(selectedCardType ?: CardType.Credit)
                    val shadowColors = listOf(
                        Color(android.graphics.Color.parseColor(gradientForShadow.startColor)),
                        Color(android.graphics.Color.parseColor(gradientForShadow.endColor)),
                    )
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .gradientShadow(
                                colors = shadowColors,
                                shadowElevation = 8.dp,
                                cornerRadius = 12.dp,
                            ),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        CardFront(
                            card = previewCard,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }

        // OCR status pill (for textual cards)
        if (isTextualCard) {
            item {
                EnhancedSlideInItem(visible = true, index = 1, baseDelay = 80) {
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
                                imageVector = if (hasOCRData) Icons.Default.AutoAwesome else Icons.Default.Edit,
                                contentDescription = if (hasOCRData) "OCR detected" else "Manual entry",
                                tint = if (hasOCRData) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (hasOCRData) "${extractedData.size} fields auto-detected" else "Enter card details manually",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (hasOCRData) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Card Details section -- GlassPremiumCard (primary section)
        item {
            val idx = if (isTextualCard) 2 else 1
            EnhancedSlideInItem(visible = true, index = idx, baseDelay = 80) {
                GlassPremiumCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        AnimatedSectionHeader(
                            title = "Card Details",
                            icon = Icons.Default.CreditCard,
                            subtitle = "Name and category for your card"
                        )
                        PremiumTextField(
                            value = cardName,
                            onValueChange = viewModel::updateCardName,
                            label = AppConstants.UIText.CARD_NAME_LABEL,
                            leadingIcon = Icons.Default.Badge,
                            modifier = Modifier.fillMaxWidth()
                        )
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

        // Card Information section (OCR) -- standard PremiumCard (secondary)
        if (isTextualCard) {
            item {
                EnhancedSlideInItem(visible = true, index = 3, baseDelay = 80) {
                    PremiumCard(
                        elevation = CardDefaults.cardElevation(defaultElevation = AppConstants.Dimensions.CARD_ELEVATION_DEFAULT)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            AnimatedSectionHeader(
                                title = AppConstants.UIText.CARD_INFORMATION,
                                icon = Icons.Default.Shield,
                                subtitle = "Card number, expiry and security details"
                            )

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
                                        Icon(Icons.Default.AutoAwesome, AppConstants.UIText.AUTO_DETECTED,
                                            tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(AppConstants.Dimensions.ICON_SIZE_MEDIUM))
                                    }
                                } else null,
                                isError = uiState.cardNumberError != null,
                                errorMessage = uiState.cardNumberError,
                                modifier = Modifier.fillMaxWidth()
                            )

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
                                        Icon(Icons.Default.AutoAwesome, AppConstants.UIText.AUTO_DETECTED,
                                            tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(AppConstants.Dimensions.ICON_SIZE_MEDIUM))
                                    }
                                } else null,
                                isError = uiState.expiryDateError != null,
                                errorMessage = uiState.expiryDateError,
                                modifier = Modifier.fillMaxWidth()
                            )

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
                                        Icon(Icons.Default.AutoAwesome, AppConstants.UIText.AUTO_DETECTED,
                                            tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(AppConstants.Dimensions.ICON_SIZE_MEDIUM))
                                    }
                                } else null,
                                isError = uiState.cardholderNameError != null,
                                errorMessage = uiState.cardholderNameError,
                                modifier = Modifier.fillMaxWidth()
                            )

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
                                        Icon(Icons.Default.AutoAwesome, AppConstants.UIText.AUTO_DETECTED,
                                            tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(AppConstants.Dimensions.ICON_SIZE_MEDIUM))
                                    }
                                } else null,
                                isError = uiState.cvvError != null,
                                errorMessage = uiState.cvvError,
                                modifier = Modifier.fillMaxWidth()
                            )

                            if (hasOCRData) {
                                PremiumButton(
                                    onClick = {
                                        cardNumber = ""
                                        expiryDate = ""
                                        cardholderName = ""
                                        cvv = ""
                                        viewModel.clearOCRData()
                                    },
                                    text = AppConstants.UIText.CLEAR_ALL_AND_ENTER_MANUALLY,
                                    icon = Icons.Default.Clear,
                                    variant = PremiumButtonVariant.Secondary,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }

        // Appearance section -- Unwrapped (gradient preview is its own visual)
        item {
            val idx = if (isTextualCard) 4 else 2
            EnhancedSlideInItem(visible = true, index = idx, baseDelay = 80) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                        showHeader = false
                    )
                }
            }
        }

        // Additional Details -- PremiumCard with surfaceVariant (tertiary)
        item {
            val idx = if (isTextualCard) 5 else 3
            EnhancedSlideInItem(visible = true, index = idx, baseDelay = 80) {
                PremiumCard(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = AppConstants.Dimensions.CARD_ELEVATION_DEFAULT)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AnimatedSectionHeader(
                            title = AppConstants.UIText.ADDITIONAL_INFORMATION,
                            icon = Icons.Default.Description,
                            subtitle = "Add custom fields and notes"
                        )

                        customFields.forEach { (key, value) ->
                            PremiumTextField(
                                value = value,
                                onValueChange = { viewModel.updateCustomField(key, it) },
                                label = key,
                                trailingIcon = {
                                    IconButton(onClick = { viewModel.removeCustomField(key) }) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = AppConstants.UIText.REMOVE_FIELD,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        PremiumButton(
                            onClick = {
                                viewModel.addCustomField(AppConstants.UIText.NOTES_FIELD_LABEL, AppConstants.UIText.EMPTY_STRING)
                            },
                            text = AppConstants.UIText.ADD_FIELD,
                            icon = Icons.Default.Add,
                            variant = PremiumButtonVariant.Secondary,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Privacy notice -- lightweight GlassSurface pill
        item {
            val idx = if (isTextualCard) 6 else 4
            EnhancedSlideInItem(visible = true, index = idx, baseDelay = 80) {
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
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Privacy",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "All data stays on your device. Nothing is sent to servers.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

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

// ─── Appearance Section ──────────────────────────────────────────────────────

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

        val gradientToShow = selectedGradient ?: Card.getDefaultGradientForType(cardType)
        val textColor = gradientContrastText(gradientToShow.startColor, gradientToShow.endColor)
        val shadowColors = listOf(
            Color(android.graphics.Color.parseColor(gradientToShow.startColor)),
            Color(android.graphics.Color.parseColor(gradientToShow.endColor)),
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .gradientShadow(colors = shadowColors, shadowElevation = 8.dp, cornerRadius = 12.dp)
                .liquidPress()
                .clickable { onEditGradient() },
            shape = RoundedCornerShape(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(brush = createGradientBrush(gradientToShow)),
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
                            text = selectedGradient?.name ?: "Default Gradient",
                            style = MaterialTheme.typography.titleSmall,
                            color = textColor,
                        )
                        Text(
                            text = "Tap to customize",
                            style = MaterialTheme.typography.bodySmall,
                            color = textColor.copy(alpha = 0.8f),
                        )
                    }
                    Icon(Icons.Default.Edit, "Edit gradient", tint = textColor)
                }
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
            start = Offset(0f, 0f), end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        )
        GradientDirection.DiagonalTopRightToBottomLeft -> Brush.linearGradient(
            colors = listOf(startColor, endColor),
            start = Offset(Float.POSITIVE_INFINITY, 0f), end = Offset(0f, Float.POSITIVE_INFINITY)
        )
    }
}

private fun formatCardNumberInput(input: String): String {
    val digitsOnly = input.filter { it.isDigit() }
    return digitsOnly.chunked(4).joinToString(" ").take(19)
}

private fun formatExpiryInput(input: String): String {
    val digitsOnly = input.filter { it.isDigit() }
    return when {
        digitsOnly.length <= 2 -> digitsOnly
        digitsOnly.length <= 4 -> "${digitsOnly.take(2)}/${digitsOnly.drop(2)}"
        else -> "${digitsOnly.take(2)}/${digitsOnly.drop(2).take(2)}"
    }
}

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
            onValueChange = {},
            readOnly = true,
            label = { Text(AppConstants.UIText.CATEGORY_LABEL) },
            leadingIcon = { Icon(Icons.Default.Category, null) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
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
