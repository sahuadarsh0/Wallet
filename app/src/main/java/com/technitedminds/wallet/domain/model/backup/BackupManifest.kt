package com.technitedminds.wallet.domain.model.backup

import kotlinx.serialization.Serializable

/**
 * Top-level manifest written into the encrypted ZIP payload of a `.wallet` backup.
 *
 * Card maps ([BackupCard.extractedData], [BackupCard.customFields]) are stored as
 * Tink-decrypted plaintext here so the backup is fully portable across devices —
 * the destination device's [com.technitedminds.wallet.data.local.security.TinkEncryptionManager]
 * re-encrypts them with its own keyset on restore.
 */
@Serializable
data class BackupManifest(
    /** Manifest schema version. Bump when fields are added/removed. */
    val schema: Int = SCHEMA_VERSION,

    /** Source app version that produced this backup. */
    val appVersion: String,

    /** Epoch ms when the backup was generated. */
    val createdAtEpochMs: Long,

    val cards: List<BackupCard>,
    val categories: List<BackupCategory>,
    val settings: BackupSettings = BackupSettings(),

    /** Relative paths of image files included in the ZIP under `images/`. */
    val imageFiles: List<String> = emptyList(),
) {
    companion object {
        const val SCHEMA_VERSION = 1
        const val MANIFEST_ENTRY_NAME = "manifest.json"
        const val IMAGES_DIR = "images"
    }
}

@Serializable
data class BackupCard(
    val id: String,
    val name: String,
    val typeKey: String,
    val customTypeName: String? = null,
    val customTypeColorHex: String? = null,
    val categoryId: String,
    val frontImageRelPath: String? = null,
    val backImageRelPath: String? = null,
    val extractedData: Map<String, String> = emptyMap(),
    val customFields: Map<String, String> = emptyMap(),
    val expiryDate: String? = null,
    val notes: String? = null,
    val customGradient: BackupGradient? = null,
    val createdAt: Long,
    val updatedAt: Long,
)

@Serializable
data class BackupGradient(
    val startColor: String,
    val endColor: String,
    val direction: String,
    val name: String? = null,
)

@Serializable
data class BackupCategory(
    val id: String,
    val name: String,
    val description: String? = null,
    val colorHex: String,
    val iconName: String? = null,
    val sortOrder: Int = 0,
    val createdAt: Long,
    val updatedAt: Long,
)

/**
 * Non-sensitive settings carried across devices. Security-related preferences
 * (PIN, biometric, lockout, recovery code, Tink keyset) are intentionally excluded.
 */
@Serializable
data class BackupSettings(
    val themeMode: String? = null,
    val folderTheme: String? = null,
    val backgroundPattern: String? = null,
    val cardFlipAnimationEnabled: Boolean? = null,
    val cameraFlashEnabled: Boolean? = null,
    val preferredCardAspectRatio: String? = null,
    val defaultCardCategory: String? = null,
)
