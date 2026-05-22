package com.technitedminds.wallet.domain.model.backup

import android.net.Uri

/**
 * Outcome of an export operation.
 */
sealed interface BackupResult {
    data class Success(
        val fileUri: Uri,
        val displayName: String,
        val sizeBytes: Long,
        val cardCount: Int,
        val categoryCount: Int,
        val imageCount: Int,
    ) : BackupResult

    data class Failure(val reason: BackupFailureReason, val message: String? = null) : BackupResult
}

enum class BackupFailureReason {
    STORAGE_FULL,
    IO_ERROR,
    CRYPTO_ERROR,
    CANCELLED,
    UNKNOWN,
}
