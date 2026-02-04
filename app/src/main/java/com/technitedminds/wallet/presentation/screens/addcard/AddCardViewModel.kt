package com.technitedminds.wallet.presentation.screens.addcard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.technitedminds.wallet.domain.model.Card
import com.technitedminds.wallet.domain.model.CardGradient
import com.technitedminds.wallet.domain.model.CardType
import com.technitedminds.wallet.domain.service.CardImageGenerator
import com.technitedminds.wallet.domain.usecase.card.AddCardUseCase
import com.technitedminds.wallet.domain.usecase.card.GetCardsUseCase
import com.technitedminds.wallet.domain.usecase.card.UpdateCardUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

// Wizard step handling (top-level so composables can reference directly)
enum class AddCardStep { TYPE_SELECTION, CAMERA_CAPTURE, FORM_DETAILS }

// Capture state for tracking front/back image capture progress
enum class CaptureState {
    AWAITING_FRONT,
    FRONT_CAPTURED,
    BACK_CAPTURED
}

// Basic events (top-level for easy collection in UI)
sealed interface AddCardEvent {
    data class CardSaved(val card: com.technitedminds.wallet.domain.model.Card) : AddCardEvent
}

/**
 * ViewModel for the Add/Edit Card screen managing form state and card operations.
 */
