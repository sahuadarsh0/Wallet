package com.technitedminds.wallet.presentation.screens.carddetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.technitedminds.wallet.domain.model.Card
import com.technitedminds.wallet.domain.usecase.card.DeleteCardUseCase
import com.technitedminds.wallet.domain.usecase.card.GetCardsUseCase
import com.technitedminds.wallet.domain.usecase.card.UpdateCardUseCase
import com.technitedminds.wallet.domain.usecase.card.UpdateCardRequest
import com.technitedminds.wallet.domain.usecase.category.GetCategoriesUseCase
import com.technitedminds.wallet.domain.model.Category
import com.technitedminds.wallet.domain.model.CardType
import com.technitedminds.wallet.presentation.components.sharing.CardSharingManager
import com.technitedminds.wallet.presentation.components.sharing.CardSharingOption
import com.technitedminds.wallet.presentation.components.sharing.CardSharingConfig
import com.technitedminds.wallet.presentation.components.sharing.CardSharingResult
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
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val cardSharingManager: CardSharingManager,
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
    
    // Sharing dialog state
    private val _showSharingDialog = MutableStateFlow(false)
    val showSharingDialog = _showSharingDialog.asStateFlow()
    
    // Categories for dropdown
    val categories = getCategoriesUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

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
                        type = edited.type,
                        categoryId = edited.categoryId,
                        extractedData = edited.extractedData,
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
     * Update card category
     */
    fun updateCardCategory(categoryId: String) {
        val current = _editedCard.value
        if (current != null) {
            _editedCard.value = current.copy(categoryId = categoryId)
        }
    }

    /**
     * Update card type
     */
    fun updateCardType(cardType: CardType) {
        val current = _editedCard.value
        if (current != null) {
            _editedCard.value = current.copy(type = cardType)
        }
    }

    /**
     * Update extracted data field
     */
    fun updateExtractedData(key: String, value: String) {
        val current = _editedCard.value
        if (current != null) {
            val updatedData = current.extractedData.toMutableMap()
            if (value.isBlank()) {
                updatedData.remove(key)
            } else {
                updatedData[key] = value
            }
            _editedCard.value = current.copy(extractedData = updatedData)
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
     * Add new custom field
     */
    fun addCustomField(key: String, value: String = "") {
        val current = _editedCard.value
        if (current != null && key.isNotBlank()) {
            val updatedFields = current.customFields.toMutableMap()
            updatedFields[key] = value
            _editedCard.value = current.copy(customFields = updatedFields)
        }
    }

    /**
     * Remove custom field
     */
    fun removeCustomField(key: String) {
        val current = _editedCard.value
        if (current != null) {
            val updatedFields = current.customFields.toMutableMap()
            updatedFields.remove(key)
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
     * Show sharing dialog for advanced options
     */
    fun showSharingDialog() {
        _showSharingDialog.value = true
    }
    
    /**
     * Hide sharing dialog
     */
    fun hideSharingDialog() {
        _showSharingDialog.value = false
    }
    
    /**
     * Quick share with default settings (for button clicks)
     */
    fun quickShare(sharingOption: CardSharingOption) {
        shareCardWithConfig(
            sharingOption = sharingOption,
            config = CardSharingConfig(
                includeSensitiveInfo = false,
                imageQuality = 0.8f,
                maxImageWidth = 1200,
                maxImageHeight = 800,
                addWatermark = true,
                watermarkText = "CardVault"
            )
        )
    }
    
    /**
     * Share card with custom configuration (from dialog)
     */
    fun shareCardWithConfig(sharingOption: CardSharingOption, config: CardSharingConfig) {
        viewModelScope.launch {
            val currentCard = card.value
            if (currentCard != null) {
                try {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                    
                    val result = cardSharingManager.shareCard(currentCard, sharingOption, config)
                    
                    when (result) {
                        is CardSharingResult.Success -> {
                            _events.emit(CardDetailEvent.ShareSuccess)
                        }
                        is CardSharingResult.Cancelled -> {
                            // User cancelled, no action needed
                        }
                        is CardSharingResult.Error -> {
                            _uiState.value = _uiState.value.copy(
                                error = "Failed to share card: ${result.message}"
                            )
                        }
                    }
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to share card: ${e.message}"
                    )
                } finally {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            }
        }
    }
    
    /**
     * Share card with sensitive information (legacy method - now uses config)
     */
    fun shareCardWithSensitiveInfo(sharingOption: CardSharingOption = CardSharingOption.FrontOnly) {
        shareCardWithConfig(
            sharingOption = sharingOption,
            config = CardSharingConfig(
                includeSensitiveInfo = true,
                imageQuality = 0.9f,
                maxImageWidth = 1200,
                maxImageHeight = 800,
                addWatermark = false,
                watermarkText = ""
            )
        )
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
    object ShareSuccess : CardDetailEvent()
}