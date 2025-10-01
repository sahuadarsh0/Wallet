package com.technitedminds.wallet.presentation.screens.home

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
// Using simple refresh approach instead of PullToRefresh for now
import com.technitedminds.wallet.domain.model.Card
import com.technitedminds.wallet.domain.model.CardType
import com.technitedminds.wallet.presentation.components.animation.AnimatedLazyColumn
import com.technitedminds.wallet.presentation.components.animation.AnimatedLazyVerticalGrid
import com.technitedminds.wallet.presentation.components.common.*

/**
 * Home screen displaying cards with search, filtering, and layout options.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onCardClick: (Card) -> Unit,
    onAddCardClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val categoryCounts by viewModel.getCategoryCounts().collectAsStateWithLifecycle(initialValue = emptyMap())
    val keyboardController = LocalSoftwareKeyboardController.current

    Scaffold(
        modifier = modifier,
        topBar = {
            HomeTopBar(
                searchQuery = uiState.searchQuery,
                onSearchQueryChange = viewModel::updateSearchQuery,
                onClearSearch = viewModel::clearSearch,
                isGridView = uiState.isGridView,
                onToggleViewMode = viewModel::toggleViewMode,
                onClearFilters = viewModel::clearFilters,
                hasActiveFilters = uiState.selectedCategory != null || 
                                uiState.selectedCardType != null || 
                                uiState.searchQuery.isNotEmpty()
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddCardClick,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Card"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Category filter chips
            if (uiState.categories.isNotEmpty()) {
                CategoryFilterChips(
                    categories = uiState.categories.map { it.name },
                    selectedCategory = uiState.categories.find { it.id == uiState.selectedCategory }?.name,
                    onCategorySelected = { categoryName ->
                        val categoryId = if (categoryName != null) {
                            uiState.categories.find { it.name == categoryName }?.id
                        } else null
                        viewModel.selectCategory(categoryId)
                    },
                    showCounts = categoryCounts.mapKeys { entry ->
                        uiState.categories.find { it.id == entry.key }?.name ?: entry.key
                    },
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // Card type filter chips
            CardTypeFilterChips(
                selectedCardType = uiState.selectedCardType,
                onCardTypeSelected = viewModel::selectCardType,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            // Main content
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                when {
                    uiState.isEmpty -> {
                        EmptyState(
                            onAddCardClick = onAddCardClick,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    uiState.cards.isEmpty() -> {
                        NoResultsState(
                            searchQuery = uiState.searchQuery,
                            hasFilters = uiState.selectedCategory != null || uiState.selectedCardType != null,
                            onClearFilters = viewModel::clearFilters,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    else -> {
                        CardsContent(
                            cards = uiState.cards,
                            isGridView = uiState.isGridView,
                            onCardClick = onCardClick,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

/**
 * Top app bar with search and view controls
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    isGridView: Boolean,
    onToggleViewMode: () -> Unit,
    onClearFilters: () -> Unit,
    hasActiveFilters: Boolean,
    modifier: Modifier = Modifier
) {
    var isSearchActive by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            if (isSearchActive) {
                SearchTextField(
                    query = searchQuery,
                    onQueryChange = onSearchQueryChange,
                    onSearchClose = {
                        isSearchActive = false
                        onClearSearch()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text("CardVault")
            }
        },
        actions = {
            if (!isSearchActive) {
                // Search button
                IconButton(onClick = { isSearchActive = true }) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                }

                // Clear filters button
                AnimatedVisibility(
                    visible = hasActiveFilters,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    IconButton(onClick = onClearFilters) {
                        Icon(
                            imageVector = Icons.Default.FilterListOff,
                            contentDescription = "Clear Filters"
                        )
                    }
                }

                // View mode toggle
                IconButton(onClick = onToggleViewMode) {
                    Icon(
                        imageVector = if (isGridView) Icons.Default.ViewList else Icons.Default.GridView,
                        contentDescription = if (isGridView) "List View" else "Grid View"
                    )
                }
            }
        },
        modifier = modifier
    )
}

/**
 * Search text field component
 */
@Composable
private fun SearchTextField(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearchClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        // Auto-focus when search becomes active
    }

    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Search cards...") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null
            )
        },
        trailingIcon = {
            Row {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear"
                        )
                    }
                }
                IconButton(onClick = {
                    keyboardController?.hide()
                    onSearchClose()
                }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Search"
                    )
                }
            }
        },
        singleLine = true,
        modifier = modifier
    )
}

/**
 * Card type filter chips
 */
@Composable
private fun CardTypeFilterChips(
    selectedCardType: CardType?,
    onCardTypeSelected: (CardType?) -> Unit,
    modifier: Modifier = Modifier
) {
    val cardTypes = remember {
        listOf(
            CardType.Credit,
            CardType.Debit,
            CardType.GiftCard,
            CardType.LoyaltyCard,
            CardType.MembershipCard
        )
    }

    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        // All types chip
        item {
            FilterChip(
                selected = selectedCardType == null,
                onClick = { onCardTypeSelected(null) },
                label = { Text("All Types") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Apps,
                        contentDescription = null
                    )
                }
            )
        }

        // Individual card type chips
        items(cardTypes) { cardType ->
            FilterChip(
                selected = selectedCardType == cardType,
                onClick = { 
                    onCardTypeSelected(if (selectedCardType == cardType) null else cardType)
                },
                label = { Text(cardType.getDisplayName()) }
            )
        }
    }
}

/**
 * Cards content with grid or list layout
 */
@Composable
private fun CardsContent(
    cards: List<Card>,
    isGridView: Boolean,
    onCardClick: (Card) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedContent(
        targetState = isGridView,
        transitionSpec = {
            fadeIn(animationSpec = tween(300)) togetherWith 
            fadeOut(animationSpec = tween(300))
        },
        modifier = modifier,
        label = "cards_layout"
    ) { gridView ->
        if (gridView) {
            // Grid layout
            AnimatedLazyVerticalGrid(
                items = cards,
                columns = GridCells.Adaptive(160.dp),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                key = { card -> card.id }
            ) { card ->
                CardGridItem(
                    card = card,
                    onClick = { onCardClick(card) }
                )
            }
        } else {
            // List layout
            AnimatedLazyColumn(
                items = cards,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                key = { card -> card.id }
            ) { card ->
                CardListItem(
                    card = card,
                    onClick = { onCardClick(card) },
                    showCategory = true
                )
            }
        }
    }
}

/**
 * Grid item for card display
 */
@Composable
private fun CardGridItem(
    card: Card,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.aspectRatio(1.586f), // Credit card aspect ratio
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        CardListItem(
            card = card,
            onClick = onClick,
            isCompact = true,
            showCategory = false
        )
    }
}

/**
 * Empty state when no cards exist
 */
@Composable
private fun EmptyState(
    onAddCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    EmptyStateMessage(
        title = "No Cards Yet",
        message = "Start building your digital wallet by adding your first card. Tap the + button to get started.",
        actionText = "Add First Card",
        onAction = onAddCardClick,
        icon = Icons.Default.CreditCard,
        modifier = modifier
    )
}

/**
 * No results state when search/filter returns empty
 */
@Composable
private fun NoResultsState(
    searchQuery: String,
    hasFilters: Boolean,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    val message = when {
        searchQuery.isNotEmpty() && hasFilters -> 
            "No cards match your search and filters. Try adjusting your criteria."
        searchQuery.isNotEmpty() -> 
            "No cards match \"$searchQuery\". Try a different search term."
        hasFilters -> 
            "No cards match your current filters. Try adjusting your selection."
        else -> 
            "No cards found."
    }

    EmptyStateMessage(
        title = "No Results",
        message = message,
        actionText = if (hasFilters || searchQuery.isNotEmpty()) "Clear Filters" else null,
        onAction = if (hasFilters || searchQuery.isNotEmpty()) onClearFilters else null,
        icon = Icons.Default.SearchOff,
        modifier = modifier
    )
}