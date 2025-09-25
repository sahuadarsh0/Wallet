package com.technitedminds.wallet.domain.usecase.export

import com.technitedminds.wallet.domain.repository.ExportImportRepository
import com.technitedminds.wallet.domain.repository.ExportResult
import java.io.File
import javax.inject.Inject

/** Use case for exporting wallet data to external files for data portability. */
class ExportDataUseCase
@Inject
constructor(private val exportImportRepository: ExportImportRepository) {

    /**
     * Exports all wallet data to a file
     * @param request The export request
     * @return Result containing export result or error
     */
    suspend operator fun invoke(request: ExportDataRequest): Result<ExportResult> {
        return try {
            // Validate the request
            val validationResult = validateRequest(request)
            if (!validationResult.isValid) {
                return Result.failure(IllegalArgumentException(validationResult.errorMessage))
            }

            // Ensure export directory exists
            val exportFile = File(request.exportDirectory, request.fileName)
            if (!exportFile.parentFile?.exists()!!) {
                exportFile.parentFile?.mkdirs()
            }

            // Perform the export
            val exportResult =
                    exportImportRepository.exportData(
                            exportFile = exportFile,
                            includeImages = request.includeImages
                    )

            Result.success(exportResult)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Exports only cards (without categories) to a file
     * @param request The cards export request
     * @return Result containing export result or error
     */
    suspend fun exportCards(request: ExportCardsRequest): Result<ExportResult> {
        return try {
            val validationResult = validateCardsRequest(request)
            if (!validationResult.isValid) {
                return Result.failure(IllegalArgumentException(validationResult.errorMessage))
            }

            val exportFile = File(request.exportDirectory, request.fileName)
            if (!exportFile.parentFile?.exists()!!) {
                exportFile.parentFile?.mkdirs()
            }

            val exportResult =
                    exportImportRepository.exportCards(
                            exportFile = exportFile,
                            cardIds = request.cardIds
                    )

            Result.success(exportResult)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Exports only categories to a file
     * @param request The categories export request
     * @return Result containing export result or error
     */
    suspend fun exportCategories(request: ExportCategoriesRequest): Result<ExportResult> {
        return try {
            val validationResult = validateCategoriesRequest(request)
            if (!validationResult.isValid) {
                return Result.failure(IllegalArgumentException(validationResult.errorMessage))
            }

            val exportFile = File(request.exportDirectory, request.fileName)
            if (!exportFile.parentFile?.exists()!!) {
                exportFile.parentFile?.mkdirs()
            }

            val exportResult = exportImportRepository.exportCategories(exportFile)

            Result.success(exportResult)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Creates a backup of current data
     * @param backupDirectory The directory where backup should be created
     * @return Result containing the backup file or error
     */
    suspend fun createBackup(backupDirectory: File): Result<File> {
        return try {
            if (!backupDirectory.exists()) {
                backupDirectory.mkdirs()
            }

            val backupFile =
                    exportImportRepository.createBackup(backupDirectory)
                            ?: return Result.failure(
                                    IllegalStateException("Failed to create backup")
                            )

            Result.success(backupFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Gets the recommended file extension for exports
     * @return The file extension
     */
    fun getExportFileExtension(): String {
        return exportImportRepository.getExportFileExtension()
    }

    /**
     * Generates a default export file name
     * @param includeTimestamp Whether to include timestamp in the name
     * @return The generated file name
     */
    fun generateExportFileName(includeTimestamp: Boolean = true): String {
        val baseName = "wallet_export"
        val extension = getExportFileExtension()

        return if (includeTimestamp) {
            val timestamp = System.currentTimeMillis()
            "${baseName}_${timestamp}${extension}"
        } else {
            "${baseName}${extension}"
        }
    }

    /** Validates the export data request */
    private fun validateRequest(request: ExportDataRequest): ValidationResult {
        if (request.fileName.isBlank()) {
            return ValidationResult(false, "File name cannot be empty")
        }

        if (!request.exportDirectory.exists() && !request.exportDirectory.mkdirs()) {
            return ValidationResult(false, "Cannot create export directory")
        }

        if (!request.exportDirectory.canWrite()) {
            return ValidationResult(false, "Cannot write to export directory")
        }

        // Check file extension
        val expectedExtension = getExportFileExtension()
        if (!request.fileName.endsWith(expectedExtension)) {
            return ValidationResult(false, "File name must end with $expectedExtension")
        }

        return ValidationResult(true)
    }

    /** Validates the export cards request */
    private fun validateCardsRequest(request: ExportCardsRequest): ValidationResult {
        if (request.fileName.isBlank()) {
            return ValidationResult(false, "File name cannot be empty")
        }

        if (!request.exportDirectory.exists() && !request.exportDirectory.mkdirs()) {
            return ValidationResult(false, "Cannot create export directory")
        }

        if (!request.exportDirectory.canWrite()) {
            return ValidationResult(false, "Cannot write to export directory")
        }

        // Validate card IDs if provided
        request.cardIds?.let { cardIds ->
            if (cardIds.isEmpty()) {
                return ValidationResult(false, "Card IDs list cannot be empty if provided")
            }

            if (cardIds.any { it.isBlank() }) {
                return ValidationResult(false, "Card IDs cannot be blank")
            }
        }

        return ValidationResult(true)
    }

    /** Validates the export categories request */
    private fun validateCategoriesRequest(request: ExportCategoriesRequest): ValidationResult {
        if (request.fileName.isBlank()) {
            return ValidationResult(false, "File name cannot be empty")
        }

        if (!request.exportDirectory.exists() && !request.exportDirectory.mkdirs()) {
            return ValidationResult(false, "Cannot create export directory")
        }

        if (!request.exportDirectory.canWrite()) {
            return ValidationResult(false, "Cannot write to export directory")
        }

        return ValidationResult(true)
    }
}

/** Request for exporting all wallet data */
data class ExportDataRequest(
        val exportDirectory: File,
        val fileName: String,
        val includeImages: Boolean = true
)

/** Request for exporting only cards */
data class ExportCardsRequest(
        val exportDirectory: File,
        val fileName: String,
        val cardIds: List<String>? = null // null means export all cards
)

/** Request for exporting only categories */
data class ExportCategoriesRequest(val exportDirectory: File, val fileName: String)

/** Validation result for export requests */
data class ValidationResult(val isValid: Boolean, val errorMessage: String? = null)
