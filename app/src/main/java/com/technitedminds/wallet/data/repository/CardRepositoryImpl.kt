package com.technitedminds.wallet.data.repository

import com.technitedminds.wallet.data.local.database.dao.CardDao
import com.technitedminds.wallet.data.mapper.toDomainModel
import com.technitedminds.wallet.data.mapper.toDomainModels
import com.technitedminds.wallet.data.mapper.toEntity
import com.technitedminds.wallet.domain.model.Card
import com.technitedminds.wallet.domain.model.CardType
import com.technitedminds.wallet.domain.repository.CardRepository
import com.technitedminds.wallet.domain.repository.CardSortBy
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Implementation of CardRepository using Room database. Handles data persistence and provides
 * reactive data access.
 */
@Singleton
class CardRepositoryImpl @Inject constructor(private val cardDao: CardDao) : CardRepository {

    override suspend fun getAllCards(): Flow<List<Card>> {
        return cardDao.getAllCards().map { entities -> entities.toDomainModels() }
    }

    override suspend fun getCardById(id: String): Card? {
        return try {
            cardDao.getCardById(id)?.toDomainModel()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getCardsByCategory(categoryId: String): Flow<List<Card>> {
        return cardDao.getCardsByCategory(categoryId).map { entities -> entities.toDomainModels() }
    }

    override suspend fun getCardsByType(cardType: CardType): Flow<List<Card>> {
        val typePattern =
                when (cardType) {
                    is CardType.Credit -> "Credit"
                    is CardType.Debit -> "Debit"
                    is CardType.GiftCard -> "GiftCard"
                    is CardType.LoyaltyCard -> "LoyaltyCard"
                    is CardType.MembershipCard -> "MembershipCard"
                    is CardType.InsuranceCard -> "InsuranceCard"
                    is CardType.IdentificationCard -> "IdentificationCard"
                    is CardType.Voucher -> "Voucher"
                    is CardType.Event -> "Event"
                    is CardType.TransportCard -> "TransportCard"
                    is CardType.BusinessCard -> "BusinessCard"
                    is CardType.LibraryCard -> "LibraryCard"
                    is CardType.HotelCard -> "HotelCard"
                    is CardType.StudentCard -> "StudentCard"
                    is CardType.AccessCard -> "AccessCard"
                    is CardType.Custom -> "Custom:${cardType.typeName}:${cardType.colorHex}"
                }

        return cardDao.getCardsByTypePattern(typePattern).map { entities ->
            entities.toDomainModels()
        }
    }

    override suspend fun insertCard(card: Card): String {
        return try {
            cardDao.insertCard(card.toEntity())
            card.id
        } catch (e: Exception) {
            throw Exception("Failed to insert card: ${e.message}", e)
        }
    }

    override suspend fun updateCard(card: Card) {
        try {
            cardDao.updateCard(card.toEntity())
        } catch (e: Exception) {
            throw Exception("Failed to update card: ${e.message}", e)
        }
    }

    override suspend fun deleteCard(id: String) {
        try {
            cardDao.deleteCardById(id)
        } catch (e: Exception) {
            throw Exception("Failed to delete card: ${e.message}", e)
        }
    }

    override suspend fun deleteCards(ids: List<String>) {
        try {
            cardDao.deleteCardsByIds(ids)
        } catch (e: Exception) {
            throw Exception("Failed to delete cards: ${e.message}", e)
        }
    }

    override suspend fun searchCards(query: String): Flow<List<Card>> {
        return cardDao.searchCards(query).map { entities -> entities.toDomainModels() }
    }

    override suspend fun getCardsSorted(sortBy: CardSortBy, ascending: Boolean): Flow<List<Card>> {
        val sortByString =
                when (sortBy) {
                    CardSortBy.NAME -> "name"
                    CardSortBy.CREATED_AT -> "created_at"
                    CardSortBy.UPDATED_AT -> "updated_at"
                    CardSortBy.CARD_TYPE -> "type"
                }

        return cardDao.getCardsSorted(sortByString, ascending).map { entities ->
            entities.toDomainModels()
        }
    }

    override suspend fun getCardsFiltered(
            categoryId: String?,
            cardType: CardType?,
            sortBy: CardSortBy,
            ascending: Boolean
    ): Flow<List<Card>> {
        val typePattern =
                cardType?.let { type ->
                    when (type) {
                        is CardType.Credit -> "Credit"
                        is CardType.Debit -> "Debit"
                        is CardType.GiftCard -> "GiftCard"
                        is CardType.LoyaltyCard -> "LoyaltyCard"
                        is CardType.MembershipCard -> "MembershipCard"
                        is CardType.InsuranceCard -> "InsuranceCard"
                        is CardType.IdentificationCard -> "IdentificationCard"
                        is CardType.Voucher -> "Voucher"
                        is CardType.Event -> "Event"
                        is CardType.TransportCard -> "TransportCard"
                        is CardType.BusinessCard -> "BusinessCard"
                        is CardType.LibraryCard -> "LibraryCard"
                        is CardType.HotelCard -> "HotelCard"
                        is CardType.StudentCard -> "StudentCard"
                        is CardType.AccessCard -> "AccessCard"
                        is CardType.Custom -> "Custom:${type.typeName}:${type.colorHex}"
                    }
                }

        val sortByString =
                when (sortBy) {
                    CardSortBy.NAME -> "name"
                    CardSortBy.CREATED_AT -> "created_at"
                    CardSortBy.UPDATED_AT -> "updated_at"
                    CardSortBy.CARD_TYPE -> "type"
                }

        return cardDao.getCardsFiltered(categoryId, typePattern, sortByString, ascending).map {
                entities ->
            entities.toDomainModels()
        }
    }

    override suspend fun getCardCount(): Int {
        return try {
            cardDao.getCardCount()
        } catch (e: Exception) {
            0
        }
    }

    override suspend fun getCardCountByCategory(categoryId: String): Int {
        return try {
            cardDao.getCardCountByCategory(categoryId)
        } catch (e: Exception) {
            0
        }
    }

    override suspend fun cardExists(id: String): Boolean {
        return try {
            cardDao.cardExists(id)
        } catch (e: Exception) {
            false
        }
    }

    /** Updates category for cards (used when reassigning cards to a new category) */
    suspend fun updateCardsCategory(oldCategoryId: String, newCategoryId: String) {
        try {
            cardDao.updateCardsCategory(oldCategoryId, newCategoryId, System.currentTimeMillis())
        } catch (e: Exception) {
            throw Exception("Failed to update cards category: ${e.message}", e)
        }
    }

    /** Gets cards with missing images (for cleanup operations) */
    suspend fun getCardsWithMissingImages(): List<Card> {
        return try {
            cardDao.getCardsWithMissingImages().toDomainModels()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /** Gets all image paths (for cleanup operations) */
    suspend fun getAllImagePaths(): List<String> {
        return try {
            val imagePaths = cardDao.getAllImagePaths()
            val allPaths = mutableListOf<String>()

            imagePaths.forEach { paths ->
                if (paths.frontImagePath.isNotBlank()) {
                    allPaths.add(paths.frontImagePath)
                }
                if (paths.backImagePath.isNotBlank()) {
                    allPaths.add(paths.backImagePath)
                }
            }

            allPaths
        } catch (e: Exception) {
            emptyList()
        }
    }

    /** Inserts multiple cards (used for import operations) */
    suspend fun insertCards(cards: List<Card>): List<String> {
        return try {
            cardDao.insertCards(cards.map { it.toEntity() })
            cards.map { it.id }
        } catch (e: Exception) {
            throw Exception("Failed to insert cards: ${e.message}", e)
        }
    }

    /** Updates multiple cards (used for batch operations) */
    suspend fun updateCards(cards: List<Card>) {
        try {
            cardDao.updateCards(cards.map { it.toEntity() })
        } catch (e: Exception) {
            throw Exception("Failed to update cards: ${e.message}", e)
        }
    }
}