@HiltViewModel
class AddCardViewModel @Inject constructor(
    private val addCardUseCase: AddCardUseCase,
    private val updateCardUseCase: UpdateCardUseCase,
    private val getCardsUseCase: GetCardsUseCase,
    private val getCategoriesUseCase: com.technitedminds.wallet.domain.usecase.category.GetCategoriesUseCase,
    private val manageCategoryUseCase: com.technitedminds.wallet.domain.usecase.category.ManageCategoryUseCase,
    private val cardImageGenerator: CardImageGenerator
) : ViewModel() {
    private val _currentStep = MutableStateFlow(AddCardStep.TYPE_SELECTION)
    val currentStep: StateFlow<AddCardStep> = _currentStep.asStateFlow()

    private val _events = MutableSharedFlow<AddCardEvent>()
    val events: SharedFlow<AddCardEvent> = _events.asSharedFlow()

    private val _uiState = MutableStateFlow(AddCardUiState())
    val uiState: StateFlow<AddCardUiState> = _uiState.asStateFlow()

    // Derived helpers for screen bindings
    val isFormValid: StateFlow<Boolean> = uiState.map { validateForm(it) }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = false
    )
    val cardName: StateFlow<String> = uiState.map { it.cardName }.stateIn(viewModelScope, SharingStarted.Eagerly, "")
    val selectedCategory: StateFlow<String> = uiState.map { it.categoryId }.stateIn(viewModelScope, SharingStarted.Eagerly, "personal")
    val customFields: StateFlow<Map<String, String>> =
        uiState.map { it.customFields }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())
    val categories: StateFlow<List<com.technitedminds.wallet.domain.model.Category>> =
        getCategoriesUseCase().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val extractedData: StateFlow<Map<String, String>> =
        uiState.map { it.extractedData }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    private val _selectedCardType = MutableStateFlow<CardType?>(null)
    val selectedCardType: StateFlow<CardType?> = _selectedCardType.asStateFlow()

    private val _selectedGradient = MutableStateFlow<CardGradient?>(null)
    val selectedGradient: StateFlow<CardGradient?> = _selectedGradient.asStateFlow()

    private val _showGradientPicker = MutableStateFlow(false)
    val showGradientPicker: StateFlow<Boolean> = _showGradientPicker.asStateFlow()

    private val _captureState = MutableStateFlow(CaptureState.AWAITING_FRONT)
    val captureState: StateFlow<CaptureState> = _captureState.asStateFlow()

    init {
        // Load categories and ensure default categories exist
        loadCategories()
    }

    /**
     * Load card for editing
     */
    fun loadCardForEdit(cardId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                getCardsUseCase.getAllCards().collect { cards ->
                    val card = cards.find { it.id == cardId }
                    if (card != null) {
                        val newState = _uiState.value.copy(
                            cardId = card.id,
                            cardName = card.name,
                            cardType = card.type,
                            categoryId = card.categoryId,
                            notes = card.getCustomNotes() ?: "",
                            frontImagePath = card.frontImagePath,
                            backImagePath = card.backImagePath,
                            extractedData = card.extractedData,
                            customFields = card.customFields,
                            customColor = card.customFields["customColor"],
                            cardNumber = card.extractedData["cardNumber"] ?: "",
                            expiryDate = card.extractedData["expiryDate"] ?: "",
                            cardholderName = card.extractedData["cardholderName"] ?: "",
                            cvv = card.extractedData["cvv"] ?: "",
                            hasOCRData = card.extractedData.isNotEmpty(),
                            isEditMode = true,
                            isLoading = false,
                            error = null
                        )
                        _uiState.value = newState.copy(canSave = validateForm(newState))
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Card not found"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load card"
                )
            }
        }
    }

    /**
     * Update card name
     */
    fun updateCardName(name: String) {
        val currentState = _uiState.value
        val newState = currentState.copy(cardName = name)
        _uiState.value = newState.copy(canSave = validateForm(newState))
    }

    fun updateCustomField(key: String, value: String) {
        val updated = _uiState.value.customFields.toMutableMap()
        updated[key] = value
        _uiState.value = _uiState.value.copy(customFields = updated, canSave = validateForm(_uiState.value.copy(customFields = updated)))
    }

    fun removeCustomField(key: String) {
        val updated = _uiState.value.customFields.toMutableMap()
        updated.remove(key)
        _uiState.value = _uiState.value.copy(customFields = updated, canSave = validateForm(_uiState.value.copy(customFields = updated)))
    }

    fun addCustomField(key: String, value: String) {
        updateCustomField(key, value)
    }

    /**
     * Update card type
     */
    fun updateCardType(type: CardType) {
        _uiState.value = _uiState.value.copy(
            cardType = type,
            canSave = validateForm(_uiState.value.copy(cardType = type))
        )
    }

    /**
     * Update category
     */
    fun updateCategory(categoryId: String) {
        _uiState.value = _uiState.value.copy(
            categoryId = categoryId,
            canSave = validateForm(_uiState.value.copy(categoryId = categoryId))
        )
    }

    /**
     * Update notes
     */
    fun updateNotes(notes: String) {
        val currentState = _uiState.value
        val newState = currentState.copy(notes = notes)
        _uiState.value = newState.copy(canSave = validateForm(newState))
    }

    /**
     * Update custom color
     */
    fun updateCustomColor(color: String?) {
        val currentState = _uiState.value
        val updatedCustomFields = currentState.customFields.toMutableMap()
        if (color != null) {
            updatedCustomFields["customColor"] = color
        } else {
            updatedCustomFields.remove("customColor")
        }

        val newState = currentState.copy(
            customColor = color,
            customFields = updatedCustomFields
        )
        _uiState.value = newState.copy(canSave = validateForm(newState))
    }

    /**
     * Save the card
     */
    fun saveCard() {
        val currentState = _uiState.value

        // Clear any previous field errors before validation
        _uiState.value = currentState.copy(
            cardNumberError = null,
            expiryDateError = null,
            cardholderNameError = null,
            cvvError = null,
            error = null
        )

        // Enhanced validation with specific error messages
        if (currentState.cardName.isBlank()) {
            _uiState.value = currentState.copy(error = "Card name is required")
            return
        }

        if (currentState.cardName.length < 2) {
            _uiState.value = currentState.copy(error = "Card name must be at least 2 characters")
            return
        }

        if (currentState.categoryId.isBlank()) {
            _uiState.value = currentState.copy(error = "Please select a category")
            return
        }

        if (currentState.frontImagePath == null) {
            _uiState.value = currentState.copy(error = "Front image is required. Please capture the front side of the card.")
            return
        }

        // Back image is required for OCR cards, optional for image cards (will generate default)
        if (currentState.cardType.supportsOCR() && currentState.backImagePath == null) {
            _uiState.value = currentState.copy(error = "Back image is required for ${currentState.cardType.getDisplayName()}. Please capture both sides of the card.")
            return
        }

        // Validate textual card fields if it's a credit/debit card
        if (currentState.cardType.supportsOCR()) {
            var hasFieldErrors = false
            val errors = mutableMapOf<String, String?>()
            
            
            // Card number validation
            when {
                currentState.cardNumber.isBlank() -> {
                    errors["cardNumber"] = "Card number is required"
                    hasFieldErrors = true
                }
                currentState.cardNumber.replace(" ", "").length < 13 -> {
                    errors["cardNumber"] = "Card number must be at least 13 digits"
                    hasFieldErrors = true
                }
                else -> errors["cardNumber"] = null
            }
            
            // Expiry date validation
            when {
                currentState.expiryDate.isBlank() -> {
                    errors["expiryDate"] = "Expiry date is required"
                    hasFieldErrors = true
                }
                !currentState.expiryDate.matches(Regex("""^\d{2}/\d{2}$""")) -> {
                    errors["expiryDate"] = "Expiry date must be in MM/YY format"
                    hasFieldErrors = true
                }
                else -> errors["expiryDate"] = null
            }
            
            // Cardholder name validation (optional but validate format if provided)
            if (currentState.cardholderName.isNotBlank() && currentState.cardholderName.length < 2) {
                errors["cardholderName"] = "Cardholder name must be at least 2 characters"
                hasFieldErrors = true
            } else {
                errors["cardholderName"] = null
            }
            
            // CVV validation (optional but validate format if provided)
            when {
                currentState.cvv.isNotBlank() && currentState.cvv.length !in 3..4 -> {
                    errors["cvv"] = "CVV must be 3 or 4 digits"
                    hasFieldErrors = true
                }
                currentState.cvv.isNotBlank() && !currentState.cvv.all { it.isDigit() } -> {
                    errors["cvv"] = "CVV must contain only digits"
                    hasFieldErrors = true
                }
                else -> errors["cvv"] = null
            }
            
            if (hasFieldErrors) {
                _uiState.value = currentState.copy(
                    cardNumberError = errors["cardNumber"],
                    expiryDateError = errors["expiryDate"],
                    cardholderNameError = errors["cardholderName"],
                    cvvError = errors["cvv"]
                )
                return
            }
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null)

            try {
                if (currentState.isEditMode) {
                    // For editing, create Card object directly
                    val card = Card(
                        id = currentState.cardId ?: generateCardId(),
                        name = currentState.cardName,
                        type = currentState.cardType,
                        categoryId = currentState.categoryId,
                        frontImagePath = currentState.frontImagePath ?: "",
                        backImagePath = currentState.backImagePath ?: "",
                        extractedData = currentState.extractedData,
                        customFields = currentState.customFields,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                    updateCardUseCase(card)
                    _events.emit(AddCardEvent.CardSaved(card))
                } else {
                    // Determine storage strategy based on card type
                    val useGradientImages = requiresGradientImages(currentState.cardType)
                    
                    // Prepare extracted data for textual cards
                    val finalExtractedData = if (currentState.cardType.supportsOCR()) {
                        mapOf(
                            "cardNumber" to currentState.cardNumber,
                            "expiryDate" to currentState.expiryDate,
                            "cardholderName" to currentState.cardholderName,
                            "cvv" to currentState.cvv
                        ).filterValues { it.isNotBlank() }
                    } else {
                        currentState.extractedData
                    }
                    
                    val cardId = currentState.cardId ?: generateCardId()
                    val gradient = _selectedGradient.value ?: Card.getDefaultGradientForType(currentState.cardType)
                    
                    val imageResult: Triple<ByteArray, ByteArray, CardGradient?> = if (useGradientImages) {
                        // OCR cards: Generate gradient images, discard original captured images
                        try {
                            // Create temporary card for image generation
                            val tempCard = Card(
                                id = cardId,
                                name = currentState.cardName,
                                type = currentState.cardType,
                                categoryId = currentState.categoryId,
                                frontImagePath = "", // Not used for generation
                                backImagePath = "", // Not used for generation
                                extractedData = finalExtractedData,
                                customFields = currentState.customFields,
                                customGradient = gradient,
                                createdAt = System.currentTimeMillis(),
                                updatedAt = System.currentTimeMillis()
                            )
                            
                            // Generate gradient images
                            val frontImagePath = cardImageGenerator.generateCardFrontImagePath(tempCard)
                                ?: throw Exception("Failed to generate front gradient image")
                            val backImagePath = cardImageGenerator.generateCardBackImagePath(tempCard)
                                ?: throw Exception("Failed to generate back gradient image")
                            
                            // Read generated images
                            val frontFile = java.io.File(frontImagePath)
                            val backFile = java.io.File(backImagePath)
                            
                            if (!frontFile.exists()) {
                                throw Exception("Generated front image file not found")
                            }
                            if (!backFile.exists()) {
                                throw Exception("Generated back image file not found")
                            }
                            
                            // Clean up original captured images (discard them)
                            currentState.frontImagePath?.let { path ->
                                java.io.File(path).takeIf { it.exists() }?.delete()
                            }
                            currentState.backImagePath?.let { path ->
                                java.io.File(path).takeIf { it.exists() }?.delete()
                            }
                            
                            Triple(frontFile.readBytes(), backFile.readBytes(), gradient)
                        } catch (e: Exception) {
                            throw Exception("Failed to generate gradient images: ${e.message}", e)
                        }
                    } else {
                        // Image cards: Save captured images, generate default back if only front captured
                        val frontImageFile = currentState.frontImagePath?.let { java.io.File(it) }
                            ?: throw Exception("Front image is required")
                        
                        if (!frontImageFile.exists()) {
                            throw Exception("Front image file not found: ${currentState.frontImagePath}")
                        }
                        
                        val frontData = frontImageFile.readBytes()
                        
                        val backData = if (currentState.backImagePath != null) {
                            val backImageFile = java.io.File(currentState.backImagePath!!)
                            if (!backImageFile.exists()) {
                                throw Exception("Back image file not found: ${currentState.backImagePath}")
                            }
                            backImageFile.readBytes()
                        } else {
                            // Generate default back image
                            val defaultBackPath = cardImageGenerator.generateDefaultBackImage(
                                cardName = currentState.cardName,
                                cardTypeName = currentState.cardType.getDisplayName(),
                                gradient = gradient
                            ) ?: throw Exception("Failed to generate default back image")
                            
                            val defaultBackFile = java.io.File(defaultBackPath)
                            if (!defaultBackFile.exists()) {
                                throw Exception("Generated default back image file not found")
                            }
                            defaultBackFile.readBytes()
                        }
                        
                        Triple(frontData, backData, null) // No custom gradient for image cards
                    }
                    
                    val frontImageData = imageResult.first
                    val backImageData = imageResult.second
                    val customGradient = imageResult.third
                    
                    val request = com.technitedminds.wallet.domain.usecase.card.AddCardRequest(
                        cardId = cardId,
                        name = currentState.cardName,
                        type = currentState.cardType,
                        categoryId = currentState.categoryId,
                        frontImageData = frontImageData,
                        backImageData = backImageData,
                        extractedData = finalExtractedData,
                        customFields = currentState.customFields,
                        customGradient = customGradient
                    )
                    
                    val result = addCardUseCase(request)
                    result.fold(
                        onSuccess = { savedCardId ->
                            // Get the actual saved card to emit event
                            val savedCard = Card(
                                id = savedCardId,
                                name = currentState.cardName,
                                type = currentState.cardType,
                                categoryId = currentState.categoryId,
                                frontImagePath = "", // Will be set by repository
                                backImagePath = "", // Will be set by repository
                                extractedData = finalExtractedData,
                                customFields = currentState.customFields,
                                customGradient = customGradient,
                                createdAt = System.currentTimeMillis(),
                                updatedAt = System.currentTimeMillis()
                            )
                            _events.emit(AddCardEvent.CardSaved(savedCard))
                        },
                        onFailure = { error ->
                            throw Exception("Failed to save card: ${error.message}", error)
                        }
                    )
                }

                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    isSaved = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = e.message ?: "Failed to save card"
                )
            }
        }
    }

    fun nextStep() {
        _currentStep.value = when (_currentStep.value) {
            AddCardStep.TYPE_SELECTION -> AddCardStep.CAMERA_CAPTURE
            AddCardStep.CAMERA_CAPTURE -> AddCardStep.FORM_DETAILS
            AddCardStep.FORM_DETAILS -> AddCardStep.FORM_DETAILS
        }
    }

    fun previousStep() {
        _currentStep.value = when (_currentStep.value) {
            AddCardStep.TYPE_SELECTION -> AddCardStep.TYPE_SELECTION
            AddCardStep.CAMERA_CAPTURE -> AddCardStep.TYPE_SELECTION
            AddCardStep.FORM_DETAILS -> AddCardStep.CAMERA_CAPTURE
        }
    }

    fun skipCameraCapture() {
        _currentStep.value = AddCardStep.FORM_DETAILS
    }

    fun selectCardType(type: CardType) {
        _selectedCardType.value = type
        // Initialize default gradient for the selected card type
        if (_selectedGradient.value == null) {
            val defaultGradient = Card.getDefaultGradientForType(type)
            _selectedGradient.value = defaultGradient
        }
        val newState = _uiState.value.copy(cardType = type)
        _uiState.value = newState.copy(canSave = validateForm(newState))
        _currentStep.value = AddCardStep.CAMERA_CAPTURE
        _captureState.value = CaptureState.AWAITING_FRONT
    }

    fun setExtractedData(data: Map<String, String>) {
        val currentState = _uiState.value
        val newState = currentState.copy(
            extractedData = data,
            // Also update individual fields for validation
            cardNumber = data["cardNumber"] ?: currentState.cardNumber,
            expiryDate = data["expiryDate"] ?: currentState.expiryDate,
            cardholderName = data["cardholderName"] ?: currentState.cardholderName,
            cvv = data["cvv"] ?: currentState.cvv,
            hasOCRData = data.isNotEmpty()
        )
        _uiState.value = newState.copy(canSave = validateForm(newState))
    }

    fun setFrontImagePath(path: String) {
        val newState = _uiState.value.copy(frontImagePath = path)
        _uiState.value = newState.copy(canSave = validateForm(newState))
        _captureState.value = CaptureState.FRONT_CAPTURED
    }

    fun setBackImagePath(path: String) {
        val newState = _uiState.value.copy(backImagePath = path)
        _uiState.value = newState.copy(canSave = validateForm(newState))
        _captureState.value = CaptureState.BACK_CAPTURED
        // Automatically progress to form details after back capture
        if (_currentStep.value == AddCardStep.CAMERA_CAPTURE) {
            _currentStep.value = AddCardStep.FORM_DETAILS
        }
    }

    fun setCapturedImages(frontPath: String, backPath: String?, extractedData: Map<String, String>) {
        val currentState = _uiState.value
        
        // Check if card type supports OCR
        val cardTypeSupportsOCR = currentState.cardType.supportsOCR() || 
                                 (_selectedCardType.value?.supportsOCR() == true)
        
        val newState = if (cardTypeSupportsOCR && extractedData.isNotEmpty()) {
            // Only populate the dedicated card fields, NOT the custom fields
            // This prevents duplication in "Additional Information" section
            currentState.copy(
                frontImagePath = frontPath,
                backImagePath = backPath,
                extractedData = extractedData,
                hasOCRData = true,
                // Pre-fill only the dedicated OCR fields
                cardNumber = extractedData["cardNumber"] ?: "",
                expiryDate = extractedData["expiryDate"] ?: "",
                cardholderName = extractedData["cardholderName"] ?: "",
                cvv = extractedData["cvv"] ?: ""
            )
        } else {
            currentState.copy(
                frontImagePath = frontPath,
                backImagePath = backPath,
                extractedData = extractedData
            )
        }
        
        _uiState.value = newState.copy(canSave = validateForm(newState))
        
        // Update capture state based on what was captured
        when {
            frontPath.isNotBlank() && backPath != null && backPath.isNotBlank() -> {
                _captureState.value = CaptureState.BACK_CAPTURED
                // Move to form details step after capturing both images
                _currentStep.value = AddCardStep.FORM_DETAILS
            }
            frontPath.isNotBlank() -> {
                _captureState.value = CaptureState.FRONT_CAPTURED
            }
            else -> {
                _captureState.value = CaptureState.AWAITING_FRONT
            }
        }
    }
    
    /**
     * Skip back capture and proceed to form details
     */
    fun skipBackCapture() {
        _captureState.value = CaptureState.BACK_CAPTURED
        _currentStep.value = AddCardStep.FORM_DETAILS
    }
    
    /**
     * Open camera for front capture
     */
    fun openCameraForFront() {
        _captureState.value = CaptureState.AWAITING_FRONT
    }
    
    /**
     * Open camera for back capture
     */
    fun openCameraForBack() {
        _captureState.value = CaptureState.FRONT_CAPTURED
    }
    
    /**
     * Update card number field
     */
    fun updateCardNumber(cardNumber: String) {
        val currentState = _uiState.value
        val updatedExtractedData = currentState.extractedData.toMutableMap().apply {
            if (cardNumber.isNotBlank()) {
                this["cardNumber"] = cardNumber
            } else {
                this.remove("cardNumber")
            }
        }
        val newState = currentState.copy(
            cardNumber = cardNumber,
            cardNumberError = null,
            extractedData = updatedExtractedData
        )
        _uiState.value = newState.copy(canSave = validateForm(newState))
    }
    
    /**
     * Update expiry date field
     */
    fun updateExpiryDate(expiryDate: String) {
        val currentState = _uiState.value
        val formatted = formatExpiryInput(expiryDate)
        val updatedExtractedData = currentState.extractedData.toMutableMap().apply {
            if (formatted.isNotBlank()) {
                this["expiryDate"] = formatted
            } else {
                this.remove("expiryDate")
            }
        }
        val newState = currentState.copy(
            expiryDate = formatted,
            expiryDateError = null,
            extractedData = updatedExtractedData
        )
        _uiState.value = newState.copy(canSave = validateForm(newState))
    }
    
    /**
     * Update cardholder name field
     */
    fun updateCardholderName(cardholderName: String) {
        val currentState = _uiState.value
        val updatedExtractedData = currentState.extractedData.toMutableMap().apply {
            if (cardholderName.isNotBlank()) {
                this["cardholderName"] = cardholderName
            } else {
                this.remove("cardholderName")
            }
        }
        val newState = currentState.copy(
            cardholderName = cardholderName,
            cardholderNameError = null,
            extractedData = updatedExtractedData
        )
        _uiState.value = newState.copy(canSave = validateForm(newState))
    }
    
    /**
     * Update CVV field
     */
    fun updateCvv(cvv: String) {
        val currentState = _uiState.value
        val newState = currentState.copy(
            cvv = cvv,
            cvvError = null
        )
        _uiState.value = newState.copy(canSave = validateForm(newState))
    }
    

    
    /**
     * Clear all OCR data and allow manual entry
     */
    fun clearOCRData() {
        val currentState = _uiState.value
        val newState = currentState.copy(
            extractedData = emptyMap(),
            hasOCRData = false,
            cardNumber = "",
            expiryDate = "",
            cardholderName = "",
            cvv = "",
            cardNumberError = null,
            expiryDateError = null,
            cardholderNameError = null,
            cvvError = null
        )
        _uiState.value = newState.copy(canSave = validateForm(newState))
    }
    

    
    /**
     * Format expiry date input as user types (MM/YY)
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
     * Validate the form and determine if it can be saved
     */
    private fun validateForm(state: AddCardUiState): Boolean {
        // Basic validation: card name is required
        if (state.cardName.isBlank() || state.cardName.length < 2) {
            return false
        }
        
        // Category is required
        if (state.categoryId.isBlank()) {
            return false
        }
        
        // Front image is always required
        if (state.frontImagePath == null) {
            return false
        }
        
        // Back image is required for OCR cards, optional for image cards
        if (state.cardType.supportsOCR() && state.backImagePath == null) {
            return false
        }
        
        // For textual cards (Credit/Debit), only require card number and expiry date
        if (state.cardType.supportsOCR()) {
            // Card number is required
            if (state.cardNumber.isBlank()) return false
            if (state.cardNumber.isNotBlank() && state.cardNumber.replace(" ", "").length < 13) return false
            
            // Expiry date is required
            if (state.expiryDate.isBlank()) return false
            if (state.expiryDate.isNotBlank() && !state.expiryDate.matches(Regex("""^\d{2}/\d{2}$"""))) return false
            
            // Optional fields - validate only if present
            if (state.cardholderName.isNotBlank() && state.cardholderName.length < 2) return false
            if (state.cvv.isNotBlank() && state.cvv.length !in 3..4) return false
        }
        
        return true
    }
    
    /**
     * Load categories and create default ones if none exist
     */
    private fun loadCategories() {
        viewModelScope.launch {
            // Check if categories exist, if not create default ones
            getCategoriesUseCase().collect { categories ->
                if (categories.isEmpty()) {
                    // Create default categories
                    manageCategoryUseCase.createDefaultCategories()
                }
            }
        }
    }
    
    /**
     * Generate a unique card ID
     */
    private fun generateCardId(): String {
        return "card_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }
    
    /**
     * Determines the storage strategy based on card type.
     * OCR cards (Credit/Debit) use gradient images, image cards use captured images.
     */
    private fun requiresGradientImages(cardType: CardType): Boolean {
        return cardType.supportsOCR()
    }

    /**
     * Update the selected gradient
     */
    fun updateGradient(gradient: CardGradient) {
        _selectedGradient.value = gradient
    }

    /**
     * Show the gradient picker dialog
     */
    fun showGradientPicker() {
        _showGradientPicker.value = true
    }

    /**
     * Hide the gradient picker dialog
     */
    fun hideGradientPicker() {
        _showGradientPicker.value = false
    }
}

/**
 * UI state for the Add/Edit Card screen
 */
data class AddCardUiState(
    val cardId: String? = null,
    val cardName: String = "",
    val cardType: CardType = CardType.Credit,
    val categoryId: String = "personal",
    val notes: String = "",
    val frontImagePath: String? = null,
    val backImagePath: String? = null,
    val extractedData: Map<String, String> = emptyMap(),
    val customFields: Map<String, String> = emptyMap(),
    val customColor: String? = null,
    val isEditMode: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null,
    val canSave: Boolean = false,
    val showColorPicker: Boolean = false,
    // OCR-related fields
    val hasOCRData: Boolean = false,
    val cardNumber: String = "",
    val expiryDate: String = "",
    val cardholderName: String = "",
    val cvv: String = "",
    // Field errors
    val cardNumberError: String? = null,
    val expiryDateError: String? = null,
    val cardholderNameError: String? = null,
    val cvvError: String? = null,
    val selectedGradient: CardGradient? = null
) {
    // Computed property for OCR status
    val hasOCRDataComputed: Boolean
        get() = extractedData.isNotEmpty()
}