package com.technitedminds.wallet.presentation.screens.categories

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.technitedminds.wallet.domain.model.Category
import com.technitedminds.wallet.presentation.components.common.getIconFromName
import com.technitedminds.wallet.ui.theme.WalletTheme
import androidx.core.graphics.toColorInt

/**
 * Categories screen for managing card categories
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    onNavigateBack: () -> Unit,
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
                title = { Text("Categories") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::createDefaultCategories) {
                        Icon(
                            imageVector = Icons.Default.Restore,
                            contentDescription = "Restore defaults"
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
                    contentDescription = "Add category"
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
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.categories) { category ->
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
            title = "Create Category",
            category = null,
            onDismiss = viewModel::hideDialogs,
            onConfirm = { name, description, color, icon ->
                viewModel.createCategory(name, description, color, icon)
            }
        )
    }

    if (uiState.showEditDialog && uiState.editingCategory != null) {
        CategoryDialog(
            title = "Edit Category",
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
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Category,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Text(
            text = "No Categories",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "Create categories to organize your cards better",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onCreateCategory,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create Category")
            }
            
            OutlinedButton(
                onClick = onCreateDefaults,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Restore,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Default Categories")
            }
        }
    }
}

/**
 * Individual category item
 */
@Composable
private fun CategoryItem(
    category: Category,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Category preview using CategoryChip
            com.technitedminds.wallet.presentation.components.common.CategoryChip(
                category = category.name,
                icon = getIconFromName(category.iconName),
                color = Color(category.colorHex.toColorInt())
            )
            
            // Category info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                category.description?.let { description ->
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Action buttons
            Row {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit category"
                    )
                }
                
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete category",
                        tint = MaterialTheme.colorScheme.error
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
        title = { Text("Delete Category") },
        text = {
            Text("Are you sure you want to delete \"$categoryName\"? Cards in this category will be moved to the default category.")
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
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