package com.technitedminds.wallet.domain.model

/**
 * Data class representing image metadata for card images. Handles both front and back images with
 * their storage paths and metadata.
 */
data class CardImage(
        /** Unique identifier for the image */
        val id: String,

        /** File path where the image is stored in the app's private storage */
        val filePath: String,

        /** Type of image (front or back) */
        val imageType: ImageType,

        /** File size in bytes */
        val fileSizeBytes: Long,

        /** Image width in pixels */
        val width: Int,

        /** Image height in pixels */
        val height: Int,

        /** Timestamp when the image was captured/created */
        val createdAt: Long,

        /** MIME type of the image (e.g., "image/jpeg") */
        val mimeType: String = "image/jpeg"
) {
    /** Returns the aspect ratio of the image */
    fun getAspectRatio(): Float = if (height != 0) width.toFloat() / height.toFloat() else 1f

    /** Returns true if the image file exists and has valid dimensions */
    fun isValid(): Boolean = filePath.isNotBlank() && width > 0 && height > 0 && fileSizeBytes > 0
}

/** Enum representing the type of card image */
enum class ImageType {
    FRONT,
    BACK
}
