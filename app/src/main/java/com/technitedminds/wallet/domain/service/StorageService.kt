package com.technitedminds.wallet.domain.service

import com.technitedminds.wallet.domain.model.CleanupResult
import com.technitedminds.wallet.domain.model.StorageStats
import java.io.File

/**
 * Service interface for storage management operations.
 * Abstracts storage operations from the domain layer to maintain Clean Architecture.
 * 
 * Implementations of this interface should handle:
 * - Storage cleanup (orphaned files, cache, temp files)
 * - Database optimization
 * - Storage statistics
 * - Temporary file management
 */
interface StorageService {
    
    /**
     * Performs comprehensive storage cleanup.
     * This includes removing orphaned images, cleaning cache and temp files,
     * and optionally optimizing the database.
     * 
     * @return CleanupResult containing details of what was cleaned
     */
    suspend fun performFullCleanup(): CleanupResult
    
    /**
     * Gets comprehensive storage statistics.
     * 
     * @return StorageStats with detailed storage usage information
     */
    suspend fun getStorageStatistics(): StorageStats
    
    /**
     * Checks if storage space is running low.
     * 
     * @return true if storage space is critically low (e.g., >90% used)
     */
    fun isStorageSpaceLow(): Boolean
    
    /**
     * Gets available storage space in bytes.
     * 
     * @return Available storage space in bytes
     */
    fun getAvailableStorageSpace(): Long
    
    /**
     * Creates a temporary file for processing.
     * 
     * @param prefix File name prefix
     * @param suffix File extension suffix
     * @return Created temporary file, or null if creation failed
     */
    suspend fun createTempFile(prefix: String, suffix: String): File?
    
    /**
     * Cleans up a specific temporary file.
     * 
     * @param file The temporary file to delete
     * @return true if file was successfully deleted
     */
    suspend fun cleanupTempFile(file: File): Boolean
}
