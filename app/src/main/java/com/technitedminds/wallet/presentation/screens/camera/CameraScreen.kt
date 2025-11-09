package com.technitedminds.wallet.presentation.screens.camera

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.technitedminds.wallet.domain.model.CardType
import com.technitedminds.wallet.presentation.components.camera.*
import com.technitedminds.wallet.presentation.components.common.LoadingIndicator
import java.io.File
import androidx.compose.ui.platform.LocalContext
import com.technitedminds.wallet.presentation.constants.AppConstants
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource

/**
 * Dedicated camera screen for card capture with step-by-step flow
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(
    cardType: CardType,
    onNavigateBack: () -> Unit,
    onImagesConfirmed: (frontImagePath: String, backImagePath: String?, extractedData: Map<String, String>) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CameraViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentStep by viewModel.currentStep.collectAsStateWithLifecycle()
    val aspectRatio by viewModel.aspectRatio.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Initialize with card type
    LaunchedEffect(cardType) {
        viewModel.setCardType(cardType)
    }

    // Handle events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CameraEvent.ImagesReady -> {
                    onImagesConfirmed(
                        event.frontImage.absolutePath, 
                        event.backImage.absolutePath, 
                        uiState.extractedData
                    )
                }
                is CameraEvent.NavigateBack -> {
                    onNavigateBack()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            CameraTopBar(
                currentStep = currentStep,
                cardType = cardType,
                onNavigateBack = onNavigateBack,
                onAspectRatioChange = viewModel::setAspectRatio,
                currentAspectRatio = aspectRatio
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (currentStep) {
                CameraStep.PERMISSION_REQUEST -> {
                    CameraPermissionStep(
                        onPermissionGranted = viewModel::onPermissionGranted,
                        onPermissionDenied = onNavigateBack
                    )
                }
                CameraStep.FRONT_CAPTURE -> {
                    CameraCaptureStep(
                        cardType = cardType,
                        aspectRatio = aspectRatio,
                        isCapturingFront = true,
                        onImageCaptured = viewModel::setFrontImage,
                        onError = viewModel::setError
                    )
                }
                CameraStep.FRONT_PREVIEW -> {
                    ImagePreviewStep(
                        imageFile = uiState.frontImage,
                        title = AppConstants.UIText.FRONT_SIDE_PREVIEW_TITLE,
                        onRetake = viewModel::retakeFrontImage,
                        onConfirm = viewModel::confirmFrontImage
                    )
                }
                CameraStep.BACK_CAPTURE -> {
                    CameraCaptureStep(
                        cardType = cardType,
                        aspectRatio = aspectRatio,
                        isCapturingFront = false,
                        onImageCaptured = viewModel::setBackImage,
                        onError = viewModel::setError
                    )
                }
                CameraStep.BACK_PREVIEW -> {
                    ImagePreviewStep(
                        imageFile = uiState.backImage,
                        title = AppConstants.UIText.BACK_SIDE_PREVIEW_TITLE,
                        onRetake = viewModel::retakeBackImage,
                        onConfirm = viewModel::confirmBackImage
                    )
                }
                CameraStep.PROCESSING -> {
                    ProcessingStep(
                        cardType = cardType,
                        progress = uiState.processingProgress
                    )
                }
            }

            // Error handling
            uiState.error?.let { error ->
                ErrorSnackbar(
                    error = error,
                    onDismiss = viewModel::clearError,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}

/**
 * Camera top bar with aspect ratio selector
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CameraTopBar(
    currentStep: CameraStep,
    cardType: CardType,
    onNavigateBack: () -> Unit,
    onAspectRatioChange: (CardAspectRatio) -> Unit,
    currentAspectRatio: CardAspectRatio,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = when (currentStep) {
                        CameraStep.PERMISSION_REQUEST -> "Camera Permission"
                        CameraStep.FRONT_CAPTURE -> "Capture Front"
                        CameraStep.FRONT_PREVIEW -> "Front Preview"
                        CameraStep.BACK_CAPTURE -> "Capture Back"
                        CameraStep.BACK_PREVIEW -> "Back Preview"
                        CameraStep.PROCESSING -> "Processing"
                    },
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = cardType.getDisplayName(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = AppConstants.ContentDescriptions.BACK
                )
            }
        },
        actions = {
            // Aspect ratio selector for capture steps
            if (currentStep == CameraStep.FRONT_CAPTURE || currentStep == CameraStep.BACK_CAPTURE) {
                AspectRatioSelector(
                    currentRatio = currentAspectRatio,
                    onRatioSelected = onAspectRatioChange
                )
            }
        },
        modifier = modifier
    )
}

/**
 * Aspect ratio selector dropdown
 */
