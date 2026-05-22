package com.technitedminds.wallet.data.local.security

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utility for securely hashing and verifying 4-digit PINs using PBKDF2WithHmacSHA256.
 * Generates random salts and never stores plaintext PINs.
 */
@Singleton
class PinHasher @Inject constructor() {

    companion object {
        private const val ALGORITHM = "PBKDF2WithHmacSHA256"
        private const val ITERATIONS = 10_000
        private const val KEY_LENGTH = 256
        private const val SALT_LENGTH = 16
    }

    /**
     * Generate a cryptographically secure random salt.
     * @return Base64-encoded salt string
     */
    fun generateSalt(): String {
        val salt = ByteArray(SALT_LENGTH)
        SecureRandom().nextBytes(salt)
        return android.util.Base64.encodeToString(salt, android.util.Base64.NO_WRAP)
    }

    /**
     * Hash a PIN with the given salt using PBKDF2.
     * @param pin The 4-digit PIN to hash
     * @param salt Base64-encoded salt
     * @return Base64-encoded hash string
     */
    fun hash(pin: String, salt: String): String {
        val saltBytes = android.util.Base64.decode(salt, android.util.Base64.NO_WRAP)
        val spec = PBEKeySpec(pin.toCharArray(), saltBytes, ITERATIONS, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance(ALGORITHM)
        val hashBytes = factory.generateSecret(spec).encoded
        return android.util.Base64.encodeToString(hashBytes, android.util.Base64.NO_WRAP)
    }

    /**
     * Verify a PIN against a stored hash and salt.
     * Uses constant-time comparison to mitigate timing side-channel attacks.
     * @param pin The PIN to verify
     * @param salt Base64-encoded salt
     * @param storedHash Base64-encoded stored hash
     * @return true if the PIN matches
     */
    fun verify(pin: String, salt: String, storedHash: String): Boolean {
        val computedHash = hash(pin, salt)
        return MessageDigest.isEqual(
            computedHash.toByteArray(Charsets.UTF_8),
            storedHash.toByteArray(Charsets.UTF_8),
        )
    }
}
