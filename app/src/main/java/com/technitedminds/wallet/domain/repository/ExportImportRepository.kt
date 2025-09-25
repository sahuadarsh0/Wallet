package com.technitedminds.wallet.domain.repository

import java.io.File

/**
 * Repository interface for data export and import operations. Handles data portability features for
 * backing up and restoring wallet data.
 */
interface ExportImportRepository {

    /**
     * Exports all wallet data to a single file
     * @param exportFile The file where data should be exported
     * @param includeImages Whether to include card images in the export
     * @return ExportResult containing success status and details
     */
    suspend fun exportData(exportFile: File, includeImages: Boolean = true): ExportResult

    /**
     * Imports wallet data from a file
     * @param importFile The file containing exported data
     * @param conflictResolution How to handle conflicts with existing data
     * @return ImportResult containing success status and details
     */
    suspend fun importData(
            importFile: File,
            conflictResolution: ConflictResolution = ConflictResolution.SKIP
    ): ImportResult

    /**
     * Validates an export file before importing
     * @param importFile The file to validate
     * @return ValidationResult containing validation status and details
     */
    suspend fun validateImportFile(importFile: File): ValidationResult

    /**
     * Gets export file information without importing
     * @param importFile The export file to analyze
     * @return ExportFileInfo containing metadata about the export
     */
    suspend fun getExportFileInfo(importFile: File): ExportFileInfo?

    /**
     * Creates a backup of current data
     * @param backupDirectory The directory where backup should be created
     * @return The created backup file
     */
    suspend fun createBackup(backupDirectory: File): File?

    /**
     * Restores data from a backup file
     * @param backupFile The backup file to restore from
     * @param replaceExisting Whether to replace existing data
     * @return ImportResult containing restore status and details
     */
    suspend fun restoreFromBackup(backupFile: File, replaceExisting: Boolean = false): ImportResult

    /**
     * Exports only cards (without categories) to a file
     * @param exportFile The file where cards should be exported
     * @param cardIds Optional list of specific card IDs to export
     * @return ExportResult containing success status and details
     */
    suspend fun exportCards(exportFile: File, cardIds: List<String>? = null): ExportResult

    /**
     * Exports only categories to a file
     * @param exportFile The file where categories should be exported
     * @return ExportResult containing success status and details
     */
    suspend fun exportCategories(exportFile: File): ExportResult

    /**
     * Gets the recommended file extension for exports
     * @return The file extension (e.g., ".wallet")
     */
    fun getExportFileExtension(): String

    /**
     * Gets supported import file formats
     * @return List of supported file extensions
     */
    fun getSupportedImportFormats(): List<String>
}

/** Result of an export operation */
data class ExportResult(
        val success: Boolean,
        val exportedFile: File?,
        val cardsExported: Int = 0,
        val categoriesExported: Int = 0,
        val imagesExported: Int = 0,
        val fileSizeBytes: Long = 0,
        val errorMessage: String? = null,
        val exportDurationMs: Long = 0
)

/** Result of an import operation */
data class ImportResult(
        val success: Boolean,
        val cardsImported: Int = 0,
        val categoriesImported: Int = 0,
        val imagesImported: Int = 0,
        val cardsSkipped: Int = 0,
        val categoriesSkipped: Int = 0,
        val cardsUpdated: Int = 0,
        val categoriesUpdated: Int = 0,
        val errorMessage: String? = null,
        val importDurationMs: Long = 0,
        val warnings: List<String> = emptyList()
)

/** Result of file validation */
data class ValidationResult(
        val isValid: Boolean,
        val fileFormat: String? = null,
        val version: String? = null,
        val errorMessage: String? = null,
        val warnings: List<String> = emptyList()
)

/** Information about an export file */
data class ExportFileInfo(
        val fileFormat: String,
        val version: String,
        val exportDate: Long,
        val cardCount: Int,
        val categoryCount: Int,
        val hasImages: Boolean,
        val fileSizeBytes: Long,
        val appVersion: String? = null
)

/** Enum for handling conflicts during import */
enum class ConflictResolution {
    /** Skip items that already exist */
    SKIP,

    /** Replace existing items with imported ones */
    REPLACE,

    /** Keep existing items and skip imported ones */
    KEEP_EXISTING,

    /** Create new items with modified names/IDs */
    CREATE_NEW
}
