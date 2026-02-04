package com.technitedminds.wallet.data.local.database.converters

import androidx.room.TypeConverter
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

/**
 * Room type converter for Map<String, String> fields. Converts maps to/from JSON string for
 * database storage.
 */
class MapConverter {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @TypeConverter
    fun fromStringMap(map: Map<String, String>?): String {
        return try {
            if (map == null || map.isEmpty()) {
                "{}"
            } else {
                json.encodeToString(map)
            }
        } catch (e: Exception) {
            "{}"
        }
    }

    @TypeConverter
    fun toStringMap(mapString: String?): Map<String, String> {
        return try {
            if (mapString.isNullOrBlank()) {
                emptyMap()
            } else {
                json.decodeFromString<Map<String, String>>(mapString)
            }
        } catch (e: Exception) {
            // Return empty map if deserialization fails
            emptyMap()
        }
    }
}
