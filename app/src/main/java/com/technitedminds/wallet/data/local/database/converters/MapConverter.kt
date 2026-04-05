package com.technitedminds.wallet.data.local.database.converters

import androidx.room.TypeConverter
import com.technitedminds.wallet.data.local.security.TinkEncryptionManager
import kotlinx.serialization.json.Json

/**
 * Room type converter for Map<String, String> fields.
 * Encrypts JSON at rest using Tink AES-256-GCM when available,
 * and transparently decrypts legacy plaintext for backward compatibility.
 */
class MapConverter {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @TypeConverter
    fun fromStringMap(map: Map<String, String>?): String {
        val jsonString = try {
            if (map.isNullOrEmpty()) "{}" else json.encodeToString(map)
        } catch (e: Exception) {
            "{}"
        }
        return try {
            TinkEncryptionManager.getInstance()?.encrypt(jsonString) ?: jsonString
        } catch (e: Exception) {
            jsonString
        }
    }

    @TypeConverter
    fun toStringMap(mapString: String?): Map<String, String> {
        if (mapString.isNullOrBlank()) return emptyMap()
        return try {
            val decrypted = if (mapString.startsWith("ENC:")) {
                TinkEncryptionManager.getInstance()?.decrypt(mapString) ?: mapString
            } else {
                mapString
            }
            json.decodeFromString<Map<String, String>>(decrypted)
        } catch (e: Exception) {
            emptyMap()
        }
    }
}
