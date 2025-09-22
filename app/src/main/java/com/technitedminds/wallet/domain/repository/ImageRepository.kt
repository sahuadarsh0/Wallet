package com.technitedminds.wallet.domain.repository

import com.technitedminds.wallet.domain.model.CardImage
import com.technitedminds.wallet.domain.model.ImageType
import java.io.File

/**
 * Repository interface for image storage and retrieval operations. Handles card image files in the
 * app's private storage.
 */
interface ImageRepository {

    /**
     * Saves an image to the app's private storage
     * @param imageData The image data as byte array
     * @param fileName The desired file name (without extension)
     * @param imageType The type of image (front or back)
     * @return The file path where the image was saved
     */
    suspend fun saveImage(imageData: ByteArray, fileName: String, imageType: ImageType): String

    /**
     * Saves an image with metadata
     * @param imageData The image data as byte array
     * @param cardImage The CardImage metadata
     * @return The updated CardImage with actual file path
     */
    suspend fun saveImageWithMetadata(imageData: ByteArray, cardImage: CardImage): CardImage

    /**
     * Retrieves an image as byte array
     * @param imagePath The file path of the image
     * @return The image data as byte array, null if not found
     */
    suspend fun getImage(imagePath: String): ByteArray?

    /**
     * Retrieves an image file
     * @param imagePath The file path of the image
     * @return The File object, null if not found
     */
    suspend fun getImageFile(imagePath: String): File?

    /**
     * Deletes an image from storage
     * @param imagePath The file path of the image to delete
     * @return True if deletion was successful, false otherwise
     */
    suspend fun deleteImage(imagePath: String): Boolean

    /**
     * Deletes multiple images from storage
     * @param imagePaths List of file paths to delete
     * @return Number of successfully deleted images
     */
    suspend fun deleteImages(imagePaths: List<String>): Int

    /**
     * Checks if an image file exists
     * @param imagePath The file path to check
     * @return True if the image exists, false otherwise
     */
    suspend fun imageExists(imagePath: String): Boolean

    /**
     * Gets the size of an image file in bytes
     * @param imagePath The file path of the image
     * @return The file size in bytes, -1 if file doesn't exist
     */
    suspend fun getImageSize(imagePath: String): Long

    /**
     * Compresses an image to reduce file size
     * @param imageData The original image data
     * @param quality Compression quality (0-100)
     * @return The compressed image data
     */
    suspend fun compressImage(imageData: ByteArray, quality: Int = 85): ByteArray

    /**
     * Resizes an image to specified dimensions
     * @param imageData The original image data
     * @param maxWidth Maximum width in pixels
     * @param maxHeight Maximum height in pixels
     * @return The resized image data
     */
    suspend fun resizeImage(imageData: ByteArray, maxWidth: Int, maxHeight: Int): ByteArray

    /**
     * Gets image dimensions without loading the full image
     * @param imagePath The file path of the image
     * @return Pair of width and height, null if image doesn't exist
     */
    suspend fun getImageDimensions(imagePath: String): Pair<Int, Int>?

    /**
     * Creates a thumbnail version of an image
     * @param imagePath The file path of the original image
     * @param thumbnailSize The size of the thumbnail (square)
     * @return The file path of the created thumbnail
     */
    suspend fun createThumbnail(imagePath: String, thumbnailSize: Int = 200): String?

    /**
     * Cleans up orphaned image files that are not referenced by any card
     * @param referencedPaths List of image paths that are currently in use
     * @return Number of orphaned files cleaned up
     */
    suspend fun cleanupOrphanedImages(referencedPaths: List<String>): Int

    /**
     * Gets the total storage used by all images
     * @return Total storage used in bytes
     */
    suspend fun getTotalStorageUsed(): Long

    /**
     * Gets storage usage statistics
     * @return Map with storage statistics (totalSize, imageCount, etc.)
     */
    suspend fun getStorageStats(): Map<String, Long>

    /**
     * Validates image data format and integrity
     * @param imageData The image data to validate
     * @return True if the image data is valid, false otherwise
     */
    suspend fun validateImageData(imageData: ByteArray): Boolean

    /**
     * Gets the MIME type of an image
     * @param imageData The image data
     * @return The MIME type (e.g., "image/jpeg")
     */
    suspend fun getImageMimeType(imageData: ByteArray): String
}
