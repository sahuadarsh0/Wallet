package com.technitedminds.wallet.presentation.screens.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.technitedminds.wallet.domain.model.Card
import com.technitedminds.wallet.domain.model.CardType
import com.technitedminds.wallet.domain.model.GradientDirection
import com.technitedminds.wallet.presentation.components.animation.EnhancedShimmerEffect
import com.technitedminds.wallet.presentation.components.animation.EnhancedSlideInItem
import com.technitedminds.wallet.presentation.components.animation.liquidPress
import com.technitedminds.wallet.presentation.components.common.PremiumCard
import com.technitedminds.wallet.presentation.components.common.PremiumChip
import com.technitedminds.wallet.presentation.components.common.PremiumLoadingIndicator
import com.technitedminds.wallet.presentation.components.common.PremiumTextField
import com.technitedminds.wallet.presentation.components.common.ScreenGradientBackground
import com.technitedminds.wallet.presentation.components.common.getIcon
import com.technitedminds.wallet.presentation.components.common.gradientShadow
import com.technitedminds.wallet.presentation.constants.AppConstants
import com.technitedminds.wallet.ui.theme.WalletSpring
import com.technitedminds.wallet.ui.theme.WalletTiming
import com.technitedminds.wallet.ui.theme.gradientContrastText
import kotlinx.coroutines.delay

/**
 * Enhanced home screen with premium animations and improved UX.
 *
 * Top-level content is now organized into category-backed **folders**.
 * Opening a folder reveals the cards it contains; card-type filters and search
 * continue to work inside the opened folder.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedHomeScreen(
    onCardClick: (Card) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showFilterSheet by remember { mutableStateOf(false) }

    val folderItems = remember(uiState.categories, uiState.folderCounts) {
        buildFolderItems(uiState.categories, uiState.folderCounts)
    }
    val openedFolderItem = remember(uiState.openedFolder, folderItems) {
        resolveOpenedFolderItem(uiState.openedFolder, folderItems)
    }
    val isInsideFolder = uiState.openedFolder != null

    // Intercept back when inside a folder so we pop back to the folders grid.
    BackHandler(enabled = isInsideFolder) {
        viewModel.closeFolder()
    }

    ScreenGradientBackground(modifier = modifier) {
        Scaffold(
            contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
            containerColor = Color.Transparent,
            topBar = {
                EnhancedHomeTopBar(
                    searchQuery = uiState.searchQuery,
                    onSearchQueryChange = viewModel::updateSearchQuery,
                    onClearSearch = viewModel::clearSearch,
                    isGridLayout = uiState.isGridLayout,
                    onToggleLayout = viewModel::toggleLayout,
                    searchEnabled = isInsideFolder,
                    layoutToggleEnabled = isInsideFolder,
                )
            },
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                AnimatedContent(
                    targetState = isInsideFolder,
                    transitionSpec = {
                        (slideInHorizontally { it / 6 } + fadeIn(tween(250))) togetherWith
                            (slideOutHorizontally { -it / 6 } + fadeOut(tween(200)))
                    },
                    label = "home_mode",
                    modifier = Modifier.fillMaxSize(),
                ) { inside ->
                    if (inside && openedFolderItem != null) {
                        FolderDetailView(
                            folderItem = openedFolderItem,
                            selectedCardType = uiState.selectedCardType,
                            cards = uiState.filteredCards,
                            isGridLayout = uiState.isGridLayout,
                            isLoading = uiState.isLoading,
                            onBack = viewModel::closeFolder,
                            onOpenFilters = { showFilterSheet = true },
                            onClearCardType = { viewModel.selectCardType(null) },
                            onCardClick = onCardClick,
                        )
                    } else {
                        FoldersView(
                            items = folderItems,
                            onFolderClick = viewModel::openFolder,
                        )
                    }
                }
            }
        }
    }

    if (showFilterSheet) {
        FilterBottomSheet(
            selectedCardType = uiState.selectedCardType,
            onCardTypeSelected = viewModel::selectCardType,
            onDismiss = { showFilterSheet = false },
            onClearAll = {
                viewModel.clearFilters()
                showFilterSheet = false
            },
        )
    }

    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            PremiumLoadingIndicator(text = AppConstants.LoadingMessages.LOADING_CARDS)
        }
    }
}

/**
 * Top-level folders grid content. Wraps [FoldersGrid] with the standard
 * horizontal padding and a small header describing the section.
 */
@Composable
private fun FoldersView(
    items: List<FolderItem>,
    onFolderClick: (OpenedFolder) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Your Folders",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
            ),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 8.dp),
        )

        FoldersGrid(
            items = items,
            onFolderClick = onFolderClick,
        )
    }
}

/**
 * Cards-inside-folder view. Shows a folder header, the card-type filter row,
 * and the filtered cards list/grid.
 */
