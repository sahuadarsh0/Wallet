package com.technitedminds.wallet.domain.usecase.storage

import com.technitedminds.wallet.domain.model.CleanupResult
import com.technitedminds.wallet.domain.model.StorageStats
import com.technitedminds.wallet.domain.service.StorageService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use case for storage management operations including cleanup, optimization, and statistics.
 * 
 * This use case follows Clean Architecture by depending on the StorageService interface
 * rather than a concrete implementation.
 */
@Singleton
class StorageManagementUseCase @Inject constructor(
    private val storageService: StorageService
) {
    
    /**
     * Performs comprehensive storage cleanup.
     * 
     * @return CleanupResult containing details of what was cleaned
     */
    suspend fun performStorageCleanup(): CleanupResult {
        return storageService.performFullCleanup()
    }
    
    /**
     * Gets storage statistics as a Flow.
     * 
     * @return Flow emitting current storage statistics
     */
    fun getStorageStatistics(): Flow<StorageStats> = flow {
        emit(storageService.getStorageStatistics())
    }
    
    /**
     * Checks if storage space is running low.
     * 
     * @return true if storage is critically low
     */
    fun isStorageSpaceLow(): Boolean {
        return storageService.isStorageSpaceLow()
    }
    
    /**
     * Gets available storage space in bytes.
     * 
     * @return Available storage space in bytes
     */
    fun getAvailableStorageSpace(): Long {
        return storageService.getAvailableStorageSpace()
    }
    
    /**
     * Creates a temporary file for processing.
     * 
     * @param prefix File name prefix
     * @param suffix File extension suffix
     * @return Created temporary file, or null if creation failed
     */
    suspend fun createTempFile(prefix: String, suffix: String): File? {
        return storageService.createTempFile(prefix, suffix)
    }
    
    /**
     * Cleans up a specific temporary file.
     * 
     * @param file The temporary file to delete
     * @return true if file was successfully deleted
     */
    suspend fun cleanupTempFile(file: File): Boolean {
        return storageService.cleanupTempFile(file)
    }
    
    /**
     * Formats bytes to human readable format.
     * Pure Kotlin implementation with no external dependencies.
     * 
     * @param bytes Number of bytes to format
     * @return Human readable string (e.g., "1.5 MB")
     */
    fun formatBytes(bytes: Long): String {
        val units = listOf("B", "KB", "MB", "GB", "TB")
        var size = bytes.toDouble()
        var unitIndex = 0
        
        while (size >= BYTES_PER_KB && unitIndex < units.size - 1) {
            size /= BYTES_PER_KB
            unitIndex++
        }
        
        return String.format("%.2f %s", size, units[unitIndex])
    }
    
    /**
     * Calculates storage usage percentage.
     * 
     * @param usedBytes Bytes currently used
     * @param totalBytes Total available bytes
     * @return Percentage as float (0.0 to 100.0)
     */
    fun calculateStorageUsagePercentage(usedBytes: Long, totalBytes: Long): Float {
        return if (totalBytes > 0) {
            (usedBytes.toFloat() / totalBytes.toFloat()) * 100f
        } else {
            0f
        }
    }
    
    companion object {
        /** Bytes per kilobyte for conversion */
        private const val BYTES_PER_KB = 1024.0
    }
}
