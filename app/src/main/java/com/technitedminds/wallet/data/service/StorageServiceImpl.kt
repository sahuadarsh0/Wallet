package com.technitedminds.wallet.data.service

import com.technitedminds.wallet.data.local.storage.StorageManager
import com.technitedminds.wallet.domain.model.CleanupResult
import com.technitedminds.wallet.domain.model.StorageStats
import com.technitedminds.wallet.domain.service.StorageService
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of StorageService that wraps StorageManager.
 * Maps data layer models to domain models.
 */
@Singleton
class StorageServiceImpl @Inject constructor(
    private val storageManager: StorageManager
) : StorageService {

    override suspend fun performFullCleanup(): CleanupResult {
        val result = storageManager.performFullCleanup()
        return result.toDomainModel()
    }

    override suspend fun getStorageStatistics(): StorageStats {
        val stats = storageManager.getStorageStatistics()
        return stats.toDomainModel()
    }

    override fun isStorageSpaceLow(): Boolean {
        return storageManager.isStorageSpaceLow()
    }

    override fun getAvailableStorageSpace(): Long {
        return storageManager.getAvailableStorageSpace()
    }

    override suspend fun createTempFile(prefix: String, suffix: String): File? {
        return storageManager.createTempFile(prefix, suffix)
    }

    override suspend fun cleanupTempFile(file: File): Boolean {
        return storageManager.cleanupTempFile(file)
    }
}

/**
 * Extension function to map StorageManager.StorageStats to domain StorageStats.
 */
private fun StorageManager.StorageStats.toDomainModel(): StorageStats {
    return StorageStats(
        totalUsedBytes = this.totalUsedBytes,
        imageStorageBytes = this.imageStorageBytes,
        thumbnailStorageBytes = this.thumbnailStorageBytes,
        cacheStorageBytes = this.cacheStorageBytes,
        tempStorageBytes = this.tempStorageBytes,
        logStorageBytes = this.logStorageBytes,
        databaseSizeBytes = this.databaseSizeBytes,
        totalImageCount = this.totalImageCount,
        totalThumbnailCount = this.totalThumbnailCount,
        orphanedFileCount = this.orphanedFileCount,
        expiredCacheFiles = this.expiredCacheFiles,
        lastCleanupTime = this.lastCleanupTime,
        lastDatabaseOptimization = this.lastDatabaseOptimization
    )
}

/**
 * Extension function to map StorageManager.CleanupResult to domain CleanupResult.
 */
private fun StorageManager.CleanupResult.toDomainModel(): CleanupResult {
    return CleanupResult(
        orphanedImagesRemoved = this.orphanedImagesRemoved,
        cacheFilesRemoved = this.cacheFilesRemoved,
        expiredCacheFilesRemoved = this.expiredCacheFilesRemoved,
        tempFilesRemoved = this.tempFilesRemoved,
        logFilesRemoved = this.logFilesRemoved,
        bytesFreed = this.bytesFreed,
        databaseOptimized = this.databaseOptimized
    )
}
