package com.technitedminds.wallet.presentation.screens.addcard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.technitedminds.wallet.domain.model.Card
import com.technitedminds.wallet.domain.model.CardType
import com.technitedminds.wallet.domain.usecase.card.AddCardUseCase
import com.technitedminds.wallet.domain.usecase.card.GetCardsUseCase
import com.technitedminds.wallet.domain.usecase.card.UpdateCardUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// Wizard step handling (top-level so composables can reference directly)
enum class AddCardStep { TYPE_SELECTION, CAMERA_CAPTURE, FORM_DETAILS }

// Basic events (top-level for easy collection in UI)
sealed interface AddCardEvent { data class CardSaved(val card: com.technitedminds.wallet.domain.model.Card) : AddCardEvent }

/**
 * ViewModel for the Add/Edit Card screen managing form state and card operations.
 */
@HiltViewModel
class AddCardViewModel @Inject constructor(
    private val addCardUseCase: AddCardUseCase,
    private val updateCardUseCase: UpdateCardUseCase,
    private val getCardsUseCase: GetCardsUseCase
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
    val customFields: StateFlow<Map<String, String>> = uiState.map { it.customFields }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())
    val categories: StateFlow<List<com.technitedminds.wallet.domain.model.Category>> = flowOf(emptyList<com.technitedminds.wallet.domain.model.Category>()).stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val extractedData: StateFlow<Map<String, String>> = uiState.map { it.extractedData }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    private val _selectedCardType = MutableStateFlow<CardType?>(null)
    val selectedCardType: StateFlow<CardType?> = _selectedCardType.asStateFlow()
    
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
                        _uiState.value = _uiState.value.copy(
                            cardId = card.id,
                            cardName = card.name,
                            cardType = card.type,
                            categoryId = card.categoryId,
                            notes = card.getNotes() ?: "",
                            frontImagePath = card.frontImagePath,
                            backImagePath = card.backImagePath,
                            extractedData = card.extractedData,
                            customFields = card.customFields,
                            customColor = card.customFields["customColor"],
                            isEditMode = true,
                            isLoading = false,
                            error = null
                        )
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
        _uiState.value = _uiState.value.copy(
            cardName = name,
            canSave = validateForm(_uiState.value.copy(cardName = name))
        )
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
        _uiState.value = _uiState.value.copy(
            notes = notes,
            canSave = validateForm(_uiState.value.copy(notes = notes))
        )
    }
    
    /**
     * Update custom color
     */
    fun updateCustomColor(color: String?) {
        val updatedCustomFields = _uiState.value.customFields.toMutableMap()
        if (color != null) {
            updatedCustomFields["customColor"] = color
        } else {
            updatedCustomFields.remove("customColor")
        }
        
        _uiState.value = _uiState.value.copy(
            customColor = color,
            customFields = updatedCustomFields,
            canSave = validateForm(_uiState.value.copy(customColor = color))
        )
    }
    
    /**
     * Save the card
     */
    fun saveCard() {
        val currentState = _uiState.value
        if (!currentState.canSave) return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null)
            
            try {
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
                
                if (currentState.isEditMode) {
                    updateCardUseCase(card)
                } else {
                    addCardUseCase(card)
                }
                
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    isSaved = true
                )
                _events.emit(AddCardEvent.CardSaved(card))
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
        _uiState.value = _uiState.value.copy(cardType = type)
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

    fun clearError() { _uiState.value = _uiState.value.copy(error = null) }

    fun selectCategory(categoryId: String) { updateCategory(categoryId) }
    
    /**
     * Validate the form and determine if it can be saved
     */
    private fun validateForm(state: AddCardUiState): Boolean {
        return state.cardName.isNotBlank() &&
               state.cardName.length >= 2 &&
               state.categoryId.isNotBlank()
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
    val showColorPicker: Boolean = false
)