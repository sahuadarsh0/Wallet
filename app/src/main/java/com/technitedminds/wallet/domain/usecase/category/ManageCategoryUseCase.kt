package com.technitedminds.wallet.domain.usecase.category

import com.technitedminds.wallet.domain.model.Category
import com.technitedminds.wallet.domain.repository.CardRepository
import com.technitedminds.wallet.domain.repository.CategoryRepository
import java.util.UUID
import javax.inject.Inject

/** Use case for managing categories (create, update, delete operations). */
class ManageCategoryUseCase
@Inject
constructor(
        private val categoryRepository: CategoryRepository,
        private val cardRepository: CardRepository
) {

    /**
     * Creates a new category
     * @param request The category creation request
     * @return Result containing the created category ID or error
     */
    suspend fun createCategory(request: CreateCategoryRequest): Result<String> {
        return try {
            // Validate the request
            val validationResult = validateCreateRequest(request)
            if (!validationResult.isValid) {
                return Result.failure(IllegalArgumentException(validationResult.errorMessage))
            }

            // Check if name already exists
            val nameExists = categoryRepository.categoryNameExists(request.name)
            if (nameExists) {
                return Result.failure(
                        IllegalArgumentException("Category name already exists: ${request.name}")
                )
            }

            // Create the category
            val category =
                    Category(
                            id = request.categoryId,
                            name = request.name.trim(),
                            iconResId = request.iconResId,
                            colorHex = request.colorHex,
                            isDefault = false, // User-created categories are never default
                            createdAt = System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis()
                    )

            // Save the category
            val categoryId = categoryRepository.insertCategory(category)
            Result.success(categoryId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Updates an existing category
     * @param request The category update request
     * @return Result indicating success or failure
     */
    suspend fun updateCategory(request: UpdateCategoryRequest): Result<Unit> {
        return try {
            // Check if category exists
            val existingCategory =
                    categoryRepository.getCategoryById(request.categoryId)
                            ?: return Result.failure(
                                    IllegalArgumentException(
                                            "Category not found: ${request.categoryId}"
                                    )
                            )

            // Check if category can be modified
            if (!existingCategory.canBeModified()) {
                return Result.failure(
                        IllegalArgumentException(
                                "Cannot modify default category: ${existingCategory.name}"
                        )
                )
            }

            // Validate the request
            val validationResult = validateUpdateRequest(request, existingCategory)
            if (!validationResult.isValid) {
                return Result.failure(IllegalArgumentException(validationResult.errorMessage))
            }

            // Check if new name already exists (if name is being changed)
            if (request.name != null && request.name != existingCategory.name) {
                val nameExists =
                        categoryRepository.categoryNameExists(request.name, request.categoryId)
                if (nameExists) {
                    return Result.failure(
                            IllegalArgumentException(
                                    "Category name already exists: ${request.name}"
                            )
                    )
                }
            }

            // Create updated category
            val updatedCategory =
                    existingCategory.copy(
                            name = request.name?.trim() ?: existingCategory.name,
                            iconResId = request.iconResId ?: existingCategory.iconResId,
                            colorHex = request.colorHex ?: existingCategory.colorHex,
                            updatedAt = System.currentTimeMillis()
                    )

            // Update the category
            categoryRepository.updateCategory(updatedCategory)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Deletes a category
     * @param categoryId The ID of the category to delete
     * @param reassignToCategory Optional category ID to reassign cards to
     * @return Result indicating success or failure
     */
    suspend fun deleteCategory(
            categoryId: String,
            reassignToCategory: String? = null
    ): Result<Unit> {
        return try {
            // Check if category exists
            val category =
                    categoryRepository.getCategoryById(categoryId)
                            ?: return Result.failure(
                                    IllegalArgumentException("Category not found: $categoryId")
                            )

            // Check if category can be deleted
            if (!category.canBeDeleted()) {
                return Result.failure(
                        IllegalArgumentException("Cannot delete default category: ${category.name}")
                )
            }

            // Check if reassign category exists (if provided)
            if (reassignToCategory != null) {
                val reassignCategoryExists = categoryRepository.categoryExists(reassignToCategory)
                if (!reassignCategoryExists) {
                    return Result.failure(
                            IllegalArgumentException(
                                    "Reassign category not found: $reassignToCategory"
                            )
                    )
                }
            }

            // Get card count for this category
            val cardCount = cardRepository.getCardCountByCategory(categoryId)

            if (cardCount > 0) {
                if (reassignToCategory == null) {
                    return Result.failure(
                            IllegalArgumentException(
                                    "Category has $cardCount cards. Please specify a category to reassign them to."
                            )
                    )
                }

                // Reassign cards to the new category
                // Note: This would require a method in CardRepository to update cards by category
                // For now, we'll assume this functionality exists
                reassignCardsToCategory(categoryId, reassignToCategory)
            }

            // Delete the category
            val deleted = categoryRepository.deleteCategory(categoryId)
            if (!deleted) {
                return Result.failure(
                        IllegalArgumentException("Failed to delete category: $categoryId")
                )
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Initializes default categories if they don't exist
     * @return Result indicating success or failure
     */
    suspend fun initializeDefaultCategories(): Result<Unit> {
        return try {
            categoryRepository.initializeDefaultCategories()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Restores default categories that may have been accidentally deleted
     * @return Result indicating success or failure
     */
    suspend fun restoreDefaultCategories(): Result<Unit> {
        return try {
            categoryRepository.restoreDefaultCategories()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Validates a create category request */
    private suspend fun validateCreateRequest(request: CreateCategoryRequest): ValidationResult {
        // Validate name
        if (request.name.isBlank()) {
            return ValidationResult(false, "Category name cannot be empty")
        }

        if (request.name.length > 50) {
            return ValidationResult(false, "Category name cannot exceed 50 characters")
        }

        // Validate color hex
        if (!isValidColorHex(request.colorHex)) {
            return ValidationResult(false, "Invalid color hex format")
        }

        // Validate icon resource ID (basic check)
        if (request.iconResId <= 0) {
            return ValidationResult(false, "Invalid icon resource ID")
        }

        return ValidationResult(true)
    }

    /** Validates an update category request */
    private suspend fun validateUpdateRequest(
            request: UpdateCategoryRequest,
            existingCategory: Category
    ): ValidationResult {
        // Validate name if provided
        request.name?.let { name ->
            if (name.isBlank()) {
                return ValidationResult(false, "Category name cannot be empty")
            }
            if (name.length > 50) {
                return ValidationResult(false, "Category name cannot exceed 50 characters")
            }
        }

        // Validate color hex if provided
        request.colorHex?.let { colorHex ->
            if (!isValidColorHex(colorHex)) {
                return ValidationResult(false, "Invalid color hex format")
            }
        }

        // Validate icon resource ID if provided
        request.iconResId?.let { iconResId ->
            if (iconResId <= 0) {
                return ValidationResult(false, "Invalid icon resource ID")
            }
        }

        return ValidationResult(true)
    }

    /** Validates color hex format */
    private fun isValidColorHex(colorHex: String): Boolean {
        return colorHex.matches(Regex("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{8})$"))
    }

    /**
     * Reassigns cards from one category to another This is a placeholder - would need actual
     * implementation in CardRepository
     */
    private suspend fun reassignCardsToCategory(fromCategoryId: String, toCategoryId: String) {
        // Placeholder implementation
        // In a real app, this would:
        // 1. Get all cards in the fromCategoryId
        // 2. Update each card's categoryId to toCategoryId
        // 3. Save the updated cards
    }
}

/** Request for creating a new category */
data class CreateCategoryRequest(
        val categoryId: String = UUID.randomUUID().toString(),
        val name: String,
        val iconResId: Int,
        val colorHex: String
)

/** Request for updating an existing category All fields except categoryId are optional */
data class UpdateCategoryRequest(
        val categoryId: String,
        val name: String? = null,
        val iconResId: Int? = null,
        val colorHex: String? = null
)

/** Validation result for category requests */
data class ValidationResult(val isValid: Boolean, val errorMessage: String? = null)