@Composable
private fun FolderDetailView(
    folderItem: FolderItem,
    selectedCardType: CardType?,
    cards: List<Card>,
    isGridLayout: Boolean,
    isLoading: Boolean,
    onBack: () -> Unit,
    onOpenFilters: () -> Unit,
    onClearCardType: () -> Unit,
    onCardClick: (Card) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        FolderHeader(
            item = folderItem,
            onBack = onBack,
        )

        FilterTriggerBar(
            selectedCardType = selectedCardType,
            onClearCardType = onClearCardType,
            onOpenFilters = onOpenFilters,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        )

        EnhancedCardsSection(
            cards = cards,
            isGridLayout = isGridLayout,
            isLoading = isLoading,
            onCardClick = onCardClick,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
        )
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
    searchEnabled: Boolean,
    layoutToggleEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    var isSearchActive by remember { mutableStateOf(false) }

    // If we leave the folder, drop the search-active UI state so the bar is reset.
    LaunchedEffect(searchEnabled) {
        if (!searchEnabled && isSearchActive) {
            isSearchActive = false
            onClearSearch()
        }
    }

    TopAppBar(
        title = {
            AnimatedContent(
                targetState = isSearchActive && searchEnabled,
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
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    Text(
                        text = AppConstants.UIText.APP_TITLE,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                    )
                }
            }
        },
        actions = {
            if (!isSearchActive) {
                if (searchEnabled) {
                    IconButton(onClick = { isSearchActive = true }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                        )
                    }
                }

                if (layoutToggleEnabled) {
                    IconButton(onClick = onToggleLayout) {
                        Icon(
                            imageVector = if (isGridLayout) Icons.AutoMirrored.Filled.ViewList else Icons.Default.GridView,
                            contentDescription = if (isGridLayout) AppConstants.ContentDescriptions.LIST_VIEW else AppConstants.ContentDescriptions.GRID_VIEW,
                        )
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
        ),
        modifier = modifier,
    )
}

@Composable
private fun FilterTriggerBar(
    selectedCardType: CardType?,
    onClearCardType: () -> Unit,
    onOpenFilters: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hasActiveFilters = selectedCardType != null

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 4.dp),
    ) {
        if (selectedCardType != null) {
            item(key = "type_filter") {
                InputChip(
                    selected = true,
                    onClick = onOpenFilters,
                    label = { Text(selectedCardType.getDisplayName()) },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear type filter",
                            modifier = Modifier
                                .size(18.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = onClearCardType,
                                ),
                        )
                    },
                )
            }
        }

        item(key = "open_filters") {
            PremiumChip(
                text = if (hasActiveFilters) "Edit Filters" else "Filters",
                selected = false,
                onClick = onOpenFilters,
                icon = Icons.Default.FilterList,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterBottomSheet(
    selectedCardType: CardType?,
    onCardTypeSelected: (CardType?) -> Unit,
    onDismiss: () -> Unit,
    onClearAll: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val hasActiveFilters = selectedCardType != null

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text(
                text = "Filter Cards",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 24.dp),
            )

            // Card Type section
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Card Type",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 24.dp),
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp),
                ) {
                    item {
                        PremiumChip(
                            text = AppConstants.UIText.ALL_TYPES_LABEL,
                            selected = selectedCardType == null,
                            onClick = { onCardTypeSelected(null) },
                            icon = Icons.Default.CreditCard,
                        )
                    }
                    items(allPredefinedCardTypes) { type ->
                        PremiumChip(
                            text = type.getDisplayName(),
                            selected = selectedCardType == type,
                            onClick = { onCardTypeSelected(type) },
                            icon = type.getIcon(),
                        )
                    }
                }
            }

            if (hasActiveFilters) {
                TextButton(
                    onClick = onClearAll,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(horizontal = 24.dp),
                ) {
                    Text("Clear All")
                }
            }
        }
    }
}

private val allPredefinedCardTypes = CardType.getAllPredefinedTypes()

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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (cards.isNotEmpty()) {
                Text(
                    text = "${cards.size} Cards",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
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
                            contentPadding = PaddingValues(bottom = 80.dp),
                        ) {
                            itemsIndexed(
                                items = cards,
                                key = { _, card -> card.id },
                            ) { index, card ->
                                SpringStaggerItem(index = index) {
                                    EnhancedCardGridItem(
                                        card = card,
                                        onClick = { onCardClick(card) },
                                    )
                                }
                            }
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(1),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 80.dp),
                        ) {
                            itemsIndexed(
                                items = cards,
                                key = { _, card -> card.id },
                            ) { index, card ->
                                SpringStaggerItem(index = index) {
                                    EnhancedCardListItem(
                                        card = card,
                                        onClick = { onCardClick(card) },
                                    )
                                }
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
    modifier: Modifier = Modifier,
) {
    val gradient = card.getGradient()
    val gradientColors = getCardGradientColors(card)
    val textColor = gradientContrastText(gradient.startColor, gradient.endColor)

    PremiumCard(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .gradientShadow(
                colors = gradientColors.toList(),
                shadowElevation = 8.dp,
                cornerRadius = 16.dp,
            )
            .liquidPress(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = getCardGradientBrush(card))
                .padding(16.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Text(
                        text = card.name,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = textColor,
                        maxLines = 2,
                    )

                    Icon(
                        imageVector = card.type.getIcon(),
                        contentDescription = "Card type",
                        tint = textColor.copy(alpha = 0.8f),
                        modifier = Modifier.size(20.dp),
                    )
                }

                Text(
                    text = card.type.getDisplayName(),
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor.copy(alpha = 0.9f),
                )
            }
        }
    }
}

