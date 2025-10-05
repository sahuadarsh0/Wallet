package com.technitedminds.wallet.presentation.screens.camera

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.technitedminds.wallet.domain.model.CardType
import com.technitedminds.wallet.domain.usecase.ocr.ProcessCardImageUseCase
import com.technitedminds.wallet.presentation.components.camera.CardAspectRatio
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

/**
 * Camera steps for the capture flow
 */
enum class CameraStep {
    PERMISSION_REQUEST,
    FRONT_CAPTURE,
    FRONT_PREVIEW,
    BACK_CAPTURE,
    BACK_PREVIEW,
    PROCESSING
}

/**
 * Camera events
 */
sealed class CameraEvent {
    data class ImagesReady(val frontImage: File, val backImage: File) : CameraEvent()
    object NavigateBack : CameraEvent()
}

/**
 * Camera UI state
 */
data class CameraUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val frontImage: File? = null,
    val backImage: File? = null,
    val extractedData: Map<String, String> = emptyMap(),
    val processingProgress: Float = 0f
)

/**
 * ViewModel for camera screen handling card capture workflow
 */
@HiltViewModel
class CameraViewModel @Inject constructor(
    private val processCardImageUseCase: ProcessCardImageUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    private val _currentStep = MutableStateFlow(CameraStep.PERMISSION_REQUEST)
    val currentStep: StateFlow<CameraStep> = _currentStep.asStateFlow()

    private val _aspectRatio = MutableStateFlow(CardAspectRatio.CREDIT_CARD)
    val aspectRatio: StateFlow<CardAspectRatio> = _aspectRatio.asStateFlow()

    private val _cardType = MutableStateFlow<CardType?>(null)
    val cardType: StateFlow<CardType?> = _cardType.asStateFlow()

    private val _events = Channel<CameraEvent>()
    val events = _events.receiveAsFlow()

    /**
     * Sets the card type for this capture session
     */
    fun setCardType(cardType: CardType) {
        _cardType.value = cardType
        // Set optimal aspect ratio based on card type
        _aspectRatio.value = when (cardType) {
            is CardType.Credit, is CardType.Debit -> CardAspectRatio.CREDIT_CARD
            is CardType.BusinessCard -> CardAspectRatio.RATIO_16_9
            else -> CardAspectRatio.RATIO_4_3
        }
    }

    /**
     * Sets the aspect ratio for camera capture
     */
    fun setAspectRatio(aspectRatio: CardAspectRatio) {
        _aspectRatio.value = aspectRatio
    }

    /**
     * Called when camera permission is granted
     */
    fun onPermissionGranted() {
        _currentStep.value = CameraStep.FRONT_CAPTURE
    }

    /**
     * Sets the front image and moves to preview
     */
    fun setFrontImage(imageFile: File) {
        _uiState.value = _uiState.value.copy(frontImage = imageFile)
        _currentStep.value = CameraStep.FRONT_PREVIEW
    }

    /**
     * Confirms the front image and moves to back capture
     */
    fun confirmFrontImage() {
        _currentStep.value = CameraStep.BACK_CAPTURE
    }

    /**
     * Retakes the front image
     */
    fun retakeFrontImage() {
        _uiState.value = _uiState.value.copy(frontImage = null)
        _currentStep.value = CameraStep.FRONT_CAPTURE
    }

    /**
     * Sets the back image and moves to preview
     */
    fun setBackImage(imageFile: File) {
        _uiState.value = _uiState.value.copy(backImage = imageFile)
        _currentStep.value = CameraStep.BACK_PREVIEW
    }

    /**
     * Confirms the back image and starts processing
     */
    fun confirmBackImage() {
        _currentStep.value = CameraStep.PROCESSING
        processImages()
    }

    /**
     * Retakes the back image
     */
    fun retakeBackImage() {
        _uiState.value = _uiState.value.copy(backImage = null)
        _currentStep.value = CameraStep.BACK_CAPTURE
    }

    /**
     * Sets an error message
     */
    fun setError(error: String) {
        _uiState.value = _uiState.value.copy(error = error)
    }

    /**
     * Clears the current error
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Processes the captured images
     */
    private fun processImages() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, processingProgress = 0f)

                val frontImage = _uiState.value.frontImage
                val backImage = _uiState.value.backImage
                val currentCardType = _cardType.value

                if (frontImage == null || backImage == null || currentCardType == null) {
                    setError("Missing images or card type")
                    return@launch
                }

                // Update progress
                _uiState.value = _uiState.value.copy(processingProgress = 0.3f)

                // Process OCR if supported
                val extractedData = if (currentCardType.supportsOCR()) {
                    _uiState.value = _uiState.value.copy(processingProgress = 0.6f)
                    
                    val frontImageBytes = frontImage.readBytes()
                    val backImageBytes = backImage.readBytes()
                    val result = processCardImageUseCase.processBothSides(
                        frontImageBytes, 
                        backImageBytes, 
                        currentCardType
                    )
                    
                    result.getOrElse { 
                        setError("Failed to extract card information: ${it.message}")
                        emptyMap()
                    }
                } else {
                    emptyMap()
                }

                // Update progress
                _uiState.value = _uiState.value.copy(processingProgress = 0.9f)

                // Complete processing
                _uiState.value = _uiState.value.copy(
                    extractedData = extractedData,
                    processingProgress = 1f,
                    isLoading = false
                )

                // Emit success event
                _events.send(CameraEvent.ImagesReady(frontImage, backImage))

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                setError("Processing failed: ${e.message}")
            }
        }
    }

    /**
     * Navigates back from camera
     */
    fun navigateBack() {
        viewModelScope.launch {
            _events.send(CameraEvent.NavigateBack)
        }
    }
}