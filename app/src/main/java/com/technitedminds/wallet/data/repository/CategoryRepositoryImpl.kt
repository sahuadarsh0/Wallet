package com.technitedminds.wallet.data.repository

import com.technitedminds.wallet.data.local.database.dao.CategoryDao
import com.technitedminds.wallet.data.local.preferences.SimplePreferencesManager
import com.technitedminds.wallet.data.mapper.toDomainModel
import com.technitedminds.wallet.data.mapper.toDomainModels
import com.technitedminds.wallet.data.mapper.toEntities
import com.technitedminds.wallet.data.mapper.toEntity
import com.technitedminds.wallet.domain.model.Category
import com.technitedminds.wallet.domain.repository.CategoryRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Implementation of CategoryRepository using Room database and DataStore. Handles category
 * persistence and integrates with user preferences.
 */
@Singleton
class CategoryRepositoryImpl
@Inject
constructor(
        private val categoryDao: CategoryDao,
        private val preferencesManager: SimplePreferencesManager
) : CategoryRepository {

    override suspend fun getAllCategories(): Flow<List<Category>> {
        return categoryDao.getAllCategories().map { entities -> entities.toDomainModels() }
    }

    override suspend fun getCategoryById(id: String): Category? {
        return try {
            categoryDao.getCategoryById(id)?.toDomainModel()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getDefaultCategories(): Flow<List<Category>> {
        return categoryDao.getDefaultCategories().map { entities -> entities.toDomainModels() }
    }

    override suspend fun getUserCategories(): Flow<List<Category>> {
        return categoryDao.getUserCategories().map { entities -> entities.toDomainModels() }
    }

    override suspend fun insertCategory(category: Category): String {
        return try {
            categoryDao.insertCategory(category.toEntity())
            category.id
        } catch (e: Exception) {
            throw Exception("Failed to insert category: ${e.message}", e)
        }
    }

    override suspend fun updateCategory(category: Category) {
        try {
            categoryDao.updateCategory(category.toEntity())
        } catch (e: Exception) {
            throw Exception("Failed to update category: ${e.message}", e)
        }
    }

    override suspend fun deleteCategory(id: String): Boolean {
        return try {
            val deletedRows = categoryDao.deleteCategoryById(id)
            deletedRows > 0
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun categoryNameExists(name: String, excludeId: String?): Boolean {
        return try {
            categoryDao.categoryNameExists(name, excludeId)
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getCategoryCount(): Int {
        return try {
            categoryDao.getCategoryCount()
        } catch (e: Exception) {
            0
        }
    }

    override suspend fun getUserCategoryCount(): Int {
        return try {
            categoryDao.getUserCategoryCount()
        } catch (e: Exception) {
            0
        }
    }

    override suspend fun categoryExists(id: String): Boolean {
        return try {
            categoryDao.categoryExists(id)
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun initializeDefaultCategories() {
        try {
            // Check if default categories are already initialized
            val hasDefaultCategories = categoryDao.hasDefaultCategories()

            if (!hasDefaultCategories) {
                // Get default categories from preferences manager
                val defaultCategories = preferencesManager.initializeDefaultCategoriesIfNeeded()

                if (defaultCategories.isNotEmpty()) {
                    // Insert default categories
                    categoryDao.insertCategories(defaultCategories.toEntities())

                    // Set default category in preferences
                    preferencesManager.setDefaultCategoryId(Category.DEFAULT_CATEGORY_ID)
                }
            }
        } catch (e: Exception) {
            throw Exception("Failed to initialize default categories: ${e.message}", e)
        }
    }

    override suspend fun restoreDefaultCategories() {
        try {
            // Get expected default categories
            val expectedDefaultCategories = createDefaultCategories()
            val currentDefaultCount = categoryDao.getDefaultCategoryCount()

            // If we're missing some default categories, restore them
            if (currentDefaultCount < expectedDefaultCategories.size) {
                // For simplicity, just insert all expected default categories
                // Room will handle conflicts with REPLACE strategy
                categoryDao.insertCategories(expectedDefaultCategories.toEntities())
            }
        } catch (e: Exception) {
            throw Exception("Failed to restore default categories: ${e.message}", e)
        }
    }

    override suspend fun getCategoriesSorted(ascending: Boolean): Flow<List<Category>> {
        return categoryDao.getCategoriesSorted(ascending).map { entities ->
            entities.toDomainModels()
        }
    }

    /** Gets categories with card counts */
    suspend fun getCategoriesWithCardCounts(): Flow<List<CategoryWithCardCount>> {
        return categoryDao.getCategoriesWithCardCounts().map { results ->
            results.map { result ->
                CategoryWithCardCount(
                        category =
                                Category(
                                        id = result.id,
                                        name = result.name,
                                        iconResId = result.iconResId,
                                        colorHex = result.colorHex,
                                        isDefault = result.isDefault,
                                        createdAt = result.createdAt,
                                        updatedAt = result.updatedAt
                                ),
                        cardCount = result.cardCount
                )
            }
        }
    }

    /** Gets empty categories (categories with no cards) */
    suspend fun getEmptyCategories(): Flow<List<Category>> {
        return categoryDao.getEmptyCategories().map { entities -> entities.toDomainModels() }
    }

    /** Deletes all user-created categories */
    suspend fun deleteAllUserCategories() {
        try {
            categoryDao.deleteAllUserCategories()
        } catch (e: Exception) {
            throw Exception("Failed to delete user categories: ${e.message}", e)
        }
    }

    /** Data class for category with card count */
    data class CategoryWithCardCount(val category: Category, val cardCount: Int)

    /** Creates default categories */
    private fun createDefaultCategories(): List<Category> {
        val currentTime = System.currentTimeMillis()

        return listOf(
                Category(
                        id = Category.DEFAULT_CATEGORY_ID,
                        name = "Uncategorized",
                        iconResId = android.R.drawable.ic_menu_gallery,
                        colorHex = "#757575",
                        isDefault = true,
                        createdAt = currentTime,
                        updatedAt = currentTime
                ),
                Category(
                        id = Category.CREDIT_CARDS_CATEGORY_ID,
                        name = "Credit Cards",
                        iconResId = android.R.drawable.ic_menu_gallery,
                        colorHex = "#2196F3",
                        isDefault = true,
                        createdAt = currentTime,
                        updatedAt = currentTime
                ),
                Category(
                        id = Category.DEBIT_CARDS_CATEGORY_ID,
                        name = "Debit Cards",
                        iconResId = android.R.drawable.ic_menu_gallery,
                        colorHex = "#4CAF50",
                        isDefault = true,
                        createdAt = currentTime,
                        updatedAt = currentTime
                ),
                Category(
                        id = Category.GIFT_CARDS_CATEGORY_ID,
                        name = "Gift Cards",
                        iconResId = android.R.drawable.ic_menu_gallery,
                        colorHex = "#FF9800",
                        isDefault = true,
                        createdAt = currentTime,
                        updatedAt = currentTime
                ),
                Category(
                        id = Category.MEMBERSHIP_CATEGORY_ID,
                        name = "Membership",
                        iconResId = android.R.drawable.ic_menu_gallery,
                        colorHex = "#9C27B0",
                        isDefault = true,
                        createdAt = currentTime,
                        updatedAt = currentTime
                )
        )
    }
}
