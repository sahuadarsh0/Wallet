package com.technitedminds.wallet.domain.model.backup

/**
 * Metadata + counts shown to the user before they commit to a restore.
 * Computed by reading + decrypting the backup file's manifest only — no DB writes happen.
 */
data class RestorePreview(
    val schema: Int,
    val sourceAppVersion: String,
    val createdAtEpochMs: Long,
    val cardCount: Int,
    val categoryCount: Int,
    val imageCount: Int,
    /** Number of card ids in the backup that already exist on this device. */
    val conflictingCardCount: Int,
)

/**
 * Result of a successful restore.
 */
data class RestoreResult(
    val cardsAdded: Int,
    val cardsSkipped: Int,
    val cardsOverwritten: Int,
    val categoriesAdded: Int,
    val categoriesSkipped: Int,
    val imagesRestored: Int,
)
