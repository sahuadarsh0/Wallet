package com.technitedminds.wallet.presentation.screens.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.technitedminds.wallet.domain.model.Category
import com.technitedminds.wallet.domain.usecase.category.GetCategoriesUseCase
import com.technitedminds.wallet.domain.usecase.category.ManageCategoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing categories screen state and operations
 */
@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val manageCategoryUseCase: ManageCategoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoriesUiState())
    val uiState: StateFlow<CategoriesUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<CategoriesEvent>()
    val events: SharedFlow<CategoriesEvent> = _events.asSharedFlow()

    init {
        loadCategories()
    }

    /**
     * Load all categories
     */
    private fun loadCategories() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                getCategoriesUseCase().collect { categories ->
                    _uiState.value = _uiState.value.copy(
                        categories = categories,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load categories"
                )
            }
        }
    }

    /**
     * Show create category dialog
     */
    fun showCreateCategoryDialog() {
        _uiState.value = _uiState.value.copy(
            showCreateDialog = true,
            editingCategory = null
        )
    }

    /**
     * Show edit category dialog
     */
    fun showEditCategoryDialog(category: Category) {
        _uiState.value = _uiState.value.copy(
            showEditDialog = true,
            editingCategory = category
        )
    }

    /**
     * Hide dialogs
     */
    fun hideDialogs() {
        _uiState.value = _uiState.value.copy(
            showCreateDialog = false,
            showEditDialog = false,
            showDeleteDialog = false,
            editingCategory = null
        )
    }

    /**
     * Show delete confirmation dialog
     */
    fun showDeleteConfirmation(category: Category) {
        _uiState.value = _uiState.value.copy(
            showDeleteDialog = true,
            editingCategory = category
        )
    }

    /**
     * Create a new category
     */
    fun createCategory(name: String, description: String, colorHex: String, iconName: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val category = Category(
                id = generateCategoryId(),
                name = name.trim(),
                description = description.trim().takeIf { it.isNotEmpty() },
                colorHex = colorHex,
                iconName = iconName,
                sortOrder = _uiState.value.categories.size
            )
            
            val result = manageCategoryUseCase.addCategory(category)
            
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        showCreateDialog = false
                    )
                    _events.emit(CategoriesEvent.CategoryCreated(category))
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to create category"
                    )
                }
            )
        }
    }

    /**
     * Update an existing category
     */
    fun updateCategory(
        categoryId: String,
        name: String,
        description: String,
        colorHex: String,
        iconName: String?
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val existingCategory = _uiState.value.categories.find { it.id == categoryId }
            if (existingCategory == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Category not found"
                )
                return@launch
            }
            
            val updatedCategory = existingCategory.copy(
                name = name.trim(),
                description = description.trim().takeIf { it.isNotEmpty() },
                colorHex = colorHex,
                iconName = iconName
            ).withUpdatedTimestamp()
            
            val result = manageCategoryUseCase.updateCategory(updatedCategory)
            
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        showEditDialog = false,
                        editingCategory = null
                    )
                    _events.emit(CategoriesEvent.CategoryUpdated(updatedCategory))
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to update category"
                    )
                }
            )
        }
    }

    /**
     * Delete a category
     */
    fun deleteCategory(categoryId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val result = manageCategoryUseCase.deleteCategory(categoryId)
            
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        showDeleteDialog = false,
                        editingCategory = null
                    )
                    _events.emit(CategoriesEvent.CategoryDeleted(categoryId))
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to delete category"
                    )
                }
            )
        }
    }

    /**
     * Create default categories
     */
    fun createDefaultCategories() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val result = manageCategoryUseCase.resetDefaultCategories()
            
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _events.emit(CategoriesEvent.DefaultCategoriesCreated)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to create default categories"
                    )
                }
            )
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Generate a unique category ID
     */
    private fun generateCategoryId(): String {
        return "category_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }
}

/**
 * UI state for categories screen
 */
data class CategoriesUiState(
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showCreateDialog: Boolean = false,
    val showEditDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val editingCategory: Category? = null
)

/**
 * Events for categories screen
 */
sealed class CategoriesEvent {
    data class CategoryCreated(val category: Category) : CategoriesEvent()
    data class CategoryUpdated(val category: Category) : CategoriesEvent()
    data class CategoryDeleted(val categoryId: String) : CategoriesEvent()
    object DefaultCategoriesCreated : CategoriesEvent()
}