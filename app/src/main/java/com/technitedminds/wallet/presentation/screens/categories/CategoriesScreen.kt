package com.technitedminds.wallet.presentation.screens.categories

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.technitedminds.wallet.domain.model.Category
import com.technitedminds.wallet.presentation.components.common.getIconFromName
import com.technitedminds.wallet.presentation.constants.AppConstants
import com.technitedminds.wallet.ui.theme.WalletTheme
import com.technitedminds.wallet.utils.asDisplayName


/**
 * Categories screen for managing card categories
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    modifier: Modifier = Modifier,
    viewModel: CategoriesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Handle events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CategoriesEvent.CategoryCreated -> {
                    // Could show a snackbar or other feedback
                }
                is CategoriesEvent.CategoryUpdated -> {
                    // Could show a snackbar or other feedback
                }
                is CategoriesEvent.CategoryDeleted -> {
                    // Could show a snackbar or other feedback
                }
                is CategoriesEvent.DefaultCategoriesCreated -> {
                    // Could show a snackbar or other feedback
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(AppConstants.NavigationLabels.CATEGORIES) },
                actions = {
                    IconButton(onClick = viewModel::createDefaultCategories) {
                        Icon(
                            imageVector = Icons.Default.Restore,
                            contentDescription = AppConstants.ContentDescriptions.RESTORE_DEFAULTS
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = viewModel::showCreateCategoryDialog
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = AppConstants.UIText.ADD_CATEGORY
                )
            }
        },
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                uiState.categories.isEmpty() -> {
                    EmptyState(
                        onCreateCategory = viewModel::showCreateCategoryDialog,
                        onCreateDefaults = viewModel::createDefaultCategories,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(AppConstants.Dimensions.PADDING_LARGE),
                        verticalArrangement = Arrangement.spacedBy(AppConstants.Dimensions.SPACING_SMALL)
                    ) {
                        items(
                            items = uiState.categories,
                            key = { it.id }
                        ) { category ->
                            CategoryItem(
                                category = category,
                                onEdit = { viewModel.showEditCategoryDialog(category) },
                                onDelete = { viewModel.showDeleteConfirmation(category) }
                            )
                        }
                    }
                }
            }
            
            // Error handling
            uiState.error?.let { error ->
                LaunchedEffect(error) {
                    // In a real app, you might show a snackbar here
                    viewModel.clearError()
                }
            }
        }
    }

    // Dialogs
    if (uiState.showCreateDialog) {
        CategoryDialog(
            title = AppConstants.UIText.CREATE_CATEGORY,
            category = null,
            onDismiss = viewModel::hideDialogs,
            onConfirm = { name, description, color, icon ->
                viewModel.createCategory(name, description, color, icon)
            }
        )
    }

    if (uiState.showEditDialog && uiState.editingCategory != null) {
        CategoryDialog(
            title = AppConstants.UIText.EDIT_CATEGORY,
            category = uiState.editingCategory,
            onDismiss = viewModel::hideDialogs,
            onConfirm = { name, description, color, icon ->
                viewModel.updateCategory(
                    uiState.editingCategory!!.id,
                    name,
                    description,
                    color,
                    icon
                )
            }
        )
    }

    if (uiState.showDeleteDialog && uiState.editingCategory != null) {
        DeleteConfirmationDialog(
            categoryName = uiState.editingCategory!!.name,
            onDismiss = viewModel::hideDialogs,
            onConfirm = {
                viewModel.deleteCategory(uiState.editingCategory!!.id)
            }
        )
    }
}

/**
 * Empty state when no categories exist
 */
@Composable
private fun EmptyState(
    onCreateCategory: () -> Unit,
    onCreateDefaults: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(AppConstants.Dimensions.PADDING_EXTRA_LARGE),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppConstants.Dimensions.SPACING_LARGE)
    ) {
        Icon(
            imageVector = Icons.Default.Category,
            contentDescription = null,
            modifier = Modifier.size(AppConstants.Dimensions.EMPTY_STATE_ICON_SIZE),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Text(
            text = AppConstants.UIText.NO_CATEGORIES,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = AppConstants.UIText.CREATE_CATEGORY_PROMPT,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Column(
            verticalArrangement = Arrangement.spacedBy(AppConstants.Dimensions.SPACING_SMALL)
        ) {
            Button(
                onClick = onCreateCategory,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(AppConstants.Dimensions.EMPTY_STATE_BUTTON_ICON_SIZE)
                )
                Spacer(modifier = Modifier.width(AppConstants.Dimensions.SPACING_SMALL))
                Text(AppConstants.UIText.CREATE_CATEGORY)
            }
            
            OutlinedButton(
                onClick = onCreateDefaults,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Restore,
                    contentDescription = null,
                    modifier = Modifier.size(AppConstants.Dimensions.EMPTY_STATE_BUTTON_ICON_SIZE)
                )
                Spacer(modifier = Modifier.width(AppConstants.Dimensions.SPACING_SMALL))
                Text(AppConstants.UIText.ADD_DEFAULT_CATEGORIES)
            }
        }
    }
}

