package com.technitedminds.wallet.data.local.database.converters

import androidx.room.TypeConverter
import org.json.JSONObject

/**
 * Room type converter for Map<String, String> fields. Converts maps to/from JSON string for
 * database storage.
 */
class MapConverter {

    @TypeConverter
    fun fromStringMap(map: Map<String, String>): String {
        return try {
            val jsonObject = JSONObject()
            for ((key, value) in map) {
                jsonObject.put(key, value)
            }
            jsonObject.toString()
        } catch (e: Exception) {
            "{}"
        }
    }

    @TypeConverter
    fun toStringMap(mapString: String): Map<String, String> {
        return try {
            val jsonObject = JSONObject(mapString)
            val map = mutableMapOf<String, String>()
            val keys = jsonObject.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                map[key] = jsonObject.getString(key)
            }
            map
        } catch (e: Exception) {
            // Return empty map if deserialization fails
            emptyMap()
        }
    }
}
