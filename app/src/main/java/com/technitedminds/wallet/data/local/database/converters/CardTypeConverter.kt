package com.technitedminds.wallet.data.local.database.converters

import androidx.room.TypeConverter
import com.technitedminds.wallet.domain.model.CardType

/**
 * Room type converter for [CardType] sealed class.
 *
 * Converts [CardType] instances to/from string representations for database storage.
 * Supports all predefined card types (Credit, Debit, and image-only types) as well as
 * custom card types with user-defined names and colors.
 *
 * **Format:**
 * - Predefined types: Stored as simple strings (e.g., "Credit", "Debit", "GiftCard")
 * - Custom types: Stored as "Custom:{typeName}:{colorHex}" (e.g., "Custom:MyCard:#FF5722")
 *
 * **Card Type Categories:**
 * - **OCR-enabled**: Credit, Debit (support text extraction)
 * - **Image-only**: TransportCard, GiftCard, LoyaltyCard, MembershipCard, InsuranceCard,
 *   IdentificationCard, Voucher, Event, BusinessCard, LibraryCard, HotelCard, StudentCard, AccessCard
 * - **User-defined**: Custom(name, color)
 */
class CardTypeConverter {

    /**
     * Converts a [CardType] instance to its string representation for database storage.
     *
     * @param cardType The card type to convert
     * @return String representation of the card type
     */
    @TypeConverter
    fun fromCardType(cardType: CardType): String {
        return when (cardType) {
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
    }

    /**
     * Converts a string representation back to a [CardType] instance.
     *
     * Handles all predefined card types and custom types in the format "Custom:{name}:{color}".
     * If the string doesn't match any known format, returns a default Custom card type.
     *
     * @param cardTypeString The string representation from the database
     * @return The corresponding [CardType] instance
     */
    @TypeConverter
    fun toCardType(cardTypeString: String): CardType {
        return when {
            cardTypeString == "Credit" -> CardType.Credit
            cardTypeString == "Debit" -> CardType.Debit
            cardTypeString == "TransportCard" -> CardType.TransportCard
            cardTypeString == "GiftCard" -> CardType.GiftCard
            cardTypeString == "LoyaltyCard" -> CardType.LoyaltyCard
            cardTypeString == "MembershipCard" -> CardType.MembershipCard
            cardTypeString == "InsuranceCard" -> CardType.InsuranceCard
            cardTypeString == "IdentificationCard" -> CardType.IdentificationCard
            cardTypeString == "Voucher" -> CardType.Voucher
            cardTypeString == "Event" -> CardType.Event
            cardTypeString == "BusinessCard" -> CardType.BusinessCard
            cardTypeString == "LibraryCard" -> CardType.LibraryCard
            cardTypeString == "HotelCard" -> CardType.HotelCard
            cardTypeString == "StudentCard" -> CardType.StudentCard
            cardTypeString == "AccessCard" -> CardType.AccessCard
            cardTypeString.startsWith("Custom:") -> {
                val parts = cardTypeString.substringAfter("Custom:").split(":")
                if (parts.size >= 2) {
                    CardType.Custom(parts[0], parts[1])
                } else {
                    CardType.Custom(parts.getOrElse(0) { "Custom Card" }, "#757575")
                }
            }
            else -> CardType.Custom("Custom Card", "#757575") // Fallback for unknown types
        }
    }
}
