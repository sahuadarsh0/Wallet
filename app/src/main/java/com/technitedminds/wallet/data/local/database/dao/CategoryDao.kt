package com.technitedminds.wallet.data.local.database.dao

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
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
    @Query("SELECT * FROM categories ORDER BY is_default DESC, name ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    /** Get a specific category by ID */
    @Query("SELECT * FROM categories WHERE id = :categoryId")
    suspend fun getCategoryById(categoryId: String): CategoryEntity?

    /** Get default system categories */
    @Query("SELECT * FROM categories WHERE is_default = 1 ORDER BY name ASC")
    fun getDefaultCategories(): Flow<List<CategoryEntity>>

    /** Get user-created categories (non-default) */
    @Query("SELECT * FROM categories WHERE is_default = 0 ORDER BY name ASC")
    fun getUserCategories(): Flow<List<CategoryEntity>>

    /** Get categories sorted by name */
    @Query(
            """
        SELECT * FROM categories 
        ORDER BY 
            is_default DESC,
            CASE WHEN :ascending = 1 THEN name END ASC,
            CASE WHEN :ascending = 0 THEN name END DESC
    """
    )
    fun getCategoriesSorted(ascending: Boolean): Flow<List<CategoryEntity>>

    /** Check if a category name already exists */
    @Query(
            "SELECT EXISTS(SELECT 1 FROM categories WHERE name = :name AND (:excludeId IS NULL OR id != :excludeId))"
    )
    suspend fun categoryNameExists(name: String, excludeId: String?): Boolean

    /** Get total count of categories */
    @Query("SELECT COUNT(*) FROM categories") suspend fun getCategoryCount(): Int

    /** Get count of user-created categories */
    @Query("SELECT COUNT(*) FROM categories WHERE is_default = 0")
    suspend fun getUserCategoryCount(): Int

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
    @Update suspend fun updateCategory(category: CategoryEntity)

    /** Delete a category */
    @Delete suspend fun deleteCategory(category: CategoryEntity)

    /** Delete a category by ID (only if it's not a default category) */
    @Query("DELETE FROM categories WHERE id = :categoryId AND is_default = 0")
    suspend fun deleteCategoryById(categoryId: String): Int

    /** Delete all user-created categories */
    @Query("DELETE FROM categories WHERE is_default = 0") suspend fun deleteAllUserCategories()

    /** Get categories with card counts (using subquery) */
    @Query(
            """
        SELECT c.*, 
               (SELECT COUNT(*) FROM cards WHERE category_id = c.id) as card_count
        FROM categories c 
        ORDER BY c.is_default DESC, c.name ASC
    """
    )
    fun getCategoriesWithCardCounts(): Flow<List<CategoryWithCardCount>>

    /** Get empty categories (categories with no cards) */
    @Query(
            """
        SELECT * FROM categories c 
        WHERE NOT EXISTS (SELECT 1 FROM cards WHERE category_id = c.id)
        ORDER BY is_default DESC, name ASC
    """
    )
    fun getEmptyCategories(): Flow<List<CategoryEntity>>

    /** Check if default categories are initialized */
    @Query("SELECT EXISTS(SELECT 1 FROM categories WHERE is_default = 1)")
    suspend fun hasDefaultCategories(): Boolean

    /** Get missing default categories (for restoration) */
    @Query("SELECT COUNT(*) FROM categories WHERE is_default = 1")
    suspend fun getDefaultCategoryCount(): Int

    /** Data class for category with card count query result */
    data class CategoryWithCardCount(
            val id: String,
            val name: String,
            @ColumnInfo(name = "icon_res_id") val iconResId: Int,
            @ColumnInfo(name = "color_hex") val colorHex: String,
            @ColumnInfo(name = "is_default") val isDefault: Boolean,
            @ColumnInfo(name = "created_at") val createdAt: Long,
            @ColumnInfo(name = "updated_at") val updatedAt: Long,
            @ColumnInfo(name = "card_count") val cardCount: Int
    )
}
