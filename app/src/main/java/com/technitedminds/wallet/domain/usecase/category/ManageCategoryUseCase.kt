package com.technitedminds.wallet.domain.usecase.category

import com.technitedminds.wallet.domain.model.Category
import com.technitedminds.wallet.domain.repository.CategoryRepository
import javax.inject.Inject

/**
 * Use case for managing categories (create, update, delete operations).
 */
class ManageCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
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
            
            // Note: The repository implementation should handle reassigning cards
            // to a default category before deleting the category
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
}