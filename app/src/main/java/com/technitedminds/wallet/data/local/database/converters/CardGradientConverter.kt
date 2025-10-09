package com.technitedminds.wallet.data.local.database.converters

import androidx.room.TypeConverter
import com.technitedminds.wallet.domain.model.CardGradient
import com.technitedminds.wallet.domain.model.GradientDirection
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable

/**
 * Room type converter for CardGradient objects. Converts between CardGradient and JSON string
 * for database storage.
 */
class CardGradientConverter {

    @TypeConverter
    fun fromCardGradient(gradient: CardGradient?): String? {
        return gradient?.let { 
            Json.encodeToString(
                CardGradientData(
                    startColor = it.startColor,
                    endColor = it.endColor,
                    direction = it.direction.name
                )
            )
        }
    }

    @TypeConverter
    fun toCardGradient(gradientJson: String?): CardGradient? {
        return gradientJson?.let {
            try {
                val data = Json.decodeFromString<CardGradientData>(it)
                CardGradient(
                    startColor = data.startColor,
                    endColor = data.endColor,
                    direction = GradientDirection.valueOf(data.direction)
                )
            } catch (e: Exception) {
                null // Return null if deserialization fails
            }
        }
    }
}

/**
 * Serializable data class for CardGradient JSON storage
 */
@Serializable
private data class CardGradientData(
    val startColor: String,
    val endColor: String,
    val direction: String
)