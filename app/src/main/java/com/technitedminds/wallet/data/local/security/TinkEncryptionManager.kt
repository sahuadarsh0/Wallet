package com.technitedminds.wallet.data.local.security

import android.content.Context
import com.google.crypto.tink.Aead
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.aead.AesGcmKeyManager
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages AES-256-GCM encryption via Google Tink backed by Android Keystore.
 * Used to encrypt sensitive card data (PAN, CVV, extracted fields) at rest.
 *
 * The keyset is stored encrypted under a master key held in the Android Keystore,
 * making it hardware-backed on supported devices.
 */
@Singleton
class TinkEncryptionManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        private const val KEYSET_NAME = "cardvault_keyset"
        private const val PREFERENCE_FILE = "cardvault_keyset_prefs"
        private const val MASTER_KEY_URI = "android-keystore://cardvault_master_key"

        @Volatile
        private var INSTANCE: TinkEncryptionManager? = null

        fun getInstance(): TinkEncryptionManager? = INSTANCE

        fun getRequiredInstance(): TinkEncryptionManager {
            return requireNotNull(INSTANCE) {
                "TinkEncryptionManager is unavailable. Encryption must be initialized before database access."
            }
        }
    }

    private val aead: Aead

    init {
        AeadConfig.register()

        val keysetHandle = AndroidKeysetManager.Builder()
            .withSharedPref(context, KEYSET_NAME, PREFERENCE_FILE)
            .withKeyTemplate(AesGcmKeyManager.aes256GcmTemplate())
            .withMasterKeyUri(MASTER_KEY_URI)
            .build()
            .keysetHandle

        aead = keysetHandle.getPrimitive(Aead::class.java)
        INSTANCE = this
    }

    /**
     * Encrypt plaintext string. Returns a Base64-encoded ciphertext prefixed with "ENC:"
     * so the converter can distinguish encrypted from legacy plaintext data.
     */
    fun encrypt(plaintext: String): String {
        if (plaintext.isBlank()) return plaintext
        val cipherBytes = aead.encrypt(plaintext.toByteArray(Charsets.UTF_8), null)
        return "ENC:" + android.util.Base64.encodeToString(cipherBytes, android.util.Base64.NO_WRAP)
    }

    /**
     * Decrypt a ciphertext string previously produced by [encrypt].
     * If the input does not carry the "ENC:" prefix it is returned as-is
     * (backward-compatible with pre-encryption data).
     */
    fun decrypt(ciphertext: String): String {
        if (!ciphertext.startsWith("ENC:")) return ciphertext
        val raw = ciphertext.removePrefix("ENC:")
        val cipherBytes = android.util.Base64.decode(raw, android.util.Base64.NO_WRAP)
        return String(aead.decrypt(cipherBytes, null), Charsets.UTF_8)
    }
}
