package com.technitedminds.wallet.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.technitedminds.wallet.domain.model.Card
import com.technitedminds.wallet.domain.model.CardType
import com.technitedminds.wallet.domain.model.Category
import com.technitedminds.wallet.domain.repository.CardRepository
import com.technitedminds.wallet.domain.repository.CategoryRepository
import com.technitedminds.wallet.domain.usecase.card.GetCardsUseCase
import com.technitedminds.wallet.domain.usecase.card.GetCardsRequest
import com.technitedminds.wallet.domain.repository.CardSortBy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Home screen that manages card display, filtering, and search functionality.
 */
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getCardsUseCase: GetCardsUseCase,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    // Search and filter state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _selectedCardType = MutableStateFlow<CardType?>(null)
    val selectedCardType = _selectedCardType.asStateFlow()

    private val _isGridView = MutableStateFlow(true)
    val isGridView = _isGridView.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    // Categories
    val categories = categoryRepository.getAllCategories()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // All cards with filtering applied
    val filteredCards = combine(
        searchQuery,
        selectedCategory,
        selectedCardType
    ) { query, category, cardType ->
        GetCardsRequest(
            categoryId = category,
            cardType = cardType,
            searchQuery = query,
            sortBy = CardSortBy.UPDATED_AT,
            ascending = false
        )
    }.debounce(300) // Debounce search input
        .flatMapLatest { request ->
            getCardsUseCase(request)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // UI State
    val uiState = combine(
        filteredCards,
        categories,
        isRefreshing,
        searchQuery,
        selectedCategory,
        selectedCardType,
        isGridView
    ) { flows ->
        val cards = flows[0] as List<Card>
        val categories = flows[1] as List<Category>
        val refreshing = flows[2] as Boolean
        val query = flows[3] as String
        val category = flows[4] as String?
        val cardType = flows[5] as CardType?
        val gridView = flows[6] as Boolean
        
        HomeUiState(
            cards = cards,
            filteredCards = cards,
            categories = categories,
            isLoading = false,
            isRefreshing = refreshing,
            searchQuery = query,
            selectedCategory = category,
            selectedCategoryId = category,
            selectedCardType = cardType,
            isGridView = gridView,
            isGridLayout = gridView,
            isEmpty = cards.isEmpty() && query.isBlank() && category == null && cardType == null
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    /**
     * Update search query
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    /**
     * Clear search query
     */
    fun clearSearch() {
        _searchQuery.value = ""
    }

    /**
     * Select category filter
     */
    fun selectCategory(categoryId: String?) {
        _selectedCategory.value = categoryId
    }

    /**
     * Select card type filter
     */
    fun selectCardType(cardType: CardType?) {
        _selectedCardType.value = cardType
    }

    /**
     * Clear all filters
     */
    fun clearFilters() {
        _selectedCategory.value = null
        _selectedCardType.value = null
        _searchQuery.value = ""
    }

    /**
     * Toggle between grid and list view
     */
    fun toggleViewMode() {
        _isGridView.value = !_isGridView.value
    }

    /**
     * Toggle layout (alias for toggleViewMode)
     */
    fun toggleLayout() {
        toggleViewMode()
    }

    /**
     * Refresh cards data
     */
    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                // Trigger data refresh by collecting the flow
                filteredCards.first()
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    /**
     * Refresh cards (alias for refresh)
     */
    fun refreshCards() {
        refresh()
    }

    /**
     * Get category counts for filter chips
     */
    fun getCategoryCounts(): Flow<Map<String, Int>> {
        return flow {
            val cards = getCardsUseCase.getAllCards().first()
            val counts = cards.groupBy { it.categoryId }
                .mapValues { it.value.size }
            emit(counts)
        }
    }
}

/**
 * UI state for the Home screen
 */
data class HomeUiState(
    val cards: List<Card> = emptyList(),
    val filteredCards: List<Card> = emptyList(),
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val searchQuery: String = "",
    val selectedCategory: String? = null,
    val selectedCategoryId: String? = null,
    val selectedCardType: CardType? = null,
    val isGridView: Boolean = true,
    val isGridLayout: Boolean = true,
    val isEmpty: Boolean = false,
    val error: String? = null
)