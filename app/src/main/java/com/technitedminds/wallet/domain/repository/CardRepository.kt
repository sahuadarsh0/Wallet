package com.technitedminds.wallet.domain.repository

import com.technitedminds.wallet.domain.model.Card
import com.technitedminds.wallet.domain.model.CardType
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for card data operations. Provides CRUD operations and search functionality
 * for cards.
 */
interface CardRepository {

    /**
     * Retrieves all cards as a Flow for reactive updates
     * @return Flow of list of all cards
     */
    suspend fun getAllCards(): Flow<List<Card>>

    /**
     * Retrieves a specific card by its ID
     * @param id The unique identifier of the card
     * @return The card if found, null otherwise
     */
    suspend fun getCardById(id: String): Card?

    /**
     * Retrieves all cards belonging to a specific category
     * @param categoryId The ID of the category
     * @return Flow of list of cards in the specified category
     */
    suspend fun getCardsByCategory(categoryId: String): Flow<List<Card>>

    /**
     * Retrieves cards by their type
     * @param cardType The type of cards to retrieve
     * @return Flow of list of cards of the specified type
     */
    suspend fun getCardsByType(cardType: CardType): Flow<List<Card>>

    /**
     * Inserts a new card into the repository
     * @param card The card to insert
     * @return The ID of the inserted card
     */
    suspend fun insertCard(card: Card): String

    /**
     * Updates an existing card
     * @param card The card with updated information
     */
    suspend fun updateCard(card: Card)

    /**
     * Deletes a card by its ID
     * @param id The ID of the card to delete
     */
    suspend fun deleteCard(id: String)

    /**
     * Deletes multiple cards by their IDs
     * @param ids List of card IDs to delete
     */
    suspend fun deleteCards(ids: List<String>)

    /**
     * Searches for cards based on a query string Searches in card name, extracted data, and custom
     * fields
     * @param query The search query
     * @return Flow of list of cards matching the search query
     */
    suspend fun searchCards(query: String): Flow<List<Card>>

    /**
     * Retrieves cards with sorting options
     * @param sortBy The field to sort by (name, createdAt, updatedAt)
     * @param ascending Whether to sort in ascending order
     * @return Flow of list of sorted cards
     */
    suspend fun getCardsSorted(sortBy: CardSortBy, ascending: Boolean = true): Flow<List<Card>>

    /**
     * Retrieves cards with filtering and sorting
     * @param categoryId Optional category filter
     * @param cardType Optional card type filter
     * @param sortBy Sort field
     * @param ascending Sort direction
     * @return Flow of list of filtered and sorted cards
     */
    suspend fun getCardsFiltered(
            categoryId: String? = null,
            cardType: CardType? = null,
            sortBy: CardSortBy = CardSortBy.CREATED_AT,
            ascending: Boolean = false
    ): Flow<List<Card>>

    /**
     * Gets the total count of cards
     * @return Total number of cards
     */
    suspend fun getCardCount(): Int

    /**
     * Gets the count of cards by category
     * @param categoryId The category ID
     * @return Number of cards in the category
     */
    suspend fun getCardCountByCategory(categoryId: String): Int

    /**
     * Checks if a card with the given ID exists
     * @param id The card ID to check
     * @return True if the card exists, false otherwise
     */
    suspend fun cardExists(id: String): Boolean

    /**
     * Updates the category for all cards in a specific category
     * Used when reassigning cards to a new category (e.g., when deleting a category)
     * @param oldCategoryId The current category ID of the cards
     * @param newCategoryId The new category ID to assign to the cards
     */
    suspend fun updateCardsCategory(oldCategoryId: String, newCategoryId: String)
}

/** Enum for card sorting options */
enum class CardSortBy {
    NAME,
    CREATED_AT,
    UPDATED_AT,
    CARD_TYPE
}
