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
            is CardType.ImageOnly -> "ImageOnly:${cardType.typeName}"
        }
    }

    @TypeConverter
    fun toCardType(cardTypeString: String): CardType {
        return when {
            cardTypeString == "Credit" -> CardType.Credit
            cardTypeString == "Debit" -> CardType.Debit
            cardTypeString == "ATM" -> CardType.ATM
            cardTypeString.startsWith("ImageOnly:") -> {
                val typeName = cardTypeString.substringAfter("ImageOnly:")
                CardType.ImageOnly(typeName)
            }
            else -> CardType.ImageOnly("Unknown") // Fallback for unknown types
        }
    }
}
