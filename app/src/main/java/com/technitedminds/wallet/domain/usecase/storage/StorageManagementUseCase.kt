package com.technitedminds.wallet.domain.usecase.storage

import com.technitedminds.wallet.data.local.storage.StorageManager
import com.technitedminds.wallet.presentation.constants.AppConstants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use case for storage management operations including cleanup, optimization, and statistics.
 */
@Singleton
class StorageManagementUseCase @Inject constructor(
    private val storageManager: StorageManager
) {
    
    /**
     * Performs comprehensive storage cleanup
     */
    suspend fun performStorageCleanup(): StorageManager.CleanupResult {
        return storageManager.performFullCleanup()
    }
    
    /**
     * Gets storage statistics as a Flow
     */
    fun getStorageStatistics(): Flow<StorageManager.StorageStats> = flow {
        emit(storageManager.getStorageStatistics())
    }
    
    /**
     * Checks if storage space is running low
     */
    fun isStorageSpaceLow(): Boolean {
        return storageManager.isStorageSpaceLow()
    }
    
    /**
     * Gets available storage space in bytes
     */
    fun getAvailableStorageSpace(): Long {
        return storageManager.getAvailableStorageSpace()
    }
    
    /**
     * Creates a temporary file for processing
     */
    suspend fun createTempFile(prefix: String, suffix: String): java.io.File? {
        return storageManager.createTempFile(prefix, suffix)
    }
    
    /**
     * Cleans up a specific temporary file
     */
    suspend fun cleanupTempFile(file: java.io.File): Boolean {
        return storageManager.cleanupTempFile(file)
    }
    
    /**
     * Formats bytes to human readable format
     */
    fun formatBytes(bytes: Long): String {
        val units = AppConstants.Storage.STORAGE_UNITS
        var size = bytes.toDouble()
        var unitIndex = 0
        
        while (size >= AppConstants.Storage.BYTES_PER_KB && unitIndex < units.size - 1) {
            size /= AppConstants.Storage.BYTES_PER_KB
            unitIndex++
        }
        
        return String.format("${AppConstants.Storage.DECIMAL_FORMAT_PATTERN} %s", size, units[unitIndex])
    }
    
    /**
     * Calculates storage usage percentage
     */
    fun calculateStorageUsagePercentage(usedBytes: Long, totalBytes: Long): Float {
        return if (totalBytes > 0) {
            (usedBytes.toFloat() / totalBytes.toFloat()) * AppConstants.Storage.STORAGE_PERCENTAGE_MULTIPLIER
        } else {
            0f
        }
    }
}