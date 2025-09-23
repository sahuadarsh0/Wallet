package com.technitedminds.wallet.domain.repository

import com.technitedminds.wallet.domain.model.Category
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for category operations.
 */
interface CategoryRepository {
    
    /** Get all categories */
    fun getAllCategories(): Flow<List<Category>>
    
    /** Get category by ID */
    suspend fun getCategoryById(id: String): Category?
    
    /** Get category name by ID (for quick lookups) */
    suspend fun getCategoryName(id: String): String?
    
    /** Add a new category */
    suspend fun addCategory(category: Category)
    
    /** Update an existing category */
    suspend fun updateCategory(category: Category)
    
    /** Delete a category */
    suspend fun deleteCategory(id: String)
    
    /** Check if category exists */
    suspend fun categoryExists(id: String): Boolean
}