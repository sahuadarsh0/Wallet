package com.technitedminds.wallet.presentation.screens.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.technitedminds.wallet.domain.model.Card
import com.technitedminds.wallet.presentation.components.animation.EnhancedShimmerEffect
import com.technitedminds.wallet.presentation.components.animation.EnhancedSlideInItem
import com.technitedminds.wallet.presentation.components.common.AnimatedSectionHeader
import com.technitedminds.wallet.presentation.components.common.PremiumCard
import com.technitedminds.wallet.presentation.components.common.PremiumChip
import com.technitedminds.wallet.presentation.components.common.PremiumFloatingActionButton
import com.technitedminds.wallet.presentation.components.common.PremiumLoadingIndicator
import com.technitedminds.wallet.presentation.components.common.PremiumTextField
import com.technitedminds.wallet.presentation.components.common.getIcon
import com.technitedminds.wallet.presentation.constants.AppConstants

/**
 * Enhanced home screen with premium animations and improved UX
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedHomeScreen(
    onCardClick: (Card) -> Unit,
    onAddCardClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val categoryCounts by viewModel.getCategoryCounts().collectAsStateWithLifecycle(initialValue = emptyMap())

    Scaffold(
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        modifier = modifier,
        topBar = {
            EnhancedHomeTopBar(
                searchQuery = uiState.searchQuery,
                onSearchQueryChange = viewModel::updateSearchQuery,
                onClearSearch = viewModel::clearSearch,
                isGridLayout = uiState.isGridLayout,
                onToggleLayout = viewModel::toggleLayout,
                onRefresh = viewModel::refreshCards
            )
        },
        floatingActionButton = {
            PremiumFloatingActionButton(
                onClick = onAddCardClick,
                icon = Icons.Default.Add,
                contentDescription = AppConstants.ContentDescriptions.ADD_CARD
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {


            // Category filter chips
            if (uiState.categories.isNotEmpty()) {
                EnhancedCategoryFilterSection(
                    categories = uiState.categories,
                    selectedCategoryId = uiState.selectedCategoryId,
                    onCategorySelected = viewModel::selectCategory,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // Cards section
            EnhancedCardsSection(
                cards = uiState.filteredCards,
                isGridLayout = uiState.isGridLayout,
                isLoading = uiState.isLoading,
                onCardClick = onCardClick,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            )
        }
    }

    // Loading overlay
    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            PremiumLoadingIndicator(text = AppConstants.LoadingMessages.LOADING_CARDS)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EnhancedHomeTopBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    isGridLayout: Boolean,
    onToggleLayout: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isSearchActive by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            AnimatedContent(
                targetState = isSearchActive,
                transitionSpec = {
                    slideInHorizontally() + fadeIn() togetherWith
                    slideOutHorizontally() + fadeOut()
                },
                label = "topbar_content"
            ) { searchActive ->
                if (searchActive) {
                    PremiumTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        label = AppConstants.UIText.SEARCH_CARDS_PLACEHOLDER,
                        leadingIcon = Icons.Default.Search,
                        trailingIcon = {
                            IconButton(onClick = {
                                onClearSearch()
                                isSearchActive = false
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = AppConstants.ContentDescriptions.CLEAR_SEARCH
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text(
                        text = AppConstants.UIText.APP_TITLE,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
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
                
                // Layout toggle
                IconButton(onClick = onToggleLayout) {
                    Icon(
                        imageVector = if (isGridLayout) Icons.Default.ViewList else Icons.Default.GridView,
                        contentDescription = if (isGridLayout) AppConstants.ContentDescriptions.LIST_VIEW else AppConstants.ContentDescriptions.GRID_VIEW
                    )
                }
                
                // Refresh button
                IconButton(onClick = onRefresh) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = AppConstants.ContentDescriptions.REFRESH
                    )
                }
            }
        },
        modifier = modifier
    )
}

@Composable
private fun EnhancedQuickStatsSection(
    totalCards: Int,
    categoryCounts: Map<String, Int>,
    modifier: Modifier = Modifier
) {
    EnhancedSlideInItem(visible = true, index = 0) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AnimatedSectionHeader(
                    title = "Quick Stats",
                    icon = Icons.Default.Analytics,
                    subtitle = "Your wallet overview"
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(
                        value = totalCards.toString(),
                        label = AppConstants.StatisticsLabels.TOTAL_CARDS,
                        icon = Icons.Default.CreditCard,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    StatItem(
                        value = categoryCounts.size.toString(),
                        label = AppConstants.StatisticsLabels.CATEGORIES,
                        icon = Icons.Default.Category,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    
                    StatItem(
                        value = categoryCounts.values.maxOrNull()?.toString() ?: "0",
                        label = "Most Used",
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    value: String,
    label: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            color.copy(alpha = 0.2f),
                            color.copy(alpha = 0.1f)
                        )
                    ),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EnhancedCategoryFilterSection(
    categories: List<com.technitedminds.wallet.domain.model.Category>,
    selectedCategoryId: String?,
    onCategorySelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    EnhancedSlideInItem(visible = true, index = 1) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Filter by Category",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                // All categories chip
                item {
                    PremiumChip(
                        text = "All",
                        selected = selectedCategoryId == null,
                        onClick = { onCategorySelected(null) },
                        icon = Icons.Default.SelectAll
                    )
                }
                
                // Category chips
                items(categories) { category ->
                    PremiumChip(
                        text = category.name,
                        selected = selectedCategoryId == category.id,
                        onClick = { onCategorySelected(category.id) },
                        icon = category.getIcon()
                    )
                }
            }
        }
    }
}

@Composable
private fun EnhancedCardsSection(
    cards: List<Card>,
    isGridLayout: Boolean,
    isLoading: Boolean,
    onCardClick: (Card) -> Unit,
    modifier: Modifier = Modifier
) {
    EnhancedSlideInItem(visible = true, index = 2) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = String.format(AppConstants.UIText.YOUR_CARDS, cards.size),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                if (cards.isNotEmpty()) {
                    Text(
                        text = if (isGridLayout) AppConstants.ContentDescriptions.GRID_VIEW else AppConstants.ContentDescriptions.LIST_VIEW,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            when {
                isLoading -> {
                    repeat(3) { index ->
                        EnhancedShimmerEffect(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(if (isGridLayout) 120.dp else 80.dp),
                            isLoading = true
                        )
                    }
                }
                cards.isEmpty() -> {
                    EmptyCardsState(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp)
                    )
                }
                else -> {
                    if (isGridLayout) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            items(cards) { card ->
                                EnhancedCardGridItem(
                                    card = card,
                                    onClick = { onCardClick(card) }
                                )
                            }
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(1),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            items(cards) { card ->
                                EnhancedCardListItem(
                                    card = card,
                                    onClick = { onCardClick(card) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EnhancedCardGridItem(
    card: Card,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    PremiumCard(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(android.graphics.Color.parseColor(card.getDisplayColor())).copy(alpha = 0.8f),
                            Color(android.graphics.Color.parseColor(card.getDisplayColor())).copy(alpha = 0.4f)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = card.name,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color.White,
                        maxLines = 2
                    )
                    
                    Icon(
                        imageVector = card.type.getIcon(),
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Text(
                    text = card.type.getDisplayName(),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
private fun EnhancedCardListItem(
    card: Card,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    PremiumCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Card type icon with background
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = Color(android.graphics.Color.parseColor(card.getDisplayColor())).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = card.type.getIcon(),
                    contentDescription = null,
                    tint = Color(android.graphics.Color.parseColor(card.getDisplayColor())),
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // Card info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = card.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = card.type.getDisplayName(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Arrow icon
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun EmptyCardsState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppConstants.Dimensions.SPACING_LARGE)
    ) {
        Icon(
            imageVector = Icons.Default.CreditCard,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.size(64.dp)
        )
        
        Text(
            text = "No cards yet",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Text(
            text = "Tap the + button to add your first card",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = AppConstants.AnimationValues.ALPHA_NEAR_OPAQUE)
        )
    }
}