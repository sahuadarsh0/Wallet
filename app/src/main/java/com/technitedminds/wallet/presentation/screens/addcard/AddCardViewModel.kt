package com.technitedminds.wallet.presentation.screens.addcard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.technitedminds.wallet.domain.model.Card
import com.technitedminds.wallet.domain.model.CardType
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
    private val manageCategoryUseCase: com.technitedminds.wallet.domain.usecase.category.ManageCategoryUseCase
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
            _uiState.value = currentState.copy(error = "Front image is required. Please capture both sides of the card.")
            return
        }

        if (currentState.backImagePath == null) {
            _uiState.value = currentState.copy(error = "Back image is required. Please capture both sides of the card.")
            return
        }

        // Validate textual card fields if it's a credit/debit card
        if (currentState.cardType.supportsOCR()) {
            // Only require card number and expiry date - make other fields optional
            if (currentState.cardNumber.isBlank()) {
                _uiState.value = currentState.copy(error = "Card number is required for ${currentState.cardType.getDisplayName()}")
                return
            }
            if (currentState.expiryDate.isBlank()) {
                _uiState.value = currentState.copy(error = "Expiry date is required for ${currentState.cardType.getDisplayName()}")
                return
            }
            // Cardholder name and CVV are optional - OCR might not always extract them
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
                    // For adding new card, use AddCardRequest
                    println("AddCardViewModel: Saving new card - ${currentState.cardName}")
                    println("AddCardViewModel: Card type - ${currentState.cardType.getDisplayName()}")
                    println("AddCardViewModel: Category - ${currentState.categoryId}")
                    println("AddCardViewModel: Front image path - ${currentState.frontImagePath}")
                    println("AddCardViewModel: Back image path - ${currentState.backImagePath}")

                    // Validate image files exist
                    val frontImageFile = java.io.File(currentState.frontImagePath!!)
                    val backImageFile = java.io.File(currentState.backImagePath!!)

                    if (!frontImageFile.exists()) {
                        throw Exception("Front image file not found: ${currentState.frontImagePath}")
                    }

                    if (!backImageFile.exists()) {
                        throw Exception("Back image file not found: ${currentState.backImagePath}")
                    }

                    val frontImageData = frontImageFile.readBytes()
                    val backImageData = backImageFile.readBytes()

                    println("AddCardViewModel: Front image size - ${frontImageData.size} bytes")
                    println("AddCardViewModel: Back image size - ${backImageData.size} bytes")

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

                    val request = com.technitedminds.wallet.domain.usecase.card.AddCardRequest(
                        cardId = currentState.cardId ?: generateCardId(),
                        name = currentState.cardName,
                        type = currentState.cardType,
                        categoryId = currentState.categoryId,
                        frontImageData = frontImageData,
                        backImageData = backImageData,
                        extractedData = finalExtractedData,
                        customFields = currentState.customFields
                    )

                    println("AddCardViewModel: Calling addCardUseCase with request")
                    val result = addCardUseCase(request)
                    result.fold(
                        onSuccess = { cardId ->
                            println("AddCardViewModel: Card saved successfully with ID: $cardId")
                            val card = Card(
                                id = cardId,
                                name = currentState.cardName,
                                type = currentState.cardType,
                                categoryId = currentState.categoryId,
                                frontImagePath = currentState.frontImagePath ?: "",
                                backImagePath = currentState.backImagePath ?: "",
                                extractedData = finalExtractedData,
                                customFields = currentState.customFields,
                                createdAt = System.currentTimeMillis(),
                                updatedAt = System.currentTimeMillis()
                            )
                            _events.emit(AddCardEvent.CardSaved(card))
                        },
                        onFailure = { error ->
                            println("AddCardViewModel: Failed to save card: ${error.message}")
                            throw error
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
        val newState = _uiState.value.copy(cardType = type)
        _uiState.value = newState.copy(canSave = validateForm(newState))
        _currentStep.value = AddCardStep.CAMERA_CAPTURE
    }

    fun setExtractedData(data: Map<String, String>) {
        _uiState.value = _uiState.value.copy(extractedData = data)
    }

    fun setFrontImagePath(path: String) {
        _uiState.value = _uiState.value.copy(frontImagePath = path)
    }

    fun setBackImagePath(path: String) {
        _uiState.value = _uiState.value.copy(backImagePath = path)
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
        
        // Move to form details step after capturing images
        _currentStep.value = AddCardStep.FORM_DETAILS
    }
    
    /**
     * Update card number field
     */
    fun updateCardNumber(cardNumber: String) {
        val currentState = _uiState.value
        val newState = currentState.copy(
            cardNumber = cardNumber,
            cardNumberError = null
        )
        _uiState.value = newState.copy(canSave = validateForm(newState))
    }
    
    /**
     * Update expiry date field
     */
    fun updateExpiryDate(expiryDate: String) {
        val currentState = _uiState.value
        val formatted = formatExpiryInput(expiryDate)
        val newState = currentState.copy(
            expiryDate = formatted,
            expiryDateError = null
        )
        _uiState.value = newState.copy(canSave = validateForm(newState))
    }
    
    /**
     * Update cardholder name field
     */
    fun updateCardholderName(cardholderName: String) {
        val currentState = _uiState.value
        val newState = currentState.copy(
            cardholderName = cardholderName,
            cardholderNameError = null
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
        
        // Images are required
        if (state.frontImagePath == null || state.backImagePath == null) {
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
    val cvvError: String? = null
) {
    // Computed property for OCR status
    val hasOCRDataComputed: Boolean
        get() = extractedData.isNotEmpty()
}