package com.technitedminds.wallet.domain.usecase.category

import com.technitedminds.wallet.domain.repository.CategoryRepository
import javax.inject.Inject

/**
 * Use case for getting category name by ID with fallback handling.
 */
class GetCategoryNameUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    
    suspend operator fun invoke(categoryId: String?): String {
        if (categoryId.isNullOrBlank()) {
            return "General"
        }
        
        return categoryRepository.getCategoryName(categoryId) ?: "Unknown Category"
    }
}