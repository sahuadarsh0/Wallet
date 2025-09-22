package com.technitedminds.wallet.data.local.database.dao

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.technitedminds.wallet.data.local.database.entities.CardEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Card operations. Provides CRUD operations, search, and filtering
 * capabilities.
 */
@Dao
interface CardDao {

        /** Get all cards as a Flow for reactive updates */
        @Query("SELECT * FROM cards ORDER BY created_at DESC")
        fun getAllCards(): Flow<List<CardEntity>>

        /** Get a specific card by ID */
        @Query("SELECT * FROM cards WHERE id = :cardId")
        suspend fun getCardById(cardId: String): CardEntity?

        /** Get cards by category ID */
        @Query("SELECT * FROM cards WHERE category_id = :categoryId ORDER BY created_at DESC")
        fun getCardsByCategory(categoryId: String): Flow<List<CardEntity>>

        /** Get cards by type (requires custom handling for CardType sealed class) */
        @Query("SELECT * FROM cards WHERE type LIKE :typePattern ORDER BY created_at DESC")
        fun getCardsByTypePattern(typePattern: String): Flow<List<CardEntity>>

        /**
         * Search cards by name, extracted data, or custom fields Uses FTS (Full Text Search) like
         * functionality with LIKE operator
         */
        @Query(
                """
        SELECT * FROM cards 
        WHERE name LIKE '%' || :query || '%' 
        OR extracted_data LIKE '%' || :query || '%' 
        OR custom_fields LIKE '%' || :query || '%'
        ORDER BY 
            CASE WHEN name LIKE '%' || :query || '%' THEN 1 ELSE 2 END,
            created_at DESC
    """
        )
        fun searchCards(query: String): Flow<List<CardEntity>>

        /** Get cards with sorting options */
        @Query(
                """
        SELECT * FROM cards 
        ORDER BY 
            CASE WHEN :sortBy = 'name' AND :ascending = 1 THEN name END ASC,
            CASE WHEN :sortBy = 'name' AND :ascending = 0 THEN name END DESC,
            CASE WHEN :sortBy = 'created_at' AND :ascending = 1 THEN created_at END ASC,
            CASE WHEN :sortBy = 'created_at' AND :ascending = 0 THEN created_at END DESC,
            CASE WHEN :sortBy = 'updated_at' AND :ascending = 1 THEN updated_at END ASC,
            CASE WHEN :sortBy = 'updated_at' AND :ascending = 0 THEN updated_at END DESC,
            CASE WHEN :sortBy = 'type' AND :ascending = 1 THEN type END ASC,
            CASE WHEN :sortBy = 'type' AND :ascending = 0 THEN type END DESC
    """
        )
        fun getCardsSorted(sortBy: String, ascending: Boolean): Flow<List<CardEntity>>

        /** Get cards with filtering and sorting */
        @Query(
                """
        SELECT * FROM cards 
        WHERE (:categoryId IS NULL OR category_id = :categoryId)
        AND (:typePattern IS NULL OR type LIKE :typePattern)
        ORDER BY 
            CASE WHEN :sortBy = 'name' AND :ascending = 1 THEN name END ASC,
            CASE WHEN :sortBy = 'name' AND :ascending = 0 THEN name END DESC,
            CASE WHEN :sortBy = 'created_at' AND :ascending = 1 THEN created_at END ASC,
            CASE WHEN :sortBy = 'created_at' AND :ascending = 0 THEN created_at END DESC,
            CASE WHEN :sortBy = 'updated_at' AND :ascending = 1 THEN updated_at END ASC,
            CASE WHEN :sortBy = 'updated_at' AND :ascending = 0 THEN updated_at END DESC,
            CASE WHEN :sortBy = 'type' AND :ascending = 1 THEN type END ASC,
            CASE WHEN :sortBy = 'type' AND :ascending = 0 THEN type END DESC
    """
        )
        fun getCardsFiltered(
                categoryId: String?,
                typePattern: String?,
                sortBy: String,
                ascending: Boolean
        ): Flow<List<CardEntity>>

        /** Get total count of cards */
        @Query("SELECT COUNT(*) FROM cards") suspend fun getCardCount(): Int

        /** Get count of cards by category */
        @Query("SELECT COUNT(*) FROM cards WHERE category_id = :categoryId")
        suspend fun getCardCountByCategory(categoryId: String): Int

        /** Check if a card exists */
        @Query("SELECT EXISTS(SELECT 1 FROM cards WHERE id = :cardId)")
        suspend fun cardExists(cardId: String): Boolean

        /** Insert a new card */
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertCard(card: CardEntity): Long

        /** Insert multiple cards */
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertCards(cards: List<CardEntity>): List<Long>

        /** Update an existing card */
        @Update suspend fun updateCard(card: CardEntity)

        /** Update multiple cards */
        @Update suspend fun updateCards(cards: List<CardEntity>)

        /** Delete a card */
        @Delete suspend fun deleteCard(card: CardEntity)

        /** Delete a card by ID */
        @Query("DELETE FROM cards WHERE id = :cardId") suspend fun deleteCardById(cardId: String)

        /** Delete multiple cards by IDs */
        @Query("DELETE FROM cards WHERE id IN (:cardIds)")
        suspend fun deleteCardsByIds(cardIds: List<String>)

        /** Delete all cards in a category */
        @Query("DELETE FROM cards WHERE category_id = :categoryId")
        suspend fun deleteCardsByCategory(categoryId: String)

        /** Update category for cards (used when reassigning cards to a new category) */
        @Query(
                "UPDATE cards SET category_id = :newCategoryId, updated_at = :updatedAt WHERE category_id = :oldCategoryId"
        )
        suspend fun updateCardsCategory(
                oldCategoryId: String,
                newCategoryId: String,
                updatedAt: Long
        )

        /** Get cards with missing images (for cleanup operations) */
        @Query("SELECT * FROM cards WHERE front_image_path = '' OR back_image_path = ''")
        suspend fun getCardsWithMissingImages(): List<CardEntity>

        /** Get all image paths (for cleanup operations) */
        @Query("SELECT front_image_path, back_image_path FROM cards")
        suspend fun getAllImagePaths(): List<ImagePaths>

        /** Data class for image paths query result */
        data class ImagePaths(
                @ColumnInfo(name = "front_image_path") val frontImagePath: String,
                @ColumnInfo(name = "back_image_path") val backImagePath: String
        )
}
