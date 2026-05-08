package com.technitedminds.wallet.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.technitedminds.wallet.domain.model.Card
import com.technitedminds.wallet.domain.model.CardType
import com.technitedminds.wallet.domain.model.Category
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

    // Opened folder: null = folders home, otherwise a specific folder is opened
    private val _openedFolder = MutableStateFlow<OpenedFolder?>(null)
    val openedFolder = _openedFolder.asStateFlow()

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

    // All cards (no filter) — used for counting cards per folder
    private val allCards: StateFlow<List<Card>> = flow {
        emit(
            GetCardsRequest(
                categoryId = null,
                cardType = null,
                searchQuery = "",
                sortBy = CardSortBy.UPDATED_AT,
                ascending = false,
            )
        )
    }.flatMapLatest { request -> getCardsUseCase(request) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )

    // Card counts per folder (category id -> count); "__all__" holds total, "__uncategorized__" holds blank-category count
    val folderCounts: StateFlow<Map<String, Int>> = allCards.map { cards ->
        buildMap {
            put(FOLDER_ALL_KEY, cards.size)
            put(FOLDER_UNCATEGORIZED_KEY, cards.count { it.categoryId.isBlank() })
            cards.groupBy { it.categoryId }
                .filterKeys { it.isNotBlank() }
                .forEach { (id, list) -> put(id, list.size) }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyMap(),
    )

    // Cards inside the currently-opened folder (filtered by search + card type)
    val filteredCards = combine(
        searchQuery,
        openedFolder,
        selectedCardType,
    ) { query, folder, cardType ->
        GetCardsRequest(
            categoryId = (folder as? OpenedFolder.Category)?.id,
            cardType = cardType,
            searchQuery = query,
            sortBy = CardSortBy.UPDATED_AT,
            ascending = false
        )
    }.debounce(300)
        .flatMapLatest { request ->
            getCardsUseCase(request)
        }
        .combine(openedFolder) { cards, folder ->
            // "Uncategorized" folder: keep only cards with blank categoryId
            if (folder is OpenedFolder.Uncategorized) {
                cards.filter { it.categoryId.isBlank() }
            } else cards
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // UI State — uses nested typed combine to avoid unchecked casts
    val uiState = combine(
        combine(filteredCards, categories, isRefreshing, folderCounts) { cards, cats, refreshing, counts ->
            HomeDataState(cards, cats, refreshing, counts)
        },
        combine(searchQuery, openedFolder, selectedCardType, isGridView) { query, folder, cardType, gridView ->
            HomeFilterState(query, folder, cardType, gridView)
        }
    ) { data, filter ->
        val openedCategoryId = (filter.folder as? OpenedFolder.Category)?.id
        HomeUiState(
            cards = data.cards,
            filteredCards = data.cards,
            categories = data.categories,
            folderCounts = data.counts,
            openedFolder = filter.folder,
            isLoading = false,
            isRefreshing = data.isRefreshing,
            searchQuery = filter.query,
            selectedCategory = openedCategoryId,
            selectedCategoryId = openedCategoryId,
            selectedCardType = filter.cardType,
            isGridView = filter.gridView,
            isGridLayout = filter.gridView,
            isEmpty = data.cards.isEmpty() && filter.query.isBlank() && filter.folder == null && filter.cardType == null
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
     * Open a folder on the home screen.
     */
    fun openFolder(folder: OpenedFolder) {
        _openedFolder.value = folder
    }

    /**
     * Close any opened folder and return to the folders grid.
     */
    fun closeFolder() {
        _openedFolder.value = null
        _selectedCardType.value = null
        _searchQuery.value = ""
    }

    /**
     * Select card type filter
     */
    fun selectCardType(cardType: CardType?) {
        _selectedCardType.value = cardType
    }

    /**
     * Clear all filters (keeps the opened folder).
     */
    fun clearFilters() {
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

    companion object {
        const val FOLDER_ALL_KEY = "__all__"
        const val FOLDER_UNCATEGORIZED_KEY = "__uncategorized__"
    }
}

/**
 * Represents which folder (if any) is currently opened on the home screen.
 */
sealed class OpenedFolder {
    /** The virtual "All Cards" folder showing every card. */
    object All : OpenedFolder()

    /** The virtual folder gathering cards with no category assigned. */
    object Uncategorized : OpenedFolder()

    /** A concrete category-backed folder. */
    data class Category(val id: String) : OpenedFolder()
}

/**
 * Intermediate holder for filter-related state to avoid vararg combine.
 */
private data class HomeFilterState(
    val query: String,
    val folder: OpenedFolder?,
    val cardType: CardType?,
    val gridView: Boolean,
)

/**
 * Intermediate holder for data-related state to avoid vararg combine.
 */
private data class HomeDataState(
    val cards: List<Card>,
    val categories: List<Category>,
    val isRefreshing: Boolean,
    val counts: Map<String, Int>,
)

/**
 * UI state for the Home screen
 */
data class HomeUiState(
    val cards: List<Card> = emptyList(),
    val filteredCards: List<Card> = emptyList(),
    val categories: List<Category> = emptyList(),
    val folderCounts: Map<String, Int> = emptyMap(),
    val openedFolder: OpenedFolder? = null,
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