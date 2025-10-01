package com.technitedminds.wallet.domain.usecase.category

import com.technitedminds.wallet.domain.model.Category
import com.technitedminds.wallet.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for retrieving all categories with proper sorting and default category handling.
 */
class GetCategoriesUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    
    operator fun invoke(): Flow<List<Category>> {
        return categoryRepository.getAllCategories()
    }
    
    suspend fun getCategoryById(id: String): Category? {
        return categoryRepository.getCategoryById(id)
    }
    
    suspend fun categoryExists(id: String): Boolean {
        return categoryRepository.categoryExists(id)
    }
}