package com.technitedminds.wallet.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.technitedminds.wallet.data.local.database.entities.CategoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Category operations. Provides CRUD operations and category management
 * functionality.
 */
@Dao
interface CategoryDao {

    /** Get all categories as a Flow for reactive updates */
    @Query("SELECT * FROM categories ORDER BY sort_order ASC, name ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    /** Get a specific category by ID */
    @Query("SELECT * FROM categories WHERE id = :categoryId")
    suspend fun getCategoryById(categoryId: String): CategoryEntity?

    /** Check if a category exists */
    @Query("SELECT EXISTS(SELECT 1 FROM categories WHERE id = :categoryId)")
    suspend fun categoryExists(categoryId: String): Boolean

    /** Insert a new category */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity): Long

    /** Insert multiple categories (used for default category initialization) */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>): List<Long>

    /** Update an existing category */
    @Update 
    suspend fun updateCategory(category: CategoryEntity)

    /** Delete a category by ID (only if it's not the default category) */
    @Query("DELETE FROM categories WHERE id = :categoryId AND id != 'default'")
    suspend fun deleteCategoryById(categoryId: String): Int
}
