package com.technitedminds.wallet.data.repository

import com.technitedminds.wallet.data.local.files.ImageFileManager
import com.technitedminds.wallet.domain.model.CardImage
import com.technitedminds.wallet.domain.model.ImageType
import com.technitedminds.wallet.domain.repository.ImageRepository
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of ImageRepository using local file storage. Handles image persistence,
 * compression, and file management.
 */
@Singleton
class ImageRepositoryImpl @Inject constructor(private val imageFileManager: ImageFileManager) :
        ImageRepository {

    override suspend fun saveImage(
            imageData: ByteArray,
            fileName: String,
            imageType: ImageType
    ): String {
        return try {
            imageFileManager.saveImage(imageData, fileName, imageType)
        } catch (e: Exception) {
            throw Exception("Failed to save image: ${e.message}", e)
        }
    }

    override suspend fun saveImageWithMetadata(
            imageData: ByteArray,
            cardImage: CardImage
    ): CardImage {
        return try {
            val savedPath =
                    imageFileManager.saveImage(
                            imageData = imageData,
                            fileName = cardImage.id,
                            imageType = cardImage.imageType
                    )

            // Get actual file info
            val dimensions = imageFileManager.getImageDimensions(savedPath)
            val fileSize = imageFileManager.getImageSize(savedPath)
            val mimeType = imageFileManager.getImageMimeType(imageData)

            cardImage.copy(
                    filePath = savedPath,
                    fileSizeBytes = if (fileSize > 0) fileSize else cardImage.fileSizeBytes,
                    width = dimensions?.first ?: cardImage.width,
                    height = dimensions?.second ?: cardImage.height,
                    mimeType = mimeType
            )
        } catch (e: Exception) {
            throw Exception("Failed to save image with metadata: ${e.message}", e)
        }
    }

    override suspend fun getImage(imagePath: String): ByteArray? {
        return try {
            imageFileManager.getImage(imagePath)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getImageFile(imagePath: String): File? {
        return try {
            imageFileManager.getImageFile(imagePath)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun deleteImage(imagePath: String): Boolean {
        return try {
            imageFileManager.deleteImage(imagePath)
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun deleteImages(imagePaths: List<String>): Int {
        return try {
            imageFileManager.deleteImages(imagePaths)
        } catch (e: Exception) {
            0
        }
    }

    override suspend fun imageExists(imagePath: String): Boolean {
        return try {
            imageFileManager.imageExists(imagePath)
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getImageSize(imagePath: String): Long {
        return try {
            imageFileManager.getImageSize(imagePath)
        } catch (e: Exception) {
            -1L
        }
    }

    override suspend fun compressImage(imageData: ByteArray, quality: Int): ByteArray {
        return try {
            imageFileManager.compressImage(imageData, quality)
        } catch (e: Exception) {
            throw Exception("Failed to compress image: ${e.message}", e)
        }
    }

    override suspend fun resizeImage(
            imageData: ByteArray,
            maxWidth: Int,
            maxHeight: Int
    ): ByteArray {
        return try {
            imageFileManager.resizeImage(imageData, maxWidth, maxHeight)
        } catch (e: Exception) {
            throw Exception("Failed to resize image: ${e.message}", e)
        }
    }

    override suspend fun getImageDimensions(imagePath: String): Pair<Int, Int>? {
        return try {
            imageFileManager.getImageDimensions(imagePath)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun createThumbnail(imagePath: String, thumbnailSize: Int): String? {
        return try {
            imageFileManager.createThumbnail(imagePath, thumbnailSize)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun cleanupOrphanedImages(referencedPaths: List<String>): Int {
        return try {
            imageFileManager.cleanupOrphanedImages(referencedPaths)
        } catch (e: Exception) {
            0
        }
    }

    override suspend fun getTotalStorageUsed(): Long {
        return try {
            imageFileManager.getTotalStorageUsed()
        } catch (e: Exception) {
            0L
        }
    }

    override suspend fun getStorageStats(): Map<String, Long> {
        return try {
            imageFileManager.getStorageStats()
        } catch (e: Exception) {
            emptyMap()
        }
    }

    override suspend fun validateImageData(imageData: ByteArray): Boolean {
        return try {
            imageFileManager.validateImageData(imageData)
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getImageMimeType(imageData: ByteArray): String {
        return try {
            imageFileManager.getImageMimeType(imageData)
        } catch (e: Exception) {
            "image/jpeg"
        }
    }
}
