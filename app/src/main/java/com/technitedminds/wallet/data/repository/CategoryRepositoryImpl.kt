package com.technitedminds.wallet.data.repository

import com.technitedminds.wallet.domain.model.Category
import com.technitedminds.wallet.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Simple in-memory implementation of CategoryRepository.
 * This can be replaced with a proper database implementation later.
 */
@Singleton
class CategoryRepositoryImpl @Inject constructor() : CategoryRepository {
    
    private val _categories = MutableStateFlow(
        Category.PREDEFINED_CATEGORIES.toMutableList()
    )
    
    override fun getAllCategories(): Flow<List<Category>> {
        return _categories.asStateFlow()
    }
    
    override suspend fun getCategoryById(id: String): Category? {
        return _categories.value.find { it.id == id }
    }
    
    override suspend fun getCategoryName(id: String): String? {
        return _categories.value.find { it.id == id }?.name
    }
    
    override suspend fun addCategory(category: Category) {
        val currentList = _categories.value.toMutableList()
        currentList.add(category)
        _categories.value = currentList
    }
    
    override suspend fun updateCategory(category: Category) {
        val currentList = _categories.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == category.id }
        if (index != -1) {
            currentList[index] = category
            _categories.value = currentList
        }
    }
    
    override suspend fun deleteCategory(id: String) {
        val currentList = _categories.value.toMutableList()
        currentList.removeAll { it.id == id }
        _categories.value = currentList
    }
    
    override suspend fun categoryExists(id: String): Boolean {
        return _categories.value.any { it.id == id }
    }
}