package com.technitedminds.wallet.data.backup

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.GeneralSecurityException
import java.security.SecureRandom
import java.util.Locale
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Cleartext header that prefixes every `.wallet` file. It carries no secret data,
 * only the parameters needed to derive the key and decrypt the payload, plus a
 * schema/version stamp so we can show validation errors before asking for the password.
 */
@Serializable
data class BackupHeader(
    val schema: Int = SCHEMA_VERSION,
    val appVersion: String,
    val createdAtEpochMs: Long,
    val kdf: String = KDF_PBKDF2,
    val pbkdf2: Pbkdf2Params? = null,
    val cipher: String = CIPHER_AES_GCM,
    val payloadFormat: String = PAYLOAD_ZIP,
) {
    companion object {
        const val SCHEMA_VERSION = 1
        const val KDF_PBKDF2 = "pbkdf2-hmac-sha256"
        const val CIPHER_AES_GCM = "aes-256-gcm"
        const val PAYLOAD_ZIP = "zip"
    }
}

@Serializable
data class Pbkdf2Params(
    val saltBase64: String,
    val iterations: Int,
    val keyLengthBits: Int,
)

/**
 * Thrown when a `.wallet` file fails sanity checks before decryption is attempted.
 */
class BackupFormatException(message: String) : IOException(message)

/**
 * Thrown when the GCM authentication tag fails — usually means a wrong passphrase
 * or a corrupted file. Caller should surface a friendly "Incorrect password" message.
 */
class BackupAuthenticationException(message: String, cause: Throwable? = null) :
    IOException(message, cause)

/**
 * Encryption/decryption primitives for the offline `.wallet` backup format.
 *
 * File layout (binary):
 * ```
 * 0..7       MAGIC          ASCII "CVAULT01"
 * 8..11      headerLen      uint32 BE
 * 12..N      header         JSON, UTF-8 (cleartext)
 * N..N+12    nonce          12 bytes (random, AES-GCM)
 * N+12..end  ciphertext     AES-256-GCM(plaintext = ZIP, AAD = magic||headerLen||header)
 * ```
 *
 * Key derivation: PBKDF2-HMAC-SHA256 with a per-backup random salt and 200 000 iterations.
 * The GCM auth tag binds the cleartext header to the ciphertext via AAD, so tampering
 * with the header (e.g. swapping iteration counts to weaken brute-force) fails decryption.
 */
@Singleton
class BackupCrypto @Inject constructor() {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    /**
     * Derives a 256-bit AES key from [passphrase] using PBKDF2-HMAC-SHA256.
     * The caller is expected to clear [passphrase] when finished.
     */
    fun deriveKey(passphrase: CharArray, params: Pbkdf2Params): SecretKey {
        val saltBytes = Base64Compat.decode(params.saltBase64)
        val spec = PBEKeySpec(passphrase, saltBytes, params.iterations, params.keyLengthBits)
        try {
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val raw = factory.generateSecret(spec).encoded
            return SecretKeySpec(raw, "AES")
        } finally {
            spec.clearPassword()
        }
    }

    /** Generates fresh KDF parameters with a cryptographically-random salt. */
    fun newPbkdf2Params(): Pbkdf2Params {
        val salt = ByteArray(SALT_LEN_BYTES).also { secureRandom.nextBytes(it) }
        return Pbkdf2Params(
            saltBase64 = Base64Compat.encode(salt),
            iterations = PBKDF2_ITERATIONS,
            keyLengthBits = AES_KEY_LEN_BITS,
        )
    }

    /**
     * Streams [zipPayload] through AES-256-GCM and writes the full `.wallet` envelope
     * (magic, header, nonce, ciphertext) to [out].
     *
     * The cleartext header (magic + length + JSON bytes) is bound into the GCM auth tag
     * via AAD, so any post-write tampering with header parameters is detected on restore.
     */
    fun writeEncryptedEnvelope(
        out: OutputStream,
        header: BackupHeader,
        key: SecretKey,
        zipPayload: ByteArray,
    ) {
        val headerJson = json.encodeToString(header).toByteArray(Charsets.UTF_8)
        val headerLenBytes = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN)
            .putInt(headerJson.size).array()

        val nonce = ByteArray(NONCE_LEN_BYTES).also { secureRandom.nextBytes(it) }

        val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LEN_BITS, nonce))
        cipher.updateAAD(MAGIC_BYTES)
        cipher.updateAAD(headerLenBytes)
        cipher.updateAAD(headerJson)
        val ciphertext = cipher.doFinal(zipPayload)

        out.write(MAGIC_BYTES)
        out.write(headerLenBytes)
        out.write(headerJson)
        out.write(nonce)
        out.write(ciphertext)
        out.flush()
    }

    /**
     * Parses just the magic + cleartext header from [input] without consuming the
     * encrypted body. Used to display a "Restore preview" without prompting for a
     * password (and to fail fast on corrupt files).
     */
    fun readHeader(input: InputStream): BackupHeader {
        val magic = ByteArray(MAGIC_BYTES.size)
        readFully(input, magic, "magic")
        if (!magic.contentEquals(MAGIC_BYTES)) {
            throw BackupFormatException(
                "Not a CardVault backup file (magic mismatch: ${magic.toHex()}).",
            )
        }

        val lenBuf = ByteArray(4)
        readFully(input, lenBuf, "header length")
        val headerLen = ByteBuffer.wrap(lenBuf).order(ByteOrder.BIG_ENDIAN).int
        if (headerLen <= 0 || headerLen > MAX_HEADER_BYTES) {
            throw BackupFormatException("Invalid backup header length: $headerLen")
        }

        val headerBytes = ByteArray(headerLen)
        readFully(input, headerBytes, "header body")
        return try {
            json.decodeFromString(BackupHeader.serializer(), headerBytes.toString(Charsets.UTF_8))
        } catch (e: Exception) {
            throw BackupFormatException("Backup header is corrupted: ${e.message}")
        }
    }

    /**
     * Reads the full envelope and returns the decrypted ZIP payload.
     * Throws [BackupAuthenticationException] on wrong passphrase or tampering.
     */
    fun decryptEnvelope(
        input: InputStream,
        passphrase: CharArray,
    ): Pair<BackupHeader, ByteArray> {
        val all = input.readBytes()
        if (all.size < MAGIC_BYTES.size + 4) {
            throw BackupFormatException("Backup file is truncated.")
        }

        val magic = all.copyOfRange(0, MAGIC_BYTES.size)
        if (!magic.contentEquals(MAGIC_BYTES)) {
            throw BackupFormatException("Not a CardVault backup file.")
        }
        val headerLen = ByteBuffer.wrap(all, MAGIC_BYTES.size, 4)
            .order(ByteOrder.BIG_ENDIAN).int
        if (headerLen <= 0 || headerLen > MAX_HEADER_BYTES) {
            throw BackupFormatException("Invalid backup header length: $headerLen")
        }
        val headerStart = MAGIC_BYTES.size + 4
        val headerEnd = headerStart + headerLen
        if (headerEnd + NONCE_LEN_BYTES > all.size) {
            throw BackupFormatException("Backup file is truncated.")
        }

        val headerBytes = all.copyOfRange(headerStart, headerEnd)
        val header = try {
            json.decodeFromString(BackupHeader.serializer(), headerBytes.toString(Charsets.UTF_8))
        } catch (e: Exception) {
            throw BackupFormatException("Backup header is corrupted: ${e.message}")
        }
        if (header.schema > BackupHeader.SCHEMA_VERSION) {
            throw BackupFormatException(
                "This backup was created by a newer version of CardVault " +
                    "(schema ${header.schema}). Please update the app and try again.",
            )
        }
        val params = header.pbkdf2
            ?: throw BackupFormatException("Backup is missing key-derivation parameters.")

        val nonce = all.copyOfRange(headerEnd, headerEnd + NONCE_LEN_BYTES)
        val ciphertext = all.copyOfRange(headerEnd + NONCE_LEN_BYTES, all.size)

        val headerLenBytes = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN)
            .putInt(headerLen).array()

        val key = deriveKey(passphrase, params)
        val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LEN_BITS, nonce))
        cipher.updateAAD(MAGIC_BYTES)
        cipher.updateAAD(headerLenBytes)
        cipher.updateAAD(headerBytes)
        return try {
            header to cipher.doFinal(ciphertext)
        } catch (e: GeneralSecurityException) {
            throw BackupAuthenticationException(
                "Incorrect password or the backup file has been tampered with.",
                e,
            )
        }
    }

    private fun readFully(input: InputStream, buf: ByteArray, what: String) {
        var read = 0
        while (read < buf.size) {
            val n = input.read(buf, read, buf.size - read)
            if (n < 0) throw BackupFormatException("Unexpected end of file while reading $what.")
            read += n
        }
    }

    private fun ByteArray.toHex(): String =
        joinToString("") { String.format(Locale.US, "%02x", it) }

    companion object {
        /** ASCII "CVAULT01" — magic + version of the binary envelope (NOT the manifest). */
        val MAGIC_BYTES: ByteArray = "CVAULT01".toByteArray(Charsets.US_ASCII)
        const val NONCE_LEN_BYTES = 12
        const val GCM_TAG_LEN_BITS = 128
        const val AES_KEY_LEN_BITS = 256
        const val SALT_LEN_BYTES = 32

        /** PBKDF2-HMAC-SHA256 iteration count. ~200ms on a modern phone. */
        const val PBKDF2_ITERATIONS = 200_000

        /** Hard cap on cleartext header size to bound memory usage on malicious inputs. */
        const val MAX_HEADER_BYTES = 64 * 1024

        private const val CIPHER_TRANSFORMATION = "AES/GCM/NoPadding"

        private val secureRandom = SecureRandom()
    }
}

/**
 * Tiny Base64 helper that doesn't depend on android.util (so this file stays JVM-friendly
 * for unit tests). Uses java.util.Base64, which is available on API 26+.
 */
internal object Base64Compat {
    private val encoder = java.util.Base64.getEncoder().withoutPadding()
    private val decoder = java.util.Base64.getDecoder()
    fun encode(bytes: ByteArray): String = encoder.encodeToString(bytes)
    fun decode(s: String): ByteArray = decoder.decode(s)
}