@Composable
private fun AspectRatioSelector(
    currentRatio: CardAspectRatio,
    onRatioSelected: (CardAspectRatio) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Default.AspectRatio,
                contentDescription = "Change aspect ratio"
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            CardAspectRatio.values().forEach { ratio ->
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (ratio == currentRatio) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Spacer(modifier = Modifier.size(16.dp))
                            }
                            Text(ratio.displayName)
                        }
                    },
                    onClick = {
                        onRatioSelected(ratio)
                        expanded = false
                    }
                )
            }
        }
    }
}

/**
 * Camera permission request step
 */
@Composable
private fun CameraPermissionStep(
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit,
    modifier: Modifier = Modifier
) {
    CameraPermission(
        onPermissionGranted = onPermissionGranted,
        onPermissionDenied = onPermissionDenied,
        modifier = modifier
    )
}

/**
 * Camera capture step with live preview
 */
@Composable
private fun CameraCaptureStep(
    cardType: CardType,
    aspectRatio: CardAspectRatio,
    isCapturingFront: Boolean,
    onImageCaptured: (File) -> Unit,
    onError: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var captureButtonState by remember { mutableStateOf(CaptureButtonState.IDLE) }

    Box(modifier = modifier.fillMaxSize()) {
        // Camera preview with overlay
        CameraPreviewWithCapture(
            onImageCaptured = { file ->
                captureButtonState = CaptureButtonState.SUCCESS
                onImageCaptured(file)
            },
            onError = { exception ->
                captureButtonState = CaptureButtonState.ERROR
                onError(exception.message ?: "Camera error occurred")
            },
            aspectRatio = aspectRatio,
            captureButtonState = captureButtonState,
            onCaptureClick = {
                captureButtonState = CaptureButtonState.CAPTURING
            },
            modifier = Modifier.fillMaxSize()
        )

        // Instructions overlay
        CaptureInstructions(
            cardType = cardType,
            isCapturingFront = isCapturingFront,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp)
        )
    }
}

/**
 * Image preview step with retake/confirm options
 */
@Composable
private fun ImagePreviewStep(
    imageFile: File?,
    title: String,
    onRetake: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Image preview
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(12.dp)
        ) {
            imageFile?.let { file ->
                SimpleImagePreview(
                    imageFile = file,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onRetake,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(AppConstants.UIText.RETAKE_BUTTON_LABEL)
            }

            Button(
                onClick = onConfirm,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(AppConstants.DialogText.CONFIRM_BUTTON)
            }
        }
    }
}

/**
 * Processing step with progress indicator
 */
@Composable
private fun ProcessingStep(
    cardType: CardType,
    progress: Float,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        LoadingIndicator(
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = if (cardType.supportsOCR()) {
                AppConstants.UIText.PROCESSING_CARD_IMAGES_WITH_OCR_MESSAGE
            } else {
                AppConstants.UIText.PROCESSING_CARD_IMAGES_MESSAGE
            },
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = AppConstants.UIText.PROCESSING_TAKES_A_FEW_MOMENTS_MESSAGE,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Progress indicator
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "${(progress * 100).toInt()}%",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Capture instructions overlay
 */
@Composable
private fun CaptureInstructions(
    cardType: CardType,
    isCapturingFront: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isCapturingFront) {
                    String.format(AppConstants.UIText.POSITION_FRONT_OF_CARD_PROMPT, cardType.getDisplayName().lowercase())
                } else {
                    String.format(AppConstants.UIText.POSITION_BACK_OF_CARD_PROMPT, cardType.getDisplayName().lowercase())
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = AppConstants.UIText.CAPTURE_INSTRUCTIONS,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Error snackbar
 */
@Composable
private fun ErrorSnackbar(
    error: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }

            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = AppConstants.UIText.DISMISS_BUTTON_LABEL,
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}
/**

 * Simple image preview for camera screen
 */
@Composable
private fun SimpleImagePreview(
    imageFile: File,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (imageFile.exists()) {
            // Use Coil to load the actual image
            coil.compose.AsyncImage(
                model = coil.request.ImageRequest.Builder(context)
                    .data(imageFile)
                    .crossfade(true)
                    .build(),
                contentDescription = "Captured image preview",
                modifier = Modifier.fillMaxSize(),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                error = painterResource(id = android.R.drawable.ic_menu_gallery),
                placeholder = painterResource(id = android.R.drawable.ic_menu_gallery)
            )
        } else {
            // Show placeholder if file doesn't exist
            ImagePreviewPlaceholder(imageFile)
        }
    }
}

/**
 * Placeholder for image preview when image can't be loaded
 */
@Composable
private fun ImagePreviewPlaceholder(imageFile: File) {
    Card(
        modifier = Modifier.fillMaxSize(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Image,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = if (imageFile.exists()) "Image Preview" else AppConstants.UIText.IMAGE_NOT_FOUND_LABEL,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = imageFile.name,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (imageFile.exists()) {
                Text(
                    text = String.format(AppConstants.UIText.IMAGE_SIZE_LABEL, imageFile.length() / 1024),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}