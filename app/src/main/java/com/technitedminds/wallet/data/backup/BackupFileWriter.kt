package com.technitedminds.wallet.data.backup

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Where the export landed plus what to show the user.
 */
data class BackupWriteOutcome(
    val uri: Uri,
    val displayName: String,
    val sizeBytes: Long,
)

/**
 * Writes a `.wallet` file to `Downloads/CardVault/` via [MediaStore.Downloads].
 *
 * On API 29+ this requires no permissions: MediaStore lets the app insert files
 * into its own scoped section of `Downloads` using `RELATIVE_PATH` + `IS_PENDING`.
 * The resulting `content://` URI can be passed to `ACTION_SEND` so the user can
 * forward the file to Drive/email/USB themselves — the app stays fully offline.
 */
@Singleton
class BackupFileWriter @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    /**
     * @param produce callback that streams encrypted bytes into the provided OutputStream.
     * Throwing inside the callback aborts the write and removes the partial MediaStore
     * record so we never leave a corrupt file behind.
     */
    suspend fun writeToDownloads(produce: (java.io.OutputStream) -> Unit): BackupWriteOutcome =
        withContext(Dispatchers.IO) {
            val displayName = generateFileName()
            val resolver = context.contentResolver

            val collection: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Files.getContentUri("external")
            }

            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, displayName)
                put(MediaStore.Downloads.MIME_TYPE, MIME_TYPE)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(
                        MediaStore.Downloads.RELATIVE_PATH,
                        "${Environment.DIRECTORY_DOWNLOADS}/CardVault",
                    )
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }
            }

            val uri = resolver.insert(collection, values)
                ?: throw IOException("Could not allocate Downloads/CardVault entry.")

            try {
                resolver.openOutputStream(uri, "w")?.use { out ->
                    produce(out)
                    out.flush()
                } ?: throw IOException("Could not open output stream for $uri")

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val finalize = ContentValues().apply {
                        put(MediaStore.Downloads.IS_PENDING, 0)
                    }
                    resolver.update(uri, finalize, null, null)
                }

                val size = sizeOf(uri)
                BackupWriteOutcome(uri = uri, displayName = displayName, sizeBytes = size)
            } catch (t: Throwable) {
                runCatching { resolver.delete(uri, null, null) }
                throw t
            }
        }

    private fun sizeOf(uri: Uri): Long {
        return runCatching {
            context.contentResolver.openFileDescriptor(uri, "r")?.use { it.statSize }
        }.getOrNull() ?: 0L
    }

    private fun generateFileName(): String {
        val ts = SimpleDateFormat("yyyyMMdd-HHmm", Locale.US).format(Date())
        return "cardvault-backup-$ts$FILE_EXTENSION"
    }

    companion object {
        const val FILE_EXTENSION = ".wallet"
        const val MIME_TYPE = "application/octet-stream"
    }
}
