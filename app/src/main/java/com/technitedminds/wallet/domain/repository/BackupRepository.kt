package com.technitedminds.wallet.domain.repository

import android.net.Uri
import com.technitedminds.wallet.domain.model.backup.BackupResult
import com.technitedminds.wallet.domain.model.backup.RestoreConflictStrategy
import com.technitedminds.wallet.domain.model.backup.RestorePreview
import com.technitedminds.wallet.domain.model.backup.RestoreResult

/**
 * Phases reported to the UI during long-running export/restore operations.
 * Used purely for status text — never for control flow.
 */
enum class BackupProgress {
    GATHERING_DATA,
    PACKAGING,
    ENCRYPTING,
    WRITING,
    READING,
    DECRYPTING,
    UNPACKING,
    APPLYING,
    DONE,
}

/**
 * Abstraction for fully-offline, password-encrypted backup and restore of the user's wallet.
 *
 * Implementations MUST:
 * - Never make network calls.
 * - Decrypt Tink-encrypted card fields on export so the file is portable to a new keyset.
 * - Re-encrypt them on restore by routing through the device's normal Room write path.
 * - Skip security-sensitive data (PIN, recovery codes, biometric flags, Tink keyset).
 */
interface BackupRepository {

    /**
     * Build, encrypt, and write a `.wallet` backup file to `Downloads/CardVault/`.
     *
     * @param passphrase user-supplied passphrase used to derive the AES-256-GCM key.
     * @param onProgress optional callback invoked off the main thread as phases change.
     */
    suspend fun export(
        passphrase: CharArray,
        onProgress: (BackupProgress) -> Unit = {},
    ): BackupResult

    /**
     * Read header + manifest from [uri] and report what would be restored.
     * Does NOT touch the database.
     */
    suspend fun preview(uri: Uri, passphrase: CharArray): RestorePreview

    /**
     * Apply the backup at [uri] to the local DB + filesystem using [strategy] for collisions.
     */
    suspend fun restore(
        uri: Uri,
        passphrase: CharArray,
        strategy: RestoreConflictStrategy,
        onProgress: (BackupProgress) -> Unit = {},
    ): RestoreResult
}
