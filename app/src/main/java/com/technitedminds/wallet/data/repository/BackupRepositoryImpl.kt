package com.technitedminds.wallet.data.repository

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.technitedminds.wallet.BuildConfig
import com.technitedminds.wallet.data.backup.BackupCrypto
import com.technitedminds.wallet.data.backup.BackupFileReader
import com.technitedminds.wallet.data.backup.BackupFileWriter
import com.technitedminds.wallet.data.backup.BackupHeader
import com.technitedminds.wallet.data.backup.BackupPackager
import com.technitedminds.wallet.data.backup.UnpackedBackup
import com.technitedminds.wallet.data.local.database.WalletDatabase
import com.technitedminds.wallet.data.local.database.dao.CardDao
import com.technitedminds.wallet.data.local.database.dao.CategoryDao
import com.technitedminds.wallet.data.local.database.entities.CardEntity
import com.technitedminds.wallet.data.local.database.entities.CategoryEntity
import com.technitedminds.wallet.data.local.preferences.SimplePreferencesManager
import com.technitedminds.wallet.domain.model.CardGradient
import com.technitedminds.wallet.domain.model.CardType
import com.technitedminds.wallet.domain.model.GradientDirection
import com.technitedminds.wallet.domain.model.backup.BackupCard
import com.technitedminds.wallet.domain.model.backup.BackupCategory
import com.technitedminds.wallet.domain.model.backup.BackupFailureReason
import com.technitedminds.wallet.domain.model.backup.BackupGradient
import com.technitedminds.wallet.domain.model.backup.BackupManifest
import com.technitedminds.wallet.domain.model.backup.BackupResult
import com.technitedminds.wallet.domain.model.backup.BackupSettings
import com.technitedminds.wallet.domain.model.backup.RestoreConflictStrategy
import com.technitedminds.wallet.domain.model.backup.RestorePreview
import com.technitedminds.wallet.domain.model.backup.RestoreResult
import com.technitedminds.wallet.domain.repository.BackupProgress
import com.technitedminds.wallet.domain.repository.BackupRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Orchestrates the offline backup/restore lifecycle.
 *
 * **Export**:
 *   1. Read decrypted cards/categories/settings via the normal repository path
 *      (Tink decryption happens transparently in [com.technitedminds.wallet.data.local.database.converters.MapConverter]).
 *   2. Build a [BackupManifest] referencing image basenames.
 *   3. Pack into a ZIP, encrypt with AES-256-GCM (key = PBKDF2(passphrase)),
 *      stream the envelope into `Downloads/CardVault/`.
 *
 * **Restore**:
 *   1. Read bytes from the SAF [Uri].
 *   2. Decrypt envelope → ZIP → unpacked manifest + image temp files.
 *   3. Apply categories (insert if absent — Categories use unique-name index).
 *   4. Apply cards per [RestoreConflictStrategy] inside a Room transaction.
 *   5. Copy image files to `filesDir/card_images/` ONLY for cards that were inserted.
 *   6. Always clean up temp files in `finally`.
 */
@Singleton
class BackupRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: WalletDatabase,
    private val cardDao: CardDao,
    private val categoryDao: CategoryDao,
    private val preferencesManager: SimplePreferencesManager,
    private val crypto: BackupCrypto,
    private val packager: BackupPackager,
    private val writer: BackupFileWriter,
    private val reader: BackupFileReader,
) : BackupRepository {

    override suspend fun export(
        passphrase: CharArray,
        onProgress: (BackupProgress) -> Unit,
    ): BackupResult = withContext(Dispatchers.IO) {
        try {
            onProgress(BackupProgress.GATHERING_DATA)
            val cards = cardDao.getAllCards().first()
            val categories = categoryDao.getAllCategories().first()
            val settings = gatherSettings()
            val imagePaths = cards.flatMap {
                listOf(it.frontImagePath, it.backImagePath)
            }.filter { it.isNotBlank() && File(it).exists() }

            val manifest = BackupManifest(
                appVersion = BuildConfig.VERSION_NAME,
                createdAtEpochMs = System.currentTimeMillis(),
                cards = cards.map { it.toBackup() },
                categories = categories.map { it.toBackup() },
                settings = settings,
                imageFiles = emptyList(),
            )

            onProgress(BackupProgress.PACKAGING)
            val zipBytes = packager.pack(manifest, imagePaths)

            onProgress(BackupProgress.ENCRYPTING)
            val params = crypto.newPbkdf2Params()
            val header = BackupHeader(
                appVersion = BuildConfig.VERSION_NAME,
                createdAtEpochMs = manifest.createdAtEpochMs,
                pbkdf2 = params,
            )
            val key = crypto.deriveKey(passphrase, params)

            onProgress(BackupProgress.WRITING)
            val outcome = writer.writeToDownloads { out ->
                crypto.writeEncryptedEnvelope(out, header, key, zipBytes)
            }

            onProgress(BackupProgress.DONE)
            BackupResult.Success(
                fileUri = outcome.uri,
                displayName = outcome.displayName,
                sizeBytes = outcome.sizeBytes,
                cardCount = manifest.cards.size,
                categoryCount = manifest.categories.size,
                imageCount = imagePaths.size,
            )
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: IOException) {
            BackupResult.Failure(
                reason = if (e.message?.contains("space", ignoreCase = true) == true) {
                    BackupFailureReason.STORAGE_FULL
                } else {
                    BackupFailureReason.IO_ERROR
                },
                message = e.message,
            )
        } catch (e: Exception) {
            BackupResult.Failure(BackupFailureReason.UNKNOWN, e.message)
        } finally {
            passphrase.fill('\u0000')
        }
    }

    override suspend fun preview(uri: Uri, passphrase: CharArray): RestorePreview =
        withContext(Dispatchers.IO) {
            var unpacked: UnpackedBackup? = null
            try {
                val bytes = reader.readAllBytes(uri)
                val (_, zip) = crypto.decryptEnvelope(bytes.inputStream(), passphrase)
                unpacked = packager.unpack(zip)
                val manifest = unpacked.manifest
                val existingIds = collectExistingCardIds(manifest.cards.map { it.id })
                RestorePreview(
                    schema = manifest.schema,
                    sourceAppVersion = manifest.appVersion,
                    createdAtEpochMs = manifest.createdAtEpochMs,
                    cardCount = manifest.cards.size,
                    categoryCount = manifest.categories.size,
                    imageCount = unpacked.imageFiles.size,
                    conflictingCardCount = manifest.cards.count { existingIds.contains(it.id) },
                )
            } finally {
                unpacked?.cleanup()
                passphrase.fill('\u0000')
            }
        }

    override suspend fun restore(
        uri: Uri,
        passphrase: CharArray,
        strategy: RestoreConflictStrategy,
        onProgress: (BackupProgress) -> Unit,
    ): RestoreResult = withContext(Dispatchers.IO) {
        var unpacked: UnpackedBackup? = null
        try {
            onProgress(BackupProgress.READING)
            val bytes = reader.readAllBytes(uri)

            onProgress(BackupProgress.DECRYPTING)
            val (_, zip) = crypto.decryptEnvelope(bytes.inputStream(), passphrase)

            onProgress(BackupProgress.UNPACKING)
            unpacked = packager.unpack(zip)

            onProgress(BackupProgress.APPLYING)
            val applied = applyManifest(unpacked, strategy)

            onProgress(BackupProgress.DONE)
            applied
        } finally {
            unpacked?.cleanup()
            passphrase.fill('\u0000')
        }
    }

    private suspend fun applyManifest(
        unpacked: UnpackedBackup,
        strategy: RestoreConflictStrategy,
    ): RestoreResult {
        val manifest = unpacked.manifest

        // Plan category mapping outside the transaction so we can do name-based dedupe
        // (categories use a unique-name index — INSERT OR REPLACE on id would still
        // collide on name and lose the existing row).
        val existingCategories = categoryDao.getAllCategories().first()
        val byName = existingCategories.associateBy { it.name.trim().lowercase() }
        val byId = existingCategories.associateBy { it.id }
        val categoryIdRemap = mutableMapOf<String, String>()
        val categoriesToInsert = mutableListOf<CategoryEntity>()
        var categoriesAdded = 0
        var categoriesSkipped = 0

        manifest.categories.forEach { backupCategory ->
            val nameMatch = byName[backupCategory.name.trim().lowercase()]
            val idMatch = byId[backupCategory.id]
            when {
                idMatch != null -> {
                    categoryIdRemap[backupCategory.id] = idMatch.id
                    categoriesSkipped++
                }
                nameMatch != null -> {
                    categoryIdRemap[backupCategory.id] = nameMatch.id
                    categoriesSkipped++
                }
                else -> {
                    categoriesToInsert += backupCategory.toEntity()
                    categoryIdRemap[backupCategory.id] = backupCategory.id
                    categoriesAdded++
                }
            }
        }

        val cardImageMoves = mutableListOf<ImageMove>()
        var cardsAdded = 0
        var cardsSkipped = 0
        var cardsOverwritten = 0

        database.withTransaction {
            if (categoriesToInsert.isNotEmpty()) {
                categoryDao.insertCategories(categoriesToInsert)
            }

            manifest.cards.forEach { backupCard ->
                val mappedCategory = categoryIdRemap[backupCard.categoryId]
                    ?: DEFAULT_CATEGORY_ID
                val safeCategory = if (categoryDao.categoryExists(mappedCategory)) {
                    mappedCategory
                } else {
                    DEFAULT_CATEGORY_ID
                }

                val exists = cardDao.cardExists(backupCard.id)
                when (strategy) {
                    RestoreConflictStrategy.SKIP_EXISTING -> if (exists) {
                        cardsSkipped++
                        return@forEach
                    }
                    RestoreConflictStrategy.OVERWRITE_EXISTING -> {
                        // Insert with REPLACE strategy (built into CardDao.insertCard).
                    }
                    RestoreConflictStrategy.KEEP_BOTH -> Unit
                }

                val finalId = when {
                    strategy == RestoreConflictStrategy.KEEP_BOTH && exists ->
                        UUID.randomUUID().toString()
                    else -> backupCard.id
                }

                // Schedule image copies; we'll execute after the DB commits so we
                // never leave images on disk for a card that failed to insert.
                val frontPlan = planImage(unpacked, backupCard.frontImageRelPath, finalId, "front")
                val backPlan = planImage(unpacked, backupCard.backImageRelPath, finalId, "back")
                cardImageMoves += frontPlan.move ?: ImageMove.Noop
                cardImageMoves += backPlan.move ?: ImageMove.Noop

                val entity = backupCard.toEntity(
                    overrideId = finalId,
                    overrideCategoryId = safeCategory,
                    overrideFrontPath = frontPlan.targetAbsPath,
                    overrideBackPath = backPlan.targetAbsPath,
                )
                cardDao.insertCard(entity)

                if (strategy == RestoreConflictStrategy.OVERWRITE_EXISTING && exists) {
                    cardsOverwritten++
                } else {
                    cardsAdded++
                }
            }
        }

        // Copy images outside the transaction. Failures here are non-fatal: cards
        // without images render a placeholder and the next storage cleanup pass
        // collects orphans.
        val imagesRestored = executeImageMoves(cardImageMoves)

        applySettingsIfPresent(manifest.settings)

        return RestoreResult(
            cardsAdded = cardsAdded,
            cardsSkipped = cardsSkipped,
            cardsOverwritten = cardsOverwritten,
            categoriesAdded = categoriesAdded,
            categoriesSkipped = categoriesSkipped,
            imagesRestored = imagesRestored,
        )
    }

    // ── Image planning / move helpers ──────────────────────────────────────

    private data class ImagePlan(val targetAbsPath: String, val move: ImageMove?)

    private sealed interface ImageMove {
        data class Copy(val src: File, val dst: File) : ImageMove
        data object Noop : ImageMove
    }

    private fun planImage(
        unpacked: UnpackedBackup,
        relPath: String?,
        cardId: String,
        suffix: String,
    ): ImagePlan {
        if (relPath.isNullOrBlank()) return ImagePlan(targetAbsPath = "", move = null)
        val src = unpacked.imageFiles[relPath] ?: return ImagePlan(targetAbsPath = "", move = null)

        val imagesDir = File(context.filesDir, IMAGES_DIRECTORY).apply {
            if (!exists()) mkdirs()
        }
        val ext = src.extension.ifBlank { "jpg" }
        val targetName = "${cardId}_${suffix}.${ext}"
        val target = File(imagesDir, targetName)
        return ImagePlan(
            targetAbsPath = target.absolutePath,
            move = ImageMove.Copy(src = src, dst = target),
        )
    }

    private fun executeImageMoves(moves: List<ImageMove>): Int {
        var copied = 0
        moves.forEach { move ->
            if (move !is ImageMove.Copy) return@forEach
            try {
                move.src.inputStream().use { input ->
                    move.dst.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                copied++
            } catch (_: Exception) {
                // non-fatal — see comment in caller.
            }
        }
        return copied
    }

    // ── Settings / category helpers ────────────────────────────────────────

    private suspend fun gatherSettings(): BackupSettings {
        return BackupSettings(
            themeMode = preferencesManager.getThemeMode().first().name,
            folderTheme = preferencesManager.getFolderTheme().first(),
            backgroundPattern = preferencesManager.getBackgroundPattern().first(),
            cardFlipAnimationEnabled = preferencesManager.isCardFlipAnimationEnabled().first(),
            cameraFlashEnabled = preferencesManager.isCameraFlashEnabled().first(),
            preferredCardAspectRatio = preferencesManager.getPreferredCardAspectRatio().first(),
            defaultCardCategory = preferencesManager.getDefaultCardCategory().first(),
        )
    }

    private suspend fun applySettingsIfPresent(settings: BackupSettings) {
        runCatching {
            settings.themeMode?.let {
                runCatching {
                    val theme = enumValueOf<com.technitedminds.wallet.data.local.preferences.ThemeMode>(it)
                    preferencesManager.setThemeMode(theme)
                }
            }
            settings.folderTheme?.let { preferencesManager.setFolderTheme(it) }
            settings.backgroundPattern?.let { preferencesManager.setBackgroundPattern(it) }
            settings.cardFlipAnimationEnabled?.let {
                preferencesManager.setCardFlipAnimationEnabled(it)
            }
            settings.cameraFlashEnabled?.let { preferencesManager.setCameraFlashEnabled(it) }
            settings.preferredCardAspectRatio?.let {
                preferencesManager.setPreferredCardAspectRatio(it)
            }
            settings.defaultCardCategory?.let { preferencesManager.setDefaultCardCategory(it) }
        }
    }

    private suspend fun collectExistingCardIds(candidates: List<String>): Set<String> {
        val existing = mutableSetOf<String>()
        candidates.forEach { id ->
            if (cardDao.cardExists(id)) existing += id
        }
        return existing
    }

    companion object {
        private const val IMAGES_DIRECTORY = "card_images"
        private const val DEFAULT_CATEGORY_ID = "default"
    }
}

// ──────────────────────────────────────────────────────────────────────────
// Local mappers — kept in this file because they are an implementation detail
// of the backup format and should never be reused by the rest of the app.
// ──────────────────────────────────────────────────────────────────────────

private fun CardEntity.toBackup(): BackupCard {
    val (typeKey, customName, customColor) = when (val t = type) {
        is CardType.Custom -> Triple("Custom", t.typeName, t.colorHex)
        else -> Triple(t.getSerializableName(), null, null)
    }
    return BackupCard(
        id = id,
        name = name,
        typeKey = typeKey,
        customTypeName = customName,
        customTypeColorHex = customColor,
        categoryId = categoryId,
        frontImageRelPath = frontImagePath.takeIf { it.isNotBlank() }
            ?.let { File(it).name },
        backImageRelPath = backImagePath.takeIf { it.isNotBlank() }
            ?.let { File(it).name },
        extractedData = extractedData,
        customFields = customFields,
        expiryDate = expiryDate,
        notes = notes,
        customGradient = customGradient?.let {
            BackupGradient(
                startColor = it.startColor,
                endColor = it.endColor,
                direction = it.direction.name,
                name = it.name,
            )
        },
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

private fun CategoryEntity.toBackup(): BackupCategory = BackupCategory(
    id = id,
    name = name,
    description = description,
    colorHex = colorHex,
    iconName = iconName,
    sortOrder = sortOrder,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

private fun BackupCategory.toEntity(): CategoryEntity = CategoryEntity(
    id = id,
    name = name,
    description = description,
    colorHex = colorHex,
    iconName = iconName ?: "Category",
    sortOrder = sortOrder,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

private fun BackupCard.toEntity(
    overrideId: String,
    overrideCategoryId: String,
    overrideFrontPath: String,
    overrideBackPath: String,
): CardEntity {
    val type = when (typeKey) {
        "Custom" -> CardType.Custom(
            typeName = customTypeName ?: "Custom",
            colorHex = customTypeColorHex ?: "#757575",
        )
        else -> CardType.fromSerializableName(typeKey)
    }
    val gradient = customGradient?.let {
        val direction = runCatching { GradientDirection.valueOf(it.direction) }
            .getOrDefault(GradientDirection.TopToBottom)
        CardGradient(
            startColor = it.startColor,
            endColor = it.endColor,
            direction = direction,
            name = it.name,
        )
    }
    return CardEntity(
        id = overrideId,
        name = name,
        type = type,
        categoryId = overrideCategoryId,
        frontImagePath = overrideFrontPath,
        backImagePath = overrideBackPath,
        extractedData = extractedData,
        customFields = customFields,
        expiryDate = expiryDate,
        notes = notes,
        customGradient = gradient,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}