@Composable
private fun EnhancedCardListItem(
    card: Card,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val gradient = card.getGradient()
    val gradientColors = getCardGradientColors(card)
    val primaryColor = try {
        Color(android.graphics.Color.parseColor(gradient.startColor))
    } catch (e: Exception) {
        Color(android.graphics.Color.parseColor(Card.getDefaultGradientForType(card.type).startColor))
    }

    PremiumCard(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .gradientShadow(
                colors = gradientColors.toList(),
                shadowElevation = 6.dp,
                cornerRadius = 14.dp,
            )
            .liquidPress(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = primaryColor.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = card.type.getIcon(),
                    contentDescription = "Card type",
                    tint = primaryColor,
                    modifier = Modifier.size(24.dp),
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = card.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Text(
                    text = card.type.getDisplayName(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "View details",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun EmptyCardsState(
    modifier: Modifier = Modifier,
) {
    // Breathing pulse on the icon
    val infiniteTransition = rememberInfiniteTransition(label = "empty_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = FastOutSlowInEasing,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "empty_pulse_scale",
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppConstants.Dimensions.SPACING_LARGE),
    ) {
        Icon(
            imageVector = Icons.Default.CreditCard,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier
                .size(64.dp)
                .graphicsLayer {
                    scaleX = pulseScale
                    scaleY = pulseScale
                },
        )

        Text(
            text = "No cards yet",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Text(
            text = "Tap the + button below to add your first card",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = AppConstants.AnimationValues.ALPHA_NEAR_OPAQUE),
        )
    }
}

/**
 * Helper function to create a gradient brush from a card's custom or default gradient.
 * Uses card.getGradient() which returns customGradient if set, otherwise the type default.
 */
private fun getCardGradientBrush(card: Card): Brush {
    val gradient = card.getGradient()

    val (startColor, endColor) = getCardGradientColors(card)

    return when (gradient.direction) {
        GradientDirection.TopToBottom ->
            Brush.verticalGradient(colors = listOf(startColor, endColor))
        GradientDirection.LeftToRight ->
            Brush.horizontalGradient(colors = listOf(startColor, endColor))
        GradientDirection.DiagonalTopLeftToBottomRight ->
            Brush.linearGradient(colors = listOf(startColor, endColor))
        GradientDirection.DiagonalTopRightToBottomLeft ->
            Brush.linearGradient(
                colors = listOf(startColor, endColor),
                start = androidx.compose.ui.geometry.Offset(Float.POSITIVE_INFINITY, 0f),
                end = androidx.compose.ui.geometry.Offset(0f, Float.POSITIVE_INFINITY),
            )
    }
}

private fun getCardGradientColors(card: Card): Pair<Color, Color> {
    val gradient = card.getGradient()
    val startColor = try {
        Color(android.graphics.Color.parseColor(gradient.startColor))
    } catch (e: Exception) {
        Color(android.graphics.Color.parseColor(Card.getDefaultGradientForType(card.type).startColor))
    }

    val endColor = try {
        Color(android.graphics.Color.parseColor(gradient.endColor))
    } catch (e: Exception) {
        Color(android.graphics.Color.parseColor(Card.getDefaultGradientForType(card.type).endColor))
    }
    return startColor to endColor
}

/**
 * Spring-staggered entrance wrapper. Each item fades + scales in with a
 * per-index delay, using spring physics for natural overshoot.
 */
@Composable
private fun SpringStaggerItem(
    index: Int,
    content: @Composable () -> Unit,
) {
    var visible by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.85f,
        animationSpec = WalletSpring.bouncy(),
        label = "stagger_scale",
    )
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = WalletSpring.gentle(),
        label = "stagger_alpha",
    )

    LaunchedEffect(Unit) {
        delay(index.toLong() * WalletTiming.STAGGER_DELAY_MS)
        visible = true
    }

    Box(
        modifier = Modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
            this.alpha = alpha
        },
    ) {
        content()
    }
}
