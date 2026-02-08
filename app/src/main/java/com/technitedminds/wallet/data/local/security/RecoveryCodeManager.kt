package com.technitedminds.wallet.data.local.security

import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages recovery (backup) codes for PIN reset.
 * Generates human-readable codes, hashes them for storage, and verifies user input.
 * The plaintext code is only shown once to the user; only the hash is persisted.
 */
@Singleton
class RecoveryCodeManager @Inject constructor(
    private val pinHasher: PinHasher,
) {

    companion object {
        private const val CODE_LENGTH = 16
        private const val CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789" // no O/0/1/I for readability
        private const val GROUP_SIZE = 4
        private const val GROUP_SEPARATOR = "-"
    }

    /**
     * Result of generating a new recovery code.
     * [plaintextCode] must be shown to the user ONCE.
     * [hash] and [salt] should be persisted.
     */
    data class GeneratedCode(
        val plaintextCode: String,
        val hash: String,
        val salt: String,
    )

    /**
     * Generate a new recovery code.
     * @return [GeneratedCode] containing the plaintext for display and the hash/salt for storage.
     */
    fun generate(): GeneratedCode {
        val rawCode = buildString {
            val random = SecureRandom()
            repeat(CODE_LENGTH) {
                append(CODE_CHARS[random.nextInt(CODE_CHARS.length)])
            }
        }
        // Format as XXXX-XXXX-XXXX-XXXX for readability
        val formatted = rawCode.chunked(GROUP_SIZE).joinToString(GROUP_SEPARATOR)

        val salt = pinHasher.generateSalt()
        val hash = pinHasher.hash(normalise(formatted), salt)

        return GeneratedCode(
            plaintextCode = formatted,
            hash = hash,
            salt = salt,
        )
    }

    /**
     * Verify a user-entered recovery code against the stored hash.
     * Accepts with or without dashes, case-insensitive.
     */
    fun verify(input: String, salt: String, storedHash: String): Boolean {
        return pinHasher.verify(normalise(input), salt, storedHash)
    }

    /** Strip dashes/spaces and upper-case for consistent hashing */
    private fun normalise(code: String): String =
        code.replace("-", "").replace(" ", "").uppercase()
}
