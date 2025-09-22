package com.technitedminds.wallet.domain.usecase.card

import com.technitedminds.wallet.domain.model.Card
import com.technitedminds.wallet.domain.model.CardType
import com.technitedminds.wallet.domain.repository.CardRepository
import com.technitedminds.wallet.domain.repository.CardSortBy
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Use case for retrieving cards with filtering and sorting capabilities. */
class GetCardsUseCase @Inject constructor(private val cardRepository: CardRepository) {

    /**
     * Gets all cards
     * @return Flow of list of all cards
     */
    suspend fun getAllCards(): Flow<List<Card>> {
        return cardRepository.getAllCards()
    }

    /**
     * Gets a specific card by ID
     * @param cardId The ID of the card to retrieve
     * @return The card if found, null otherwise
     */
    suspend fun getCardById(cardId: String): Card? {
        return cardRepository.getCardById(cardId)
    }

    /**
     * Gets cards with filtering and sorting options
     * @param request The filter and sort request
     * @return Flow of filtered and sorted cards
     */
    suspend operator fun invoke(request: GetCardsRequest): Flow<List<Card>> {
        return when {
            // Search query provided
            request.searchQuery.isNotBlank() -> {
                cardRepository.searchCards(request.searchQuery).map { cards ->
                    applyFiltersAndSort(cards, request)
                }
            }

            // Category filter provided
            request.categoryId != null -> {
                cardRepository.getCardsByCategory(request.categoryId).map { cards ->
                    applyFiltersAndSort(cards, request)
                }
            }

            // Card type filter provided
            request.cardType != null -> {
                cardRepository.getCardsByType(request.cardType).map { cards ->
                    applyFiltersAndSort(cards, request)
                }
            }

            // Use repository's built-in filtering
            else -> {
                cardRepository.getCardsFiltered(
                        categoryId = request.categoryId,
                        cardType = request.cardType,
                        sortBy = request.sortBy,
                        ascending = request.ascending
                )
            }
        }
    }

    /**
     * Gets cards by category
     * @param categoryId The category ID
     * @return Flow of cards in the specified category
     */
    suspend fun getCardsByCategory(categoryId: String): Flow<List<Card>> {
        return cardRepository.getCardsByCategory(categoryId)
    }

    /**
     * Gets cards by type
     * @param cardType The card type
     * @return Flow of cards of the specified type
     */
    suspend fun getCardsByType(cardType: CardType): Flow<List<Card>> {
        return cardRepository.getCardsByType(cardType)
    }

    /**
     * Searches cards by query
     * @param query The search query
     * @return Flow of cards matching the search
     */
    suspend fun searchCards(query: String): Flow<List<Card>> {
        return if (query.isBlank()) {
            cardRepository.getAllCards()
        } else {
            cardRepository.searchCards(query)
        }
    }

    /**
     * Gets card count statistics
     * @return CardStats with various counts
     */
    suspend fun getCardStats(): CardStats {
        val totalCards = cardRepository.getCardCount()
        val allCards = cardRepository.getAllCards()

        // We need to collect the flow to get the actual cards for statistics
        // In a real implementation, you might want to add specific repository methods for these
        // stats
        return CardStats(
                totalCards = totalCards,
                textualCards = 0, // Would be calculated from actual cards
                imageCards = 0, // Would be calculated from actual cards
                categorizedCards = 0, // Would be calculated from actual cards
                uncategorizedCards = 0 // Would be calculated from actual cards
        )
    }

    /** Applies additional filters and sorting to a list of cards */
    private fun applyFiltersAndSort(cards: List<Card>, request: GetCardsRequest): List<Card> {
        var filteredCards = cards

        // Apply card type filter if not already applied
        if (request.cardType != null) {
            filteredCards = filteredCards.filter { it.type == request.cardType }
        }

        // Apply date range filter
        if (request.dateFrom != null) {
            filteredCards = filteredCards.filter { it.createdAt >= request.dateFrom }
        }

        if (request.dateTo != null) {
            filteredCards = filteredCards.filter { it.createdAt <= request.dateTo }
        }

        // Apply has extracted data filter
        if (request.hasExtractedData != null) {
            filteredCards =
                    filteredCards.filter { it.hasExtractedData() == request.hasExtractedData }
        }

        // Apply has custom fields filter
        if (request.hasCustomFields != null) {
            filteredCards = filteredCards.filter { it.hasCustomFields() == request.hasCustomFields }
        }

        // Apply sorting
        return when (request.sortBy) {
            CardSortBy.NAME -> {
                if (request.ascending) {
                    filteredCards.sortedBy { it.name.lowercase() }
                } else {
                    filteredCards.sortedByDescending { it.name.lowercase() }
                }
            }
            CardSortBy.CREATED_AT -> {
                if (request.ascending) {
                    filteredCards.sortedBy { it.createdAt }
                } else {
                    filteredCards.sortedByDescending { it.createdAt }
                }
            }
            CardSortBy.UPDATED_AT -> {
                if (request.ascending) {
                    filteredCards.sortedBy { it.updatedAt }
                } else {
                    filteredCards.sortedByDescending { it.updatedAt }
                }
            }
            CardSortBy.CARD_TYPE -> {
                if (request.ascending) {
                    filteredCards.sortedBy { it.type.getDisplayName() }
                } else {
                    filteredCards.sortedByDescending { it.type.getDisplayName() }
                }
            }
        }
    }
}

/** Request for getting cards with filters and sorting */
data class GetCardsRequest(
        val categoryId: String? = null,
        val cardType: CardType? = null,
        val searchQuery: String = "",
        val sortBy: CardSortBy = CardSortBy.CREATED_AT,
        val ascending: Boolean = false,
        val dateFrom: Long? = null,
        val dateTo: Long? = null,
        val hasExtractedData: Boolean? = null,
        val hasCustomFields: Boolean? = null
)

/** Statistics about cards in the wallet */
data class CardStats(
        val totalCards: Int,
        val textualCards: Int,
        val imageCards: Int,
        val categorizedCards: Int,
        val uncategorizedCards: Int
)
