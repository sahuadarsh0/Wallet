package com.technitedminds.wallet.data.repository

import com.technitedminds.wallet.domain.model.Card
import com.technitedminds.wallet.domain.model.Category
import com.technitedminds.wallet.domain.repository.CardRepository
import com.technitedminds.wallet.domain.repository.CategoryRepository
import com.technitedminds.wallet.domain.repository.ConflictResolution
import com.technitedminds.wallet.domain.repository.ExportFileInfo
import com.technitedminds.wallet.domain.repository.ExportImportRepository
import com.technitedminds.wallet.domain.repository.ExportResult
import com.technitedminds.wallet.domain.repository.ImageRepository
import com.technitedminds.wallet.domain.repository.ImportResult
import com.technitedminds.wallet.domain.repository.ValidationResult
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject

/**
 * Implementation of ExportImportRepository for data portability. Handles JSON serialization and
 * file operations for backup/restore functionality.
 */
@Singleton
class ExportImportRepositoryImpl
@Inject
constructor(
        private val cardRepository: CardRepository,
        private val categoryRepository: CategoryRepository,
        private val imageRepository: ImageRepository
) : ExportImportRepository {

    companion object {
        private const val EXPORT_FILE_EXTENSION = ".wallet"
        private const val EXPORT_VERSION = "1.0"
        private const val CARDS_FILE = "cards.json"
        private const val CATEGORIES_FILE = "categories.json"
        private const val METADATA_FILE = "metadata.json"
        private const val IMAGES_FOLDER = "images/"
    }

    override suspend fun exportData(exportFile: File, includeImages: Boolean): ExportResult {
        val startTime = System.currentTimeMillis()

        return try {
            // Get all data
            val cards = cardRepository.getAllCards().first()
            val categories = categoryRepository.getAllCategories().first()

            // Create metadata
            val metadata =
                    JSONObject().apply {
                        put("version", EXPORT_VERSION)
                        put("exportDate", System.currentTimeMillis())
                        put("cardCount", cards.size)
                        put("categoryCount", categories.size)
                        put(
                                "hasImages",
                                includeImages &&
                                        cards.any {
                                            it.frontImagePath.isNotBlank() ||
                                                    it.backImagePath.isNotBlank()
                                        }
                        )
                        put("appVersion", getAppVersion())
                    }

            var imagesExported = 0

            // Create ZIP file
            ZipOutputStream(exportFile.outputStream()).use { zipOut ->
                // Add metadata
                zipOut.putNextEntry(ZipEntry(METADATA_FILE))
                zipOut.write(metadata.toString().toByteArray())
                zipOut.closeEntry()

                // Add cards
                zipOut.putNextEntry(ZipEntry(CARDS_FILE))
                zipOut.write(cardsToJson(cards).toByteArray())
                zipOut.closeEntry()

                // Add categories
                zipOut.putNextEntry(ZipEntry(CATEGORIES_FILE))
                zipOut.write(categoriesToJson(categories).toByteArray())
                zipOut.closeEntry()

                // Add images if requested
                if (includeImages) {
                    for (card in cards) {
                        // Export front image
                        if (card.frontImagePath.isNotBlank()) {
                            val imageData = imageRepository.getImage(card.frontImagePath)
                            if (imageData != null) {
                                val imageName = "${card.id}_front.jpg"
                                zipOut.putNextEntry(ZipEntry(IMAGES_FOLDER + imageName))
                                zipOut.write(imageData)
                                zipOut.closeEntry()
                                imagesExported++
                            }
                        }

                        // Export back image
                        if (card.backImagePath.isNotBlank()) {
                            val imageData = imageRepository.getImage(card.backImagePath)
                            if (imageData != null) {
                                val imageName = "${card.id}_back.jpg"
                                zipOut.putNextEntry(ZipEntry(IMAGES_FOLDER + imageName))
                                zipOut.write(imageData)
                                zipOut.closeEntry()
                                imagesExported++
                            }
                        }
                    }
                }
            }

            val duration = System.currentTimeMillis() - startTime

            ExportResult(
                    success = true,
                    exportedFile = exportFile,
                    cardsExported = cards.size,
                    categoriesExported = categories.size,
                    imagesExported = imagesExported,
                    fileSizeBytes = exportFile.length(),
                    exportDurationMs = duration
            )
        } catch (e: Exception) {
            ExportResult(
                    success = false,
                    exportedFile = null,
                    errorMessage = "Export failed: ${e.message}",
                    exportDurationMs = System.currentTimeMillis() - startTime
            )
        }
    }

    override suspend fun importData(
            importFile: File,
            conflictResolution: ConflictResolution
    ): ImportResult {
        val startTime = System.currentTimeMillis()

        return try {
            var cardsImported = 0
            var categoriesImported = 0
            var imagesImported = 0
            var cardsSkipped = 0
            var categoriesSkipped = 0
            var cardsUpdated = 0
            var categoriesUpdated = 0
            val warnings = mutableListOf<String>()

            // Read ZIP file
            ZipInputStream(importFile.inputStream()).use { zipIn ->
                var entry: ZipEntry?
                val fileContents = mutableMapOf<String, ByteArray>()

                // Read all entries
                while (zipIn.nextEntry.also { entry = it } != null) {
                    val entryName = entry!!.name
                    val content = zipIn.readBytes()
                    fileContents[entryName] = content
                }

                // Parse metadata
                val metadataContent =
                        fileContents[METADATA_FILE]
                                ?: throw IllegalArgumentException("Missing metadata file")
                val metadata = JSONObject(String(metadataContent))

                // Parse and import categories first
                val categoriesContent =
                        fileContents[CATEGORIES_FILE]
                                ?: throw IllegalArgumentException("Missing categories file")
                val categoriesJson = JSONArray(String(categoriesContent))

                for (i in 0 until categoriesJson.length()) {
                    val categoryJson = categoriesJson.getJSONObject(i)
                    val category = jsonToCategory(categoryJson)
                    val exists = categoryRepository.categoryExists(category.id)

                    when {
                        !exists -> {
                            categoryRepository.addCategory(category)
                            categoriesImported++
                        }
                        conflictResolution == ConflictResolution.REPLACE -> {
                            categoryRepository.updateCategory(category)
                            categoriesUpdated++
                        }
                        conflictResolution == ConflictResolution.SKIP -> {
                            categoriesSkipped++
                        }
                        conflictResolution == ConflictResolution.KEEP_EXISTING -> {
                            categoriesSkipped++
                        }
                        conflictResolution == ConflictResolution.CREATE_NEW -> {
                            val newCategory =
                                    category.copy(
                                            id =
                                                    "${category.id}_imported_${System.currentTimeMillis()}",
                                            name = "${category.name} (Imported)"
                                    )
                            categoryRepository.addCategory(newCategory)
                            categoriesImported++
                        }
                    }
                }

                // Parse and import cards
                val cardsContent =
                        fileContents[CARDS_FILE]
                                ?: throw IllegalArgumentException("Missing cards file")
                val cardsJson = JSONArray(String(cardsContent))

                for (i in 0 until cardsJson.length()) {
                    val cardJson = cardsJson.getJSONObject(i)
                    val card = jsonToCard(cardJson)
                    val exists = cardRepository.cardExists(card.id)

                    // Handle images
                    var frontImagePath = ""
                    var backImagePath = ""

                    if (metadata.getBoolean("hasImages")) {
                        // Import front image
                        val frontImageName = "${card.id}_front.jpg"
                        val frontImageData = fileContents[IMAGES_FOLDER + frontImageName]
                        if (frontImageData != null) {
                            frontImagePath =
                                    imageRepository.saveImage(
                                            frontImageData,
                                            card.id,
                                            com.technitedminds.wallet.domain.model.ImageType.FRONT
                                    )
                            imagesImported++
                        }

                        // Import back image
                        val backImageName = "${card.id}_back.jpg"
                        val backImageData = fileContents[IMAGES_FOLDER + backImageName]
                        if (backImageData != null) {
                            backImagePath =
                                    imageRepository.saveImage(
                                            backImageData,
                                            card.id,
                                            com.technitedminds.wallet.domain.model.ImageType.BACK
                                    )
                            imagesImported++
                        }
                    }

                    val cardWithImages =
                            card.copy(
                                    frontImagePath = frontImagePath.ifEmpty { card.frontImagePath },
                                    backImagePath = backImagePath.ifEmpty { card.backImagePath }
                            )

                    when {
                        !exists -> {
                            cardRepository.insertCard(cardWithImages)
                            cardsImported++
                        }
                        conflictResolution == ConflictResolution.REPLACE -> {
                            cardRepository.updateCard(cardWithImages)
                            cardsUpdated++
                        }
                        conflictResolution == ConflictResolution.SKIP -> {
                            cardsSkipped++
                        }
                        conflictResolution == ConflictResolution.KEEP_EXISTING -> {
                            cardsSkipped++
                        }
                        conflictResolution == ConflictResolution.CREATE_NEW -> {
                            val newCard =
                                    cardWithImages.copy(
                                            id =
                                                    "${card.id}_imported_${System.currentTimeMillis()}",
                                            name = "${card.name} (Imported)"
                                    )
                            cardRepository.insertCard(newCard)
                            cardsImported++
                        }
                    }
                }
            }

            val duration = System.currentTimeMillis() - startTime

            ImportResult(
                    success = true,
                    cardsImported = cardsImported,
                    categoriesImported = categoriesImported,
                    imagesImported = imagesImported,
                    cardsSkipped = cardsSkipped,
                    categoriesSkipped = categoriesSkipped,
                    cardsUpdated = cardsUpdated,
                    categoriesUpdated = categoriesUpdated,
                    importDurationMs = duration,
                    warnings = warnings
            )
        } catch (e: Exception) {
            ImportResult(
                    success = false,
                    errorMessage = "Import failed: ${e.message}",
                    importDurationMs = System.currentTimeMillis() - startTime
            )
        }
    }

    override suspend fun validateImportFile(importFile: File): ValidationResult {
        return try {
            if (!importFile.exists()) {
                return ValidationResult(false, errorMessage = "File does not exist")
            }

            if (!importFile.name.endsWith(EXPORT_FILE_EXTENSION)) {
                return ValidationResult(false, errorMessage = "Invalid file format")
            }

            // Try to read metadata
            ZipInputStream(importFile.inputStream()).use { zipIn ->
                var entry: ZipEntry?
                var foundMetadata = false

                while (zipIn.nextEntry.also { entry = it } != null) {
                    if (entry!!.name == METADATA_FILE) {
                        val content = zipIn.readBytes()
                        val metadata = JSONObject(String(content))
                        foundMetadata = true

                        return ValidationResult(
                                isValid = true,
                                fileFormat = "CardVault Wallet Export",
                                version = metadata.getString("version")
                        )
                    }
                }

                if (!foundMetadata) {
                    return ValidationResult(false, errorMessage = "Invalid wallet export file")
                }
            }

            ValidationResult(false, errorMessage = "Unknown error")
        } catch (e: Exception) {
            ValidationResult(false, errorMessage = "File validation failed: ${e.message}")
        }
    }

    override suspend fun getExportFileInfo(importFile: File): ExportFileInfo? {
        return try {
            ZipInputStream(importFile.inputStream()).use { zipIn ->
                var entry: ZipEntry?

                while (zipIn.nextEntry.also { entry = it } != null) {
                    if (entry!!.name == METADATA_FILE) {
                        val content = zipIn.readBytes()
                        val metadata = JSONObject(String(content))

                        return ExportFileInfo(
                                fileFormat = "CardVault Wallet Export",
                                version = metadata.getString("version"),
                                exportDate = metadata.getLong("exportDate"),
                                cardCount = metadata.getInt("cardCount"),
                                categoryCount = metadata.getInt("categoryCount"),
                                hasImages = metadata.getBoolean("hasImages"),
                                fileSizeBytes = importFile.length(),
                                appVersion = metadata.optString("appVersion", "Unknown")
                        )
                    }
                }
            }

            null
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun createBackup(backupDirectory: File): File? {
        return try {
            val timestamp = System.currentTimeMillis()
            val backupFile = File(backupDirectory, "wallet_backup_$timestamp$EXPORT_FILE_EXTENSION")

            val result = exportData(backupFile, includeImages = true)
            if (result.success) backupFile else null
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun restoreFromBackup(
            backupFile: File,
            replaceExisting: Boolean
    ): ImportResult {
        val conflictResolution =
                if (replaceExisting) {
                    ConflictResolution.REPLACE
                } else {
                    ConflictResolution.SKIP
                }

        return importData(backupFile, conflictResolution)
    }

    override suspend fun exportCards(exportFile: File, cardIds: List<String>?): ExportResult {
        // Simplified implementation - would filter cards by IDs in a real implementation
        return exportData(exportFile, includeImages = true)
    }

    override suspend fun exportCategories(exportFile: File): ExportResult {
        // Simplified implementation - would export only categories in a real implementation
        return exportData(exportFile, includeImages = false)
    }

    override fun getExportFileExtension(): String = EXPORT_FILE_EXTENSION

    override fun getSupportedImportFormats(): List<String> = listOf(EXPORT_FILE_EXTENSION)

    private fun getAppVersion(): String {
        // This would typically get the actual app version
        return "1.0.0"
    }

    // JSON conversion functions
    private fun cardsToJson(cards: List<Card>): String {
        val jsonArray = JSONArray()
        for (card in cards) {
            jsonArray.put(cardToJson(card))
        }
        return jsonArray.toString()
    }

    private fun cardToJson(card: Card): JSONObject {
        return JSONObject().apply {
            put("id", card.id)
            put("name", card.name)
            put(
                    "type",
                    when (card.type) {
                        is com.technitedminds.wallet.domain.model.CardType.Credit -> "Credit"
                        is com.technitedminds.wallet.domain.model.CardType.Debit -> "Debit"
                        is com.technitedminds.wallet.domain.model.CardType.ATM -> "ATM"
                        is com.technitedminds.wallet.domain.model.CardType.GiftCard -> "GiftCard"
                        is com.technitedminds.wallet.domain.model.CardType.LoyaltyCard -> "LoyaltyCard"
                        is com.technitedminds.wallet.domain.model.CardType.MembershipCard -> "MembershipCard"
                        is com.technitedminds.wallet.domain.model.CardType.InsuranceCard -> "InsuranceCard"
                        is com.technitedminds.wallet.domain.model.CardType.IdentificationCard -> "IdentificationCard"
                        is com.technitedminds.wallet.domain.model.CardType.TransportCard -> "TransportCard"
                        is com.technitedminds.wallet.domain.model.CardType.BusinessCard -> "BusinessCard"
                        is com.technitedminds.wallet.domain.model.CardType.LibraryCard -> "LibraryCard"
                        is com.technitedminds.wallet.domain.model.CardType.HotelCard -> "HotelCard"
                        is com.technitedminds.wallet.domain.model.CardType.StudentCard -> "StudentCard"
                        is com.technitedminds.wallet.domain.model.CardType.AccessCard -> "AccessCard"
                        is com.technitedminds.wallet.domain.model.CardType.Custom ->
                                "Custom:${card.type.typeName}:${card.type.colorHex}"
                    }
            )
            put("categoryId", card.categoryId)
            put("frontImagePath", card.frontImagePath)
            put("backImagePath", card.backImagePath)
            put("extractedData", JSONObject(card.extractedData))
            put("customFields", JSONObject(card.customFields))
            put("createdAt", card.createdAt)
            put("updatedAt", card.updatedAt)
        }
    }

    private fun jsonToCard(json: JSONObject): Card {
        val typeString = json.getString("type")
        val cardType =
                when {
                    typeString == "Credit" -> com.technitedminds.wallet.domain.model.CardType.Credit
                    typeString == "Debit" -> com.technitedminds.wallet.domain.model.CardType.Debit
                    typeString == "ATM" -> com.technitedminds.wallet.domain.model.CardType.ATM
                    typeString == "GiftCard" -> com.technitedminds.wallet.domain.model.CardType.GiftCard
                    typeString == "LoyaltyCard" -> com.technitedminds.wallet.domain.model.CardType.LoyaltyCard
                    typeString == "MembershipCard" -> com.technitedminds.wallet.domain.model.CardType.MembershipCard
                    typeString == "InsuranceCard" -> com.technitedminds.wallet.domain.model.CardType.InsuranceCard
                    typeString == "IdentificationCard" -> com.technitedminds.wallet.domain.model.CardType.IdentificationCard
                    typeString == "TransportCard" -> com.technitedminds.wallet.domain.model.CardType.TransportCard
                    typeString == "BusinessCard" -> com.technitedminds.wallet.domain.model.CardType.BusinessCard
                    typeString == "LibraryCard" -> com.technitedminds.wallet.domain.model.CardType.LibraryCard
                    typeString == "HotelCard" -> com.technitedminds.wallet.domain.model.CardType.HotelCard
                    typeString == "StudentCard" -> com.technitedminds.wallet.domain.model.CardType.StudentCard
                    typeString == "AccessCard" -> com.technitedminds.wallet.domain.model.CardType.AccessCard
                    typeString.startsWith("Custom:") -> {
                        val parts = typeString.substringAfter("Custom:").split(":")
                        if (parts.size >= 2) {
                            com.technitedminds.wallet.domain.model.CardType.Custom(parts[0], parts[1])
                        } else {
                            com.technitedminds.wallet.domain.model.CardType.Custom(parts.getOrElse(0) { "Custom" }, "#757575")
                        }
                    }
                    // Legacy support for old ImageOnly format
                    typeString.startsWith("ImageOnly:") -> {
                        val typeName = typeString.substringAfter("ImageOnly:")
                        com.technitedminds.wallet.domain.model.CardType.Custom(typeName, "#757575")
                    }
                    else -> com.technitedminds.wallet.domain.model.CardType.Custom("Unknown", "#757575")
                }

        return Card(
                id = json.getString("id"),
                name = json.getString("name"),
                type = cardType,
                categoryId = json.getString("categoryId"),
                frontImagePath = json.getString("frontImagePath"),
                backImagePath = json.getString("backImagePath"),
                extractedData = jsonObjectToMap(json.getJSONObject("extractedData")),
                customFields = jsonObjectToMap(json.getJSONObject("customFields")),
                createdAt = json.getLong("createdAt"),
                updatedAt = json.getLong("updatedAt")
        )
    }

    private fun categoriesToJson(categories: List<Category>): String {
        val jsonArray = JSONArray()
        for (category in categories) {
            jsonArray.put(categoryToJson(category))
        }
        return jsonArray.toString()
    }

    private fun categoryToJson(category: Category): JSONObject {
        return JSONObject().apply {
            put("id", category.id)
            put("name", category.name)
            put("description", category.description ?: "")
            put("colorHex", category.colorHex)
            put("iconName", category.iconName ?: "")
            put("sortOrder", category.sortOrder)
            put("createdAt", category.createdAt)
            put("updatedAt", category.updatedAt)
        }
    }

    private fun jsonToCategory(json: JSONObject): Category {
        return Category(
                id = json.getString("id"),
                name = json.getString("name"),
                description = json.optString("description").takeIf { it.isNotEmpty() },
                colorHex = json.optString("colorHex", "#1976D2"),
                iconName = json.optString("iconName").takeIf { it.isNotEmpty() },
                sortOrder = json.optInt("sortOrder", 0),
                createdAt = json.getLong("createdAt"),
                updatedAt = json.getLong("updatedAt")
        )
    }

    private fun jsonObjectToMap(jsonObject: JSONObject): Map<String, String> {
        val map = mutableMapOf<String, String>()
        val keys = jsonObject.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            map[key] = jsonObject.getString(key)
        }
        return map
    }
}
