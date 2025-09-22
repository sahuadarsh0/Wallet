package com.technitedminds.wallet.domain.usecase.category

import com.technitedminds.wallet.domain.model.Category
import com.technitedminds.wallet.domain.repository.CardRepository
import com.technitedminds.wallet.domain.repository.CategoryRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/** Use case for retrieving categories with various filtering and sorting options. */
class GetCategoriesUseCase
@Inject
constructor(
        private val categoryRepository: CategoryRepository,
        private val cardRepository: CardRepository
) {

    /**
     * Gets all categories
     * @return Flow of list of all categories
     */
    suspend fun getAllCategories(): Flow<List<Category>> {
        return categoryRepository.getAllCategories()
    }

    /**
     * Gets a specific category by ID
     * @param categoryId The ID of the category to retrieve
     * @return The category if found, null otherwise
     */
    suspend fun getCategoryById(categoryId: String): Category? {
        return categoryRepository.getCategoryById(categoryId)
    }

    /**
     * Gets categories with card counts
     * @return Flow of list of categories with their card counts
     */
    suspend fun getCategoriesWithCardCounts(): Flow<List<CategoryWithCardCount>> {
        return combine(categoryRepository.getAllCategories(), cardRepository.getAllCards()) {
                categories,
                cards ->
            categories.map { category ->
                val cardCount = cards.count { it.categoryId == category.id }
                CategoryWithCardCount(category, cardCount)
            }
        }
    }

    /**
     * Gets default system categories
     * @return Flow of list of default categories
     */
    suspend fun getDefaultCategories(): Flow<List<Category>> {
        return categoryRepository.getDefaultCategories()
    }

    /**
     * Gets user-created categories (non-default)
     * @return Flow of list of user-created categories
     */
    suspend fun getUserCategories(): Flow<List<Category>> {
        return categoryRepository.getUserCategories()
    }

    /**
     * Gets categories sorted by name
     * @param ascending Whether to sort in ascending order
     * @return Flow of list of categories sorted by name
     */
    suspend fun getCategoriesSorted(ascending: Boolean = true): Flow<List<Category>> {
        return categoryRepository.getCategoriesSorted(ascending)
    }

    /**
     * Gets categories with filtering options
     * @param request The filter request
     * @return Flow of filtered categories
     */
    suspend operator fun invoke(request: GetCategoriesRequest): Flow<List<Category>> {
        return when {
            request.includeDefault && request.includeUser -> {
                categoryRepository.getAllCategories()
            }
            request.includeDefault && !request.includeUser -> {
                categoryRepository.getDefaultCategories()
            }
            !request.includeDefault && request.includeUser -> {
                categoryRepository.getUserCategories()
            }
            else -> {
                // Neither default nor user categories requested - return empty flow
                kotlinx.coroutines.flow.flowOf(emptyList())
            }
        }
    }

    /**
     * Gets category statistics
     * @return CategoryStats with various counts and information
     */
    suspend fun getCategoryStats(): CategoryStats {
        val totalCategories = categoryRepository.getCategoryCount()
        val userCategories = categoryRepository.getUserCategoryCount()
        val defaultCategories = totalCategories - userCategories

        return CategoryStats(
                totalCategories = totalCategories,
                defaultCategories = defaultCategories,
                userCategories = userCategories
        )
    }

    /**
     * Checks if a category name is available (not already used)
     * @param name The category name to check
     * @param excludeId Optional category ID to exclude from the check (for updates)
     * @return True if the name is available, false if already used
     */
    suspend fun isCategoryNameAvailable(name: String, excludeId: String? = null): Boolean {
        return !categoryRepository.categoryNameExists(name, excludeId)
    }

    /**
     * Gets categories that can be deleted (non-default categories)
     * @return Flow of list of deletable categories
     */
    suspend fun getDeletableCategories(): Flow<List<Category>> {
        return combine(categoryRepository.getUserCategories(), cardRepository.getAllCards()) {
                userCategories,
                cards ->
            userCategories.filter { category ->
                // Only include categories that can be deleted
                category.canBeDeleted()
            }
        }
    }

    /**
     * Gets empty categories (categories with no cards)
     * @return Flow of list of categories with no cards
     */
    suspend fun getEmptyCategories(): Flow<List<Category>> {
        return combine(categoryRepository.getAllCategories(), cardRepository.getAllCards()) {
                categories,
                cards ->
            categories.filter { category -> cards.none { it.categoryId == category.id } }
        }
    }
}

/** Request for getting categories with filters */
data class GetCategoriesRequest(
        val includeDefault: Boolean = true,
        val includeUser: Boolean = true,
        val sortByName: Boolean = true,
        val ascending: Boolean = true
)

/** Category with its card count */
data class CategoryWithCardCount(val category: Category, val cardCount: Int) {
    val isEmpty: Boolean
        get() = cardCount == 0
}

/** Statistics about categories */
data class CategoryStats(
        val totalCategories: Int,
        val defaultCategories: Int,
        val userCategories: Int
)
