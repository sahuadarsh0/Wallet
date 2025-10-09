package com.technitedminds.wallet.domain.usecase.category

import com.technitedminds.wallet.domain.model.Category
import com.technitedminds.wallet.domain.repository.CategoryRepository
import com.technitedminds.wallet.domain.repository.CardRepository
import javax.inject.Inject

/**
 * Use case for managing categories (create, update, delete operations).
 */
class ManageCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val cardRepository: CardRepository
) {
    
    suspend fun addCategory(category: Category): Result<Unit> {
        return try {
            // Validate category data
            if (category.name.isBlank()) {
                return Result.failure(Exception("Category name cannot be empty"))
            }
            
            if (categoryRepository.categoryExists(category.id)) {
                return Result.failure(Exception("Category with this ID already exists"))
            }
            
            categoryRepository.addCategory(category)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateCategory(category: Category): Result<Unit> {
        return try {
            // Validate category data
            if (category.name.isBlank()) {
                return Result.failure(Exception("Category name cannot be empty"))
            }
            
            if (!categoryRepository.categoryExists(category.id)) {
                return Result.failure(Exception("Category not found"))
            }
            
            categoryRepository.updateCategory(category.withUpdatedTimestamp())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteCategory(categoryId: String): Result<Unit> {
        return try {
            if (!categoryRepository.categoryExists(categoryId)) {
                return Result.failure(Exception("Category not found"))
            }
            
            // Prevent deletion of the default category
            if (categoryId == Category.DEFAULT.id) {
                return Result.failure(Exception("Cannot delete the default category"))
            }
            
            // Ensure the default category exists before reassigning cards
            if (!categoryRepository.categoryExists(Category.DEFAULT.id)) {
                categoryRepository.addCategory(Category.DEFAULT)
            }
            
            // Reassign all cards from this category to the default category
            // This ensures no cards are orphaned when the category is deleted
            cardRepository.updateCardsCategory(categoryId, Category.DEFAULT.id)
            
            // Now safely delete the category
            categoryRepository.deleteCategory(categoryId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun createDefaultCategories(): Result<Unit> {
        return try {
            Category.PREDEFINED_CATEGORIES.forEach { category ->
                if (!categoryRepository.categoryExists(category.id)) {
                    categoryRepository.addCategory(category)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun resetDefaultCategories(): Result<Unit> {
        return try {
            // Reset all predefined categories to their original state
            Category.PREDEFINED_CATEGORIES.forEach { category ->
                if (categoryRepository.categoryExists(category.id)) {
                    // Update existing category to restore original values
                    categoryRepository.updateCategory(category.withUpdatedTimestamp())
                } else {
                    // Add missing category
                    categoryRepository.addCategory(category)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}