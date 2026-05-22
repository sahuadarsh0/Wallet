package com.technitedminds.wallet.data.backup

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reads bytes from any `content://` URI returned by Storage Access Framework
 * (`ACTION_OPEN_DOCUMENT`). The picker grants us a one-shot read URI permission,
 * so this works for files in Downloads, Drive (the Drive provider mediates network),
 * email attachments, and any other DocumentsProvider — without us holding INTERNET.
 */
@Singleton
class BackupFileReader @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    /**
     * Reads the entire backup file into memory. Memory cap is enforced before any
     * decryption to prevent allocation attacks via crafted file headers.
     */
    suspend fun readAllBytes(uri: Uri, maxBytes: Long = MAX_BACKUP_BYTES): ByteArray =
        withContext(Dispatchers.IO) {
            val resolver = context.contentResolver
            val size = runCatching {
                resolver.openFileDescriptor(uri, "r")?.use { it.statSize }
            }.getOrNull() ?: -1L
            if (size in 1..maxBytes || size < 0) {
                resolver.openInputStream(uri)?.use { input ->
                    readBounded(input, maxBytes)
                } ?: throw IOException("Cannot open backup file at $uri")
            } else {
                throw IOException(
                    "Backup file is too large (${size / (1024 * 1024)} MB). " +
                        "Maximum supported is ${maxBytes / (1024 * 1024)} MB.",
                )
            }
        }

    private fun readBounded(input: InputStream, maxBytes: Long): ByteArray {
        val buffer = ByteArray(BUFFER_SIZE)
        val out = java.io.ByteArrayOutputStream()
        var total = 0L
        while (true) {
            val read = input.read(buffer)
            if (read <= 0) break
            total += read
            if (total > maxBytes) {
                throw IOException("Backup file exceeds maximum allowed size.")
            }
            out.write(buffer, 0, read)
        }
        return out.toByteArray()
    }

    companion object {
        private const val BUFFER_SIZE = 32 * 1024

        /** 512 MB — comfortably more than any realistic wallet of cards + images. */
        const val MAX_BACKUP_BYTES = 512L * 1024L * 1024L
    }
}
