package com.technitedminds.wallet.data.repository

import com.technitedminds.wallet.data.local.database.dao.CategoryDao
import com.technitedminds.wallet.data.mapper.toDomainModel
import com.technitedminds.wallet.data.mapper.toDomainModels
import com.technitedminds.wallet.data.mapper.toEntity
import com.technitedminds.wallet.domain.model.Category
import com.technitedminds.wallet.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of CategoryRepository using Room database. Handles category persistence
 * and provides reactive data access.
 */
@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao
) : CategoryRepository {
    
    override fun getAllCategories(): Flow<List<Category>> {
        return categoryDao.getAllCategories().map { entities -> entities.toDomainModels() }
    }
    
    override suspend fun getCategoryById(id: String): Category? {
        return try {
            categoryDao.getCategoryById(id)?.toDomainModel()
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun getCategoryName(id: String): String? {
        return try {
            categoryDao.getCategoryById(id)?.name
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun addCategory(category: Category) {
        try {
            categoryDao.insertCategory(category.toEntity())
        } catch (e: Exception) {
            throw Exception("Failed to add category: ${e.message}", e)
        }
    }
    
    override suspend fun updateCategory(category: Category) {
        try {
            categoryDao.updateCategory(category.toEntity())
        } catch (e: Exception) {
            throw Exception("Failed to update category: ${e.message}", e)
        }
    }
    
    override suspend fun deleteCategory(id: String) {
        try {
            categoryDao.deleteCategoryById(id)
        } catch (e: Exception) {
            throw Exception("Failed to delete category: ${e.message}", e)
        }
    }
    
    override suspend fun categoryExists(id: String): Boolean {
        return try {
            categoryDao.categoryExists(id)
        } catch (e: Exception) {
            false
        }
    }
}