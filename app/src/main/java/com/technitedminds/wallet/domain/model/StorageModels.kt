package com.technitedminds.wallet.domain.model

/**
 * Storage statistics data class.
 * Pure Kotlin domain model with no Android dependencies.
 */
data class StorageStats(
    /** Total bytes used by the application */
    val totalUsedBytes: Long,
    
    /** Bytes used for card images */
    val imageStorageBytes: Long,
    
    /** Bytes used for thumbnails */
    val thumbnailStorageBytes: Long,
    
    /** Bytes used for cache */
    val cacheStorageBytes: Long,
    
    /** Bytes used for temporary files */
    val tempStorageBytes: Long,
    
    /** Bytes used for log files */
    val logStorageBytes: Long,
    
    /** Bytes used by the database */
    val databaseSizeBytes: Long,
    
    /** Total number of images stored */
    val totalImageCount: Int,
    
    /** Total number of thumbnails stored */
    val totalThumbnailCount: Int,
    
    /** Number of orphaned files (not referenced by any card) */
    val orphanedFileCount: Int,
    
    /** Number of expired cache files */
    val expiredCacheFiles: Int,
    
    /** Last cleanup timestamp */
    val lastCleanupTime: String?,
    
    /** Last database optimization timestamp */
    val lastDatabaseOptimization: String?
) {
    companion object {
        /** Creates an empty StorageStats instance */
        fun empty(): StorageStats = StorageStats(
            totalUsedBytes = 0,
            imageStorageBytes = 0,
            thumbnailStorageBytes = 0,
            cacheStorageBytes = 0,
            tempStorageBytes = 0,
            logStorageBytes = 0,
            databaseSizeBytes = 0,
            totalImageCount = 0,
            totalThumbnailCount = 0,
            orphanedFileCount = 0,
            expiredCacheFiles = 0,
            lastCleanupTime = null,
            lastDatabaseOptimization = null
        )
    }
}

/**
 * Cleanup result data class.
 * Pure Kotlin domain model with no Android dependencies.
 */
data class CleanupResult(
    /** Number of orphaned images removed */
    val orphanedImagesRemoved: Int,
    
    /** Number of cache files removed */
    val cacheFilesRemoved: Int,
    
    /** Number of expired cache files removed */
    val expiredCacheFilesRemoved: Int,
    
    /** Number of temporary files removed */
    val tempFilesRemoved: Int,
    
    /** Number of log files removed */
    val logFilesRemoved: Int,
    
    /** Total bytes freed by cleanup */
    val bytesFreed: Long,
    
    /** Whether database was optimized */
    val databaseOptimized: Boolean
) {
    /** Total number of files removed */
    val totalFilesRemoved: Int
        get() = orphanedImagesRemoved + cacheFilesRemoved + expiredCacheFilesRemoved + tempFilesRemoved + logFilesRemoved
    
    companion object {
        /** Creates an empty CleanupResult instance */
        fun empty(): CleanupResult = CleanupResult(
            orphanedImagesRemoved = 0,
            cacheFilesRemoved = 0,
            expiredCacheFilesRemoved = 0,
            tempFilesRemoved = 0,
            logFilesRemoved = 0,
            bytesFreed = 0,
            databaseOptimized = false
        )
    }
}