/**
 * Individual category item
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoryItem(
    category: Category,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val baseColor = remember(category.colorHex) {
        runCatching { 
            Color(category.colorHex.toColorInt())
        }.getOrElse { 
            Color("#1976D2".toColorInt())
        }
    }

    val iconGradient = remember(baseColor) {
        Brush.linearGradient(
            colors = listOf(
                Color(
                    ColorUtils.blendARGB(
                        baseColor.toArgb(),
                        Color.White.copy(alpha = 0.85f).toArgb(),
                        0.2f
                    )
                ),
                Color(
                    ColorUtils.blendARGB(
                        baseColor.toArgb(),
                        Color.Black.copy(alpha = 0.55f).toArgb(),
                        0.1f
                    )
                )
            )
        )
    }

    val accentContent = remember(baseColor) {
        if (baseColor.luminance() > 0.5f) Color.Black else Color.White
    }
    val iconVector: ImageVector = remember(category.iconName) {
        getIconFromName(category.iconName) ?: Icons.Default.Category
    }

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp)
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = AppConstants.Dimensions.CARD_ELEVATION_DEFAULT)
    ) {
        val surfaceColorAt8dp = MaterialTheme.colorScheme.surfaceColorAtElevation(AppConstants.Dimensions.SPACING_SMALL)
        val chipSurfaceColor = MaterialTheme.colorScheme.surfaceColorAtElevation(AppConstants.Dimensions.CARD_ELEVATION_DEFAULT)
        val colorChipContainer = remember(baseColor, surfaceColorAt8dp) {
            Color(
                ColorUtils.blendARGB(
                    baseColor.toArgb(),
                    surfaceColorAt8dp.toArgb(),
                    0.55f
                )
            )
        }
        val colorChipContent = remember(colorChipContainer) {
            if (colorChipContainer.luminance() > 0.5f) Color.Black else Color.White
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(AppConstants.Dimensions.SPACING_LARGE)
        ) {
            Box(Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    IconButton(
                        onClick = onEdit,
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = AppConstants.UIText.EDIT_CATEGORY_CONTENT_DESC
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = AppConstants.UIText.DELETE_CATEGORY_CONTENT_DESC
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppConstants.Dimensions.SPACING_LARGE),
                    modifier = Modifier.padding(end = AppConstants.Dimensions.CATEGORY_ACTION_BUTTON_PADDING) // prevent overlap
                ) {
                    Box(
                        modifier = Modifier
                            .size(AppConstants.Dimensions.CATEGORY_ITEM_ICON_SIZE)
                            .background(iconGradient, shape = MaterialTheme.shapes.large),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = iconVector,
                            contentDescription = null,
                            tint = accentContent,
                            modifier = Modifier.size(AppConstants.Dimensions.CATEGORY_ITEM_ICON_INNER_SIZE)
                        )
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        AnimatedVisibility(visible = !category.description.isNullOrBlank()) {
                            Text(
                                text = category.description.orEmpty(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(AppConstants.Dimensions.SPACING_SMALL),
                verticalArrangement = Arrangement.spacedBy(AppConstants.Dimensions.SPACING_SMALL)
            ) {
                AssistChip(
                    onClick = {},
                    enabled = false,
                    label = {
                        Text(category.colorHex.uppercase())
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Palette, contentDescription = null)
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        disabledContainerColor = colorChipContainer,
                        disabledLabelColor = colorChipContent,
                        disabledLeadingIconContentColor = colorChipContent
                    )
                )

                category.iconName?.let { iconName ->
                    AssistChip(
                        onClick = {},
                        enabled = false,
                        label = {
                            Text(iconName.asDisplayName())
                        },
                        leadingIcon = {
                            Icon(iconVector, contentDescription = null)
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            disabledContainerColor = chipSurfaceColor,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledLeadingIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    }
}

/**
 * Delete confirmation dialog
 */
@Composable
private fun DeleteConfirmationDialog(
    categoryName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(AppConstants.UIText.DELETE_CATEGORY) },
        text = {
            Text(String.format(AppConstants.UIText.DELETE_CATEGORY_CONFIRMATION, categoryName))
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(AppConstants.DialogText.DELETE_BUTTON)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(AppConstants.DialogText.CANCEL_BUTTON)
            }
        }
    )
}

/**
 * Preview for CategoriesScreen
 */
@Preview(showBackground = true)
@Composable
fun CategoriesScreenPreview() {
    WalletTheme {
        // Preview would need mock data
    }
}