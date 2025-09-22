package com.technitedminds.wallet.data.local.files

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.technitedminds.wallet.domain.model.ImageType
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Manages image file operations in the app's sandboxed storage. Handles image compression,
 * optimization, and file management.
 */
@Singleton
class ImageFileManager @Inject constructor(@ApplicationContext private val context: Context) {

    companion object {
        private const val IMAGES_DIRECTORY = "card_images"
        private const val THUMBNAILS_DIRECTORY = "thumbnails"
        private const val MAX_IMAGE_WIDTH = 1920
        private const val MAX_IMAGE_HEIGHT = 1080
        private const val THUMBNAIL_SIZE = 200
        private const val JPEG_QUALITY = 85
        private const val THUMBNAIL_QUALITY = 70
    }

    private val imagesDir: File by lazy {
        File(context.filesDir, IMAGES_DIRECTORY).apply { if (!exists()) mkdirs() }
    }

    private val thumbnailsDir: File by lazy {
        File(context.filesDir, THUMBNAILS_DIRECTORY).apply { if (!exists()) mkdirs() }
    }

    /** Saves an image to the app's private storage with compression and optimization */
    suspend fun saveImage(imageData: ByteArray, fileName: String, imageType: ImageType): String =
            withContext(Dispatchers.IO) {
                try {
                    // Create filename with proper extension
                    val fullFileName = "${fileName}_${imageType.name.lowercase()}.jpg"
                    val imageFile = File(imagesDir, fullFileName)

                    // Decode and optimize the image
                    val optimizedImageData = optimizeImage(imageData)

                    // Save to file
                    FileOutputStream(imageFile).use { outputStream ->
                        outputStream.write(optimizedImageData)
                        outputStream.flush()
                    }

                    // Create thumbnail
                    createThumbnail(imageFile, fileName, imageType)

                    imageFile.absolutePath
                } catch (e: Exception) {
                    throw IOException("Failed to save image: ${e.message}", e)
                }
            }

    /** Retrieves an image as byte array */
    suspend fun getImage(imagePath: String): ByteArray? =
            withContext(Dispatchers.IO) {
                try {
                    val file = File(imagePath)
                    if (!file.exists()) return@withContext null

                    FileInputStream(file).use { inputStream -> inputStream.readBytes() }
                } catch (e: Exception) {
                    null
                }
            }

    /** Gets an image file */
    suspend fun getImageFile(imagePath: String): File? =
            withContext(Dispatchers.IO) {
                val file = File(imagePath)
                if (file.exists()) file else null
            }

    /** Deletes an image from storage */
    suspend fun deleteImage(imagePath: String): Boolean =
            withContext(Dispatchers.IO) {
                try {
                    val file = File(imagePath)
                    val deleted = file.delete()

                    // Also delete thumbnail if it exists
                    val fileName = file.nameWithoutExtension
                    val thumbnailFile = File(thumbnailsDir, "${fileName}_thumbnail.jpg")
                    thumbnailFile.delete()

                    deleted
                } catch (e: Exception) {
                    false
                }
            }

    /** Deletes multiple images from storage */
    suspend fun deleteImages(imagePaths: List<String>): Int =
            withContext(Dispatchers.IO) {
                var deletedCount = 0
                imagePaths.forEach { path ->
                    if (deleteImage(path)) {
                        deletedCount++
                    }
                }
                deletedCount
            }

    /** Checks if an image file exists */
    suspend fun imageExists(imagePath: String): Boolean =
            withContext(Dispatchers.IO) { File(imagePath).exists() }

    /** Gets the size of an image file in bytes */
    suspend fun getImageSize(imagePath: String): Long =
            withContext(Dispatchers.IO) {
                try {
                    val file = File(imagePath)
                    if (file.exists()) file.length() else -1L
                } catch (e: Exception) {
                    -1L
                }
            }

    /** Compresses an image to reduce file size */
    suspend fun compressImage(imageData: ByteArray, quality: Int = JPEG_QUALITY): ByteArray =
            withContext(Dispatchers.IO) {
                try {
                    val bitmap =
                            BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
                                    ?: throw IllegalArgumentException("Invalid image data")

                    val outputStream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
                    bitmap.recycle()

                    outputStream.toByteArray()
                } catch (e: Exception) {
                    throw IOException("Failed to compress image: ${e.message}", e)
                }
            }

