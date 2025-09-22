package com.technitedminds.wallet.domain.repository

import com.technitedminds.wallet.domain.model.Category
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for category data operations. Provides CRUD operations for managing card
 * categories.
 */
interface CategoryRepository {

    /**
     * Retrieves all categories as a Flow for reactive updates
     * @return Flow of list of all categories
     */
    suspend fun getAllCategories(): Flow<List<Category>>

    /**
     * Retrieves a specific category by its ID
     * @param id The unique identifier of the category
     * @return The category if found, null otherwise
     */
    suspend fun getCategoryById(id: String): Category?

    /**
     * Retrieves all default system categories
     * @return Flow of list of default categories
     */
    suspend fun getDefaultCategories(): Flow<List<Category>>

    /**
     * Retrieves all user-created categories (non-default)
     * @return Flow of list of user-created categories
     */
    suspend fun getUserCategories(): Flow<List<Category>>

    /**
     * Inserts a new category into the repository
     * @param category The category to insert
     * @return The ID of the inserted category
     */
    suspend fun insertCategory(category: Category): String

    /**
     * Updates an existing category
     * @param category The category with updated information
     */
    suspend fun updateCategory(category: Category)

    /**
     * Deletes a category by its ID Only non-default categories can be deleted
     * @param id The ID of the category to delete
     * @return True if deletion was successful, false if category is default
     */
    suspend fun deleteCategory(id: String): Boolean

    /**
     * Checks if a category with the given name already exists
     * @param name The category name to check
     * @param excludeId Optional ID to exclude from the check (for updates)
     * @return True if a category with this name exists, false otherwise
     */
    suspend fun categoryNameExists(name: String, excludeId: String? = null): Boolean

    /**
     * Gets the total count of categories
     * @return Total number of categories
     */
    suspend fun getCategoryCount(): Int

    /**
     * Gets the count of user-created categories
     * @return Number of user-created categories
     */
    suspend fun getUserCategoryCount(): Int

    /**
     * Checks if a category with the given ID exists
     * @param id The category ID to check
     * @return True if the category exists, false otherwise
     */
    suspend fun categoryExists(id: String): Boolean

    /**
     * Initializes default categories if they don't exist This should be called on app first launch
     */
    suspend fun initializeDefaultCategories()

    /** Restores default categories that may have been accidentally deleted */
    suspend fun restoreDefaultCategories()

    /**
     * Gets categories sorted by name
     * @param ascending Whether to sort in ascending order
     * @return Flow of list of categories sorted by name
     */
    suspend fun getCategoriesSorted(ascending: Boolean = true): Flow<List<Category>>
}
