package com.technitedminds.wallet.data.backup

import android.content.Context
import com.technitedminds.wallet.domain.model.backup.BackupManifest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Result of unpacking a backup ZIP into a temp directory on the device's cache.
 * Caller must invoke [cleanup] when finished — even on error — so we never leave
 * decrypted card data on disk.
 */
data class UnpackedBackup(
    val manifest: BackupManifest,
    /** Map of `images/<rel>` → absolute file in cache. Lookup uses keys from manifest. */
    val imageFiles: Map<String, File>,
    /** Root temp dir to delete on cleanup. */
    private val tempDir: File,
) {
    fun cleanup() {
        runCatching { tempDir.deleteRecursively() }
    }
}

/**
 * Packs a [BackupManifest] + image files into a streaming ZIP, and unpacks one back.
 *
 * Layout inside the ZIP:
 * ```
 * manifest.json
 * images/<file1>.jpg
 * images/<file2>.jpg
 * ...
 * ```
 *
 * Images are written using their original file names (the basename of their absolute
 * paths) and the manifest stores those names so restore can map them back to cards.
 */
@Singleton
class BackupPackager @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val json = Json {
        prettyPrint = false
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    /**
     * Builds an in-memory ZIP archive. Suitable for libraries up to a few hundred MB;
     * the ZIP is then streamed through AES-GCM in [BackupCrypto.writeEncryptedEnvelope].
     *
     * @param imagePaths absolute paths to image files to include. Files that don't
     * exist are silently skipped — manifest is updated to match what actually got in.
     */
    fun pack(manifest: BackupManifest, imagePaths: List<String>): ByteArray {
        val out = ByteArrayOutputStream()
        ZipOutputStream(out).use { zip ->
            val includedNames = mutableListOf<String>()
            val seenNames = HashSet<String>()
            val buffer = ByteArray(BUFFER_SIZE)

            imagePaths.forEach { absPath ->
                val file = File(absPath)
                if (!file.exists() || !file.isFile) return@forEach

                var entryName = file.name
                if (!seenNames.add(entryName)) {
                    // Duplicate basename across different absolute paths — make unique.
                    var i = 1
                    while (!seenNames.add("${file.nameWithoutExtension}_$i.${file.extension}")) {
                        i++
                    }
                    entryName = "${file.nameWithoutExtension}_$i.${file.extension}"
                }

                zip.putNextEntry(ZipEntry("${BackupManifest.IMAGES_DIR}/$entryName"))
                file.inputStream().use { input ->
                    while (true) {
                        val read = input.read(buffer)
                        if (read <= 0) break
                        zip.write(buffer, 0, read)
                    }
                }
                zip.closeEntry()
                includedNames += entryName
            }

            val finalManifest = manifest.copy(imageFiles = includedNames)
            zip.putNextEntry(ZipEntry(BackupManifest.MANIFEST_ENTRY_NAME))
            zip.write(json.encodeToString(finalManifest).toByteArray(Charsets.UTF_8))
            zip.closeEntry()
        }
        return out.toByteArray()
    }

    /**
     * Reads a ZIP payload, extracts files into a fresh subdirectory of [Context.cacheDir],
     * and parses the manifest.
     *
     * Caller MUST invoke [UnpackedBackup.cleanup] when finished.
     */
    fun unpack(zipBytes: ByteArray): UnpackedBackup {
        val tempDir = File(context.cacheDir, "backup_restore_${System.currentTimeMillis()}")
            .apply { mkdirs() }

        val imageFiles = mutableMapOf<String, File>()
        var manifestJson: String? = null

        try {
            ZipInputStream(zipBytes.inputStream()).use { zip ->
                val buffer = ByteArray(BUFFER_SIZE)
                while (true) {
                    val entry = zip.nextEntry ?: break
                    val name = entry.name
                    // Defend against zip-slip: never write outside tempDir.
                    if (name.contains("..") || name.startsWith("/")) {
                        zip.closeEntry()
                        continue
                    }

                    when {
                        name == BackupManifest.MANIFEST_ENTRY_NAME -> {
                            val out = ByteArrayOutputStream()
                            while (true) {
                                val read = zip.read(buffer)
                                if (read <= 0) break
                                out.write(buffer, 0, read)
                            }
                            manifestJson = out.toString(Charsets.UTF_8.name())
                        }
                        name.startsWith("${BackupManifest.IMAGES_DIR}/") -> {
                            val relName = name.removePrefix("${BackupManifest.IMAGES_DIR}/")
                            if (relName.isBlank() || entry.isDirectory) {
                                zip.closeEntry()
                                continue
                            }
                            val outFile = File(tempDir, relName).apply {
                                parentFile?.mkdirs()
                            }
                            outFile.outputStream().use { fileOut ->
                                while (true) {
                                    val read = zip.read(buffer)
                                    if (read <= 0) break
                                    fileOut.write(buffer, 0, read)
                                }
                            }
                            imageFiles[relName] = outFile
                        }
                    }
                    zip.closeEntry()
                }
            }

            val manifestText = manifestJson
                ?: throw IOException("Backup is missing manifest.json")
            val manifest = json.decodeFromString(BackupManifest.serializer(), manifestText)
            if (manifest.schema > BackupManifest.SCHEMA_VERSION) {
                throw IOException(
                    "This backup uses a newer manifest format (schema ${manifest.schema}). " +
                        "Please update CardVault and try again.",
                )
            }
            return UnpackedBackup(
                manifest = manifest,
                imageFiles = imageFiles,
                tempDir = tempDir,
            )
        } catch (t: Throwable) {
            runCatching { tempDir.deleteRecursively() }
            throw t
        }
    }

    private companion object {
        const val BUFFER_SIZE = 16 * 1024
    }
}