    /** Resizes an image to specified dimensions */
    suspend fun resizeImage(imageData: ByteArray, maxWidth: Int, maxHeight: Int): ByteArray =
            withContext(Dispatchers.IO) {
                try {
                    val bitmap =
                            BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
                                    ?: throw IllegalArgumentException("Invalid image data")

                    val resizedBitmap = resizeBitmap(bitmap, maxWidth, maxHeight)

                    val outputStream = ByteArrayOutputStream()
                    resizedBitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, outputStream)

                    bitmap.recycle()
                    resizedBitmap.recycle()

                    outputStream.toByteArray()
                } catch (e: Exception) {
                    throw IOException("Failed to resize image: ${e.message}", e)
                }
            }

    /** Gets image dimensions without loading the full image */
    suspend fun getImageDimensions(imagePath: String): Pair<Int, Int>? =
            withContext(Dispatchers.IO) {
                try {
                    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    BitmapFactory.decodeFile(imagePath, options)

                    if (options.outWidth > 0 && options.outHeight > 0) {
                        Pair(options.outWidth, options.outHeight)
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    null
                }
            }

    /** Creates a thumbnail version of an image */
    suspend fun createThumbnail(imagePath: String, thumbnailSize: Int = THUMBNAIL_SIZE): String? =
            withContext(Dispatchers.IO) {
                try {
                    val originalFile = File(imagePath)
                    if (!originalFile.exists()) return@withContext null

                    createThumbnail(
                            originalFile,
                            originalFile.nameWithoutExtension,
                            null,
                            thumbnailSize
                    )
                } catch (e: Exception) {
                    null
                }
            }

    /** Cleans up orphaned image files that are not referenced by any card */
    suspend fun cleanupOrphanedImages(referencedPaths: List<String>): Int =
            withContext(Dispatchers.IO) {
                var cleanedCount = 0

                try {
                    // Get all image files
                    val allImageFiles = imagesDir.listFiles() ?: return@withContext 0
                    val allThumbnailFiles = thumbnailsDir.listFiles() ?: emptyArray()

                    // Find orphaned images
                    val referencedFileNames = referencedPaths.map { File(it).name }.toSet()

                    allImageFiles.forEach { file ->
                        if (file.name !in referencedFileNames) {
                            if (file.delete()) {
                                cleanedCount++
                            }
                        }
                    }

                    // Clean up orphaned thumbnails
                    allThumbnailFiles.forEach { file ->
                        val originalName = file.name.replace("_thumbnail.jpg", ".jpg")
                        if (originalName !in referencedFileNames) {
                            file.delete()
                        }
                    }
                } catch (e: Exception) {
                    // Log error but don't throw
                }

                cleanedCount
            }

    /** Gets the total storage used by all images */
    suspend fun getTotalStorageUsed(): Long =
            withContext(Dispatchers.IO) {
                try {
                    val imageFiles = imagesDir.listFiles() ?: return@withContext 0L
                    val thumbnailFiles = thumbnailsDir.listFiles() ?: emptyArray()

                    val imageSize = imageFiles.sumOf { it.length() }
                    val thumbnailSize = thumbnailFiles.sumOf { it.length() }

                    imageSize + thumbnailSize
                } catch (e: Exception) {
                    0L
                }
            }

    /** Gets storage usage statistics */
    suspend fun getStorageStats(): Map<String, Long> =
            withContext(Dispatchers.IO) {
                try {
                    val imageFiles = imagesDir.listFiles() ?: emptyArray()
                    val thumbnailFiles = thumbnailsDir.listFiles() ?: emptyArray()

                    val imageSize = imageFiles.sumOf { it.length() }
                    val thumbnailSize = thumbnailFiles.sumOf { it.length() }

                    mapOf(
                            "totalSize" to (imageSize + thumbnailSize),
                            "imageSize" to imageSize,
                            "thumbnailSize" to thumbnailSize,
                            "imageCount" to imageFiles.size.toLong(),
                            "thumbnailCount" to thumbnailFiles.size.toLong()
                    )
                } catch (e: Exception) {
                    emptyMap()
                }
            }

    /** Validates image data format and integrity */
    suspend fun validateImageData(imageData: ByteArray): Boolean =
            withContext(Dispatchers.IO) {
                try {
                    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    BitmapFactory.decodeByteArray(imageData, 0, imageData.size, options)

                    options.outWidth > 0 && options.outHeight > 0
                } catch (e: Exception) {
                    false
                }
            }

    /** Gets the MIME type of an image */
    suspend fun getImageMimeType(imageData: ByteArray): String =
            withContext(Dispatchers.IO) {
                try {
                    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    BitmapFactory.decodeByteArray(imageData, 0, imageData.size, options)

                    options.outMimeType ?: "image/jpeg"
                } catch (e: Exception) {
                    "image/jpeg"
                }
            }

    /** Optimizes an image by resizing and compressing */
    private suspend fun optimizeImage(imageData: ByteArray): ByteArray =
            withContext(Dispatchers.IO) {
                try {
                    var bitmap =
                            BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
                                    ?: throw IllegalArgumentException("Invalid image data")

                    // Resize if too large
                    if (bitmap.width > MAX_IMAGE_WIDTH || bitmap.height > MAX_IMAGE_HEIGHT) {
                        val resizedBitmap = resizeBitmap(bitmap, MAX_IMAGE_WIDTH, MAX_IMAGE_HEIGHT)
                        bitmap.recycle()
                        bitmap = resizedBitmap
                    }

                    // Compress to JPEG
                    val outputStream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, outputStream)
                    bitmap.recycle()

                    outputStream.toByteArray()
                } catch (e: Exception) {
                    throw IOException("Failed to optimize image: ${e.message}", e)
                }
            }

    /** Creates a thumbnail for an image */
    private suspend fun createThumbnail(
            imageFile: File,
            baseName: String,
            imageType: ImageType?,
            thumbnailSize: Int = THUMBNAIL_SIZE
    ): String? =
            withContext(Dispatchers.IO) {
                try {
                    val bitmap =
                            BitmapFactory.decodeFile(imageFile.absolutePath)
                                    ?: return@withContext null

                    val thumbnailBitmap = createSquareThumbnail(bitmap, thumbnailSize)

                    val thumbnailFileName =
                            if (imageType != null) {
                                "${baseName}_${imageType.name.lowercase()}_thumbnail.jpg"
                            } else {
                                "${baseName}_thumbnail.jpg"
                            }

                    val thumbnailFile = File(thumbnailsDir, thumbnailFileName)

                    FileOutputStream(thumbnailFile).use { outputStream ->
                        thumbnailBitmap.compress(
                                Bitmap.CompressFormat.JPEG,
                                THUMBNAIL_QUALITY,
                                outputStream
                        )
                    }

                    bitmap.recycle()
                    thumbnailBitmap.recycle()

                    thumbnailFile.absolutePath
                } catch (e: Exception) {
                    null
                }
            }

    /** Resizes a bitmap maintaining aspect ratio */
    private fun resizeBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        val aspectRatio = width.toFloat() / height.toFloat()

        val (newWidth, newHeight) =
                if (aspectRatio > 1) {
                    // Landscape
                    val w = minOf(maxWidth, width)
                    val h = (w / aspectRatio).toInt()
                    w to h
                } else {
                    // Portrait or square
                    val h = minOf(maxHeight, height)
                    val w = (h * aspectRatio).toInt()
                    w to h
                }

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    /** Creates a square thumbnail from a bitmap */
    private fun createSquareThumbnail(bitmap: Bitmap, size: Int): Bitmap {
        val dimension = minOf(bitmap.width, bitmap.height)
        val x = (bitmap.width - dimension) / 2
        val y = (bitmap.height - dimension) / 2

        val squareBitmap = Bitmap.createBitmap(bitmap, x, y, dimension, dimension)
        return Bitmap.createScaledBitmap(squareBitmap, size, size, true)
    }
}
