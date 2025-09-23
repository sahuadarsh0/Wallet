package com.technitedminds.wallet.data.local.database.converters

import androidx.room.TypeConverter
import com.technitedminds.wallet.domain.model.CardType

/**
 * Room type converter for CardType sealed class. Converts CardType to/from string for database
 * storage.
 */
class CardTypeConverter {

    @TypeConverter
    fun fromCardType(cardType: CardType): String {
        return when (cardType) {
            is CardType.Credit -> "Credit"
            is CardType.Debit -> "Debit"
            is CardType.ATM -> "ATM"
            is CardType.GiftCard -> "GiftCard"
            is CardType.LoyaltyCard -> "LoyaltyCard"
            is CardType.MembershipCard -> "MembershipCard"
            is CardType.InsuranceCard -> "InsuranceCard"
            is CardType.IdentificationCard -> "IdentificationCard"
            is CardType.TransportCard -> "TransportCard"
            is CardType.BusinessCard -> "BusinessCard"
            is CardType.LibraryCard -> "LibraryCard"
            is CardType.HotelCard -> "HotelCard"
            is CardType.StudentCard -> "StudentCard"
            is CardType.AccessCard -> "AccessCard"
            is CardType.Custom -> "Custom:${cardType.typeName}:${cardType.colorHex}"
        }
    }

    @TypeConverter
    fun toCardType(cardTypeString: String): CardType {
        return when {
            cardTypeString == "Credit" -> CardType.Credit
            cardTypeString == "Debit" -> CardType.Debit
            cardTypeString == "ATM" -> CardType.ATM
            cardTypeString == "GiftCard" -> CardType.GiftCard
            cardTypeString == "LoyaltyCard" -> CardType.LoyaltyCard
            cardTypeString == "MembershipCard" -> CardType.MembershipCard
            cardTypeString == "InsuranceCard" -> CardType.InsuranceCard
            cardTypeString == "IdentificationCard" -> CardType.IdentificationCard
            cardTypeString == "TransportCard" -> CardType.TransportCard
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
                    CardType.Custom(parts.getOrElse(0) { "Custom" }, "#757575")
                }
            }
            // Legacy support for old ImageOnly format
            cardTypeString.startsWith("ImageOnly:") -> {
                val typeName = cardTypeString.substringAfter("ImageOnly:")
                CardType.Custom(typeName, "#757575")
            }
            else -> CardType.Custom("Unknown", "#757575") // Fallback for unknown types
        }
    }
}
