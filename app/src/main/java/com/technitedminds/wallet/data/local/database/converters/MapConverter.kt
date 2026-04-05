package com.technitedminds.wallet.data.local.database.converters

import androidx.room.TypeConverter
import com.technitedminds.wallet.data.local.security.TinkEncryptionManager
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

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
        val jsonString = if (map == null || map.isEmpty()) {
            "{}"
        } else {
            json.encodeToString(map)
        }
        return TinkEncryptionManager.getRequiredInstance().encrypt(jsonString)
    }

    @TypeConverter
    fun toStringMap(mapString: String?): Map<String, String> {
        if (mapString.isNullOrBlank()) return emptyMap()
        val decrypted = if (mapString.startsWith("ENC:")) {
            TinkEncryptionManager.getRequiredInstance().decrypt(mapString)
        } else {
            mapString
        }
        return json.decodeFromString<Map<String, String>>(decrypted)
    }
}
