package com.technitedminds.wallet.domain.usecase.export

import com.technitedminds.wallet.domain.repository.ConflictResolution
import com.technitedminds.wallet.domain.repository.ExportFileInfo
import com.technitedminds.wallet.domain.repository.ExportImportRepository
import com.technitedminds.wallet.domain.repository.ImportResult
import com.technitedminds.wallet.domain.repository.ValidationResult
import java.io.File
import javax.inject.Inject

/** Use case for importing wallet data from external files for data portability. */
class ImportDataUseCase
@Inject
constructor(private val exportImportRepository: ExportImportRepository) {

    /**
     * Imports wallet data from a file
     * @param request The import request
     * @return Result containing import result or error
     */
    suspend operator fun invoke(request: ImportDataRequest): Result<ImportResult> {
        return try {
            // Validate the request
            val validationResult = validateRequest(request)
            if (!validationResult.isValid) {
                return Result.failure(IllegalArgumentException(validationResult.errorMessage))
            }

            // Validate the import file
            val fileValidation = exportImportRepository.validateImportFile(request.importFile)
            if (!fileValidation.isValid) {
                return Result.failure(
                        IllegalArgumentException(
                                fileValidation.errorMessage ?: "Invalid import file"
                        )
                )
            }

            // Perform the import
            val importResult =
                    exportImportRepository.importData(
                            importFile = request.importFile,
                            conflictResolution = request.conflictResolution
                    )

            Result.success(importResult)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Validates an import file without actually importing
     * @param importFile The file to validate
     * @return Result containing validation result or error
     */
    suspend fun validateImportFile(importFile: File): Result<ValidationResult> {
        return try {
            if (!importFile.exists()) {
                return Result.failure(IllegalArgumentException("Import file does not exist"))
            }

            if (!importFile.canRead()) {
                return Result.failure(IllegalArgumentException("Cannot read import file"))
            }

            val validationResult = exportImportRepository.validateImportFile(importFile)
            Result.success(validationResult)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Gets information about an export file without importing
     * @param importFile The file to analyze
     * @return Result containing export file info or error
     */
    suspend fun getImportFileInfo(importFile: File): Result<ExportFileInfo> {
        return try {
            if (!importFile.exists()) {
                return Result.failure(IllegalArgumentException("Import file does not exist"))
            }

            if (!importFile.canRead()) {
                return Result.failure(IllegalArgumentException("Cannot read import file"))
            }

            val fileInfo =
                    exportImportRepository.getExportFileInfo(importFile)
                            ?: return Result.failure(
                                    IllegalArgumentException("Cannot read file information")
                            )

            Result.success(fileInfo)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Restores data from a backup file
     * @param request The restore request
     * @return Result containing import result or error
     */
    suspend fun restoreFromBackup(request: RestoreBackupRequest): Result<ImportResult> {
        return try {
            val validationResult = validateBackupRequest(request)
            if (!validationResult.isValid) {
                return Result.failure(IllegalArgumentException(validationResult.errorMessage))
            }

            val importResult =
                    exportImportRepository.restoreFromBackup(
                            backupFile = request.backupFile,
                            replaceExisting = request.replaceExisting
                    )

            Result.success(importResult)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Gets supported import file formats
     * @return List of supported file extensions
     */
    fun getSupportedImportFormats(): List<String> {
        return exportImportRepository.getSupportedImportFormats()
    }

    /**
     * Checks if a file format is supported for import
     * @param fileName The file name to check
     * @return True if the format is supported
     */
    fun isFormatSupported(fileName: String): Boolean {
        val supportedFormats = getSupportedImportFormats()
        return supportedFormats.any { format -> fileName.lowercase().endsWith(format.lowercase()) }
    }

    /**
     * Previews what would be imported from a file without actually importing
     * @param importFile The file to preview
     * @return Result containing preview information
     */
    suspend fun previewImport(importFile: File): Result<ImportPreview> {
        return try {
            val fileInfo =
                    getImportFileInfo(importFile).getOrNull()
                            ?: return Result.failure(
                                    IllegalArgumentException("Cannot read file information")
                            )

            val preview =
                    ImportPreview(
                            fileName = importFile.name,
                            fileSize = importFile.length(),
                            cardCount = fileInfo.cardCount,
                            categoryCount = fileInfo.categoryCount,
                            hasImages = fileInfo.hasImages,
                            exportDate = fileInfo.exportDate,
                            appVersion = fileInfo.appVersion
                    )

            Result.success(preview)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Validates the import data request */
    private fun validateRequest(request: ImportDataRequest): ValidationResult {
        if (!request.importFile.exists()) {
            return ValidationResult(false, "Import file does not exist")
        }

        if (!request.importFile.canRead()) {
            return ValidationResult(false, "Cannot read import file")
        }

        if (request.importFile.length() == 0L) {
            return ValidationResult(false, "Import file is empty")
        }

        if (request.importFile.length() > 100 * 1024 * 1024) { // 100MB limit
            return ValidationResult(false, "Import file is too large (max 100MB)")
        }

        // Check file extension
        if (!isFormatSupported(request.importFile.name)) {
            val supportedFormats = getSupportedImportFormats().joinToString(", ")
            return ValidationResult(
                    false,
                    "Unsupported file format. Supported formats: $supportedFormats"
            )
        }

        return ValidationResult(true)
    }

    /** Validates the restore backup request */
    private fun validateBackupRequest(request: RestoreBackupRequest): ValidationResult {
        if (!request.backupFile.exists()) {
            return ValidationResult(false, "Backup file does not exist")
        }

        if (!request.backupFile.canRead()) {
            return ValidationResult(false, "Cannot read backup file")
        }

        if (request.backupFile.length() == 0L) {
            return ValidationResult(false, "Backup file is empty")
        }

        return ValidationResult(true)
    }
}

/** Request for importing wallet data */
data class ImportDataRequest(
        val importFile: File,
        val conflictResolution: ConflictResolution = ConflictResolution.SKIP
)

/** Request for restoring from backup */
data class RestoreBackupRequest(val backupFile: File, val replaceExisting: Boolean = false)

/** Preview information for an import operation */
data class ImportPreview(
        val fileName: String,
        val fileSize: Long,
        val cardCount: Int,
        val categoryCount: Int,
        val hasImages: Boolean,
        val exportDate: Long,
        val appVersion: String?
) {
    val fileSizeMB: Double
        get() = fileSize / (1024.0 * 1024.0)
}
