package com.technitedminds.wallet.domain.model.backup

/**
 * How to handle cards that already exist on the device when restoring a backup.
 * Plain-English copy intentionally lives near the enum so UI and domain stay aligned.
 */
enum class RestoreConflictStrategy(
    val displayTitle: String,
    val displayDescription: String,
) {
    /** Restored card is inserted with a fresh UUID alongside the existing one. */
    KEEP_BOTH(
        displayTitle = "Keep both",
        displayDescription = "Restore as a new card next to the one you already have. " +
            "Nothing on this device is changed.",
    ),

    /** Restored card is dropped if a card with the same id already exists. */
    SKIP_EXISTING(
        displayTitle = "Skip duplicates",
        displayDescription = "Only restore cards that don't already exist on this device. " +
            "Existing cards are left untouched.",
    ),

    /** Restored card replaces any existing card with the same id. */
    OVERWRITE_EXISTING(
        displayTitle = "Overwrite existing",
        displayDescription = "Replace cards on this device with the version from the backup. " +
            "This cannot be undone.",
    ),
}
