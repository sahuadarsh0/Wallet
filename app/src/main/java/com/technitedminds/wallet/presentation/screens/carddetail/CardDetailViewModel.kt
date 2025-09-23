package com.technitedminds.wallet.presentation.screens.carddetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.technitedminds.wallet.domain.model.Card
import com.technitedminds.wallet.domain.usecase.card.DeleteCardUseCase
import com.technitedminds.wallet.domain.usecase.card.GetCardsUseCase
import com.technitedminds.wallet.domain.usecase.card.UpdateCardUseCase
import com.technitedminds.wallet.domain.usecase.card.UpdateCardRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Card Detail screen that manages card viewing, editing, and deletion.
 */
@HiltViewModel
class CardDetailViewModel @Inject constructor(
    private val getCardsUseCase: GetCardsUseCase,
    private val updateCardUseCase: UpdateCardUseCase,
    private val deleteCardUseCase: DeleteCardUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val cardId: String = checkNotNull(savedStateHandle["cardId"])

    // Card data - convert suspend function to Flow
    val card = flow {
        emit(getCardsUseCase.getCardById(cardId))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    // UI state
    private val _uiState = MutableStateFlow(CardDetailUiState())
    val uiState = _uiState.asStateFlow()

    // Edit state
    private val _isEditing = MutableStateFlow(false)
    val isEditing = _isEditing.asStateFlow()

    private val _editedCard = MutableStateFlow<Card?>(null)
    val editedCard = _editedCard.asStateFlow()

    // Actions
    private val _events = MutableSharedFlow<CardDetailEvent>()
    val events = _events.asSharedFlow()

    /**
     * Start editing the card
     */
    fun startEditing() {
        viewModelScope.launch {
            val currentCard = card.value
            if (currentCard != null) {
                _editedCard.value = currentCard
                _isEditing.value = true
            }
        }
    }

    /**
     * Cancel editing
     */
    fun cancelEditing() {
        _isEditing.value = false
        _editedCard.value = null
    }

    /**
     * Save edited card
     */
    fun saveCard() {
        val edited = _editedCard.value
        if (edited != null) {
            viewModelScope.launch {
                try {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                    val request = UpdateCardRequest(
                        cardId = edited.id,
                        name = edited.name,
                        categoryId = edited.categoryId,
                        customFields = edited.customFields
                    )
                    val result = updateCardUseCase(request)
                    if (result.isFailure) {
                        throw result.exceptionOrNull() ?: Exception("Failed to update card")
                    }
                    _isEditing.value = false
                    _editedCard.value = null
                    _events.emit(CardDetailEvent.CardSaved)
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to save card: ${e.message}"
                    )
                } finally {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            }
        }
    }

    /**
     * Update card name
     */
    fun updateCardName(name: String) {
        val current = _editedCard.value
        if (current != null) {
            _editedCard.value = current.copy(name = name)
        }
    }

    /**
     * Update custom field
     */
    fun updateCustomField(key: String, value: String) {
        val current = _editedCard.value
        if (current != null) {
            val updatedFields = current.customFields.toMutableMap()
            if (value.isBlank()) {
                updatedFields.remove(key)
            } else {
                updatedFields[key] = value
            }
            _editedCard.value = current.copy(customFields = updatedFields)
        }
    }

    /**
     * Update card color
     */
    fun updateCardColor(colorHex: String) {
        val current = _editedCard.value
        if (current != null) {
            _editedCard.value = current.withCustomColor(colorHex)
        }
    }

    /**
     * Delete the card
     */
    fun deleteCard() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                deleteCardUseCase(cardId)
                _events.emit(CardDetailEvent.CardDeleted)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to delete card: ${e.message}"
                )
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    /**
     * Toggle card flip state
     */
    fun toggleCardFlip() {
        _uiState.value = _uiState.value.copy(
            isCardFlipped = !_uiState.value.isCardFlipped
        )
    }

    /**
     * Show delete confirmation dialog
     */
    fun showDeleteConfirmation() {
        _uiState.value = _uiState.value.copy(showDeleteDialog = true)
    }

    /**
     * Hide delete confirmation dialog
     */
    fun hideDeleteConfirmation() {
        _uiState.value = _uiState.value.copy(showDeleteDialog = false)
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Share card information (without sensitive data)
     */
    fun shareCard() {
        viewModelScope.launch {
            val currentCard = card.value
            if (currentCard != null) {
                val shareText = buildString {
                    appendLine("Card: ${currentCard.name}")
                    appendLine("Type: ${currentCard.type.getDisplayName()}")
                    
                    // Only include non-sensitive information
                    currentCard.customFields.forEach { (key, value) ->
                        if (key !in listOf("cardNumber", "cvv", "pin", "password")) {
                            appendLine("$key: $value")
                        }
                    }
                }
                
                _events.emit(CardDetailEvent.ShareCard(shareText))
            }
        }
    }
}

/**
 * UI state for the Card Detail screen
 */
data class CardDetailUiState(
    val isLoading: Boolean = false,
    val isCardFlipped: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val error: String? = null
)

/**
 * Events emitted by the Card Detail screen
 */
sealed class CardDetailEvent {
    object CardSaved : CardDetailEvent()
    object CardDeleted : CardDetailEvent()
    data class ShareCard(val text: String) : CardDetailEvent()
}