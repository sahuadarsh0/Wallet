package com.technitedminds.wallet.data.local.storage

import android.content.Context
import androidx.room.withTransaction
import com.technitedminds.wallet.data.local.cache.CacheManager
import com.technitedminds.wallet.data.local.database.WalletDatabase
import com.technitedminds.wallet.data.local.files.ImageFileManager
import com.technitedminds.wallet.data.local.preferences.SimplePreferencesManager
import com.technitedminds.wallet.domain.usecase.card.GetCardsUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Comprehensive storage management service that handles cache cleanup, database optimization,
 * and maintenance routines for the CardVault application.
 */
@Singleton
class StorageManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: WalletDatabase,
    private val imageFileManager: ImageFileManager,
    private val cacheManager: CacheManager,
    private val preferencesManager: SimplePreferencesManager,
    private val getCardsUseCase: GetCardsUseCase
) {
    
    companion object {
        private const val CACHE_DIRECTORY = "cache"
        private const val TEMP_DIRECTORY = "temp"
        private const val LOGS_DIRECTORY = "logs"
        private const val MAX_CACHE_AGE_DAYS = 7
        private const val MAX_LOG_AGE_DAYS = 30
        private const val MAX_TEMP_FILE_AGE_HOURS = 24
        private const val DATABASE_VACUUM_THRESHOLD_DAYS = 30
    }
    
    private val cacheDir: File by lazy {
        File(context.cacheDir, CACHE_DIRECTORY).apply { if (!exists()) mkdirs() }
    }
    
    private val tempDir: File by lazy {
        File(context.filesDir, TEMP_DIRECTORY).apply { if (!exists()) mkdirs() }
    }
    
    private val logsDir: File by lazy {
        File(context.filesDir, LOGS_DIRECTORY).apply { if (!exists()) mkdirs() }
    }
    
    /**
     * Storage statistics data class
     */
    data class StorageStats(
        val totalUsedBytes: Long,
        val imageStorageBytes: Long,
        val thumbnailStorageBytes: Long,
        val cacheStorageBytes: Long,
        val tempStorageBytes: Long,
        val logStorageBytes: Long,
        val databaseSizeBytes: Long,
        val totalImageCount: Int,
        val totalThumbnailCount: Int,
        val orphanedFileCount: Int,
        val expiredCacheFiles: Int,
        val lastCleanupTime: String?,
        val lastDatabaseOptimization: String?
    )
    
    /**
     * Cleanup result data class
     */
    data class CleanupResult(
        val orphanedImagesRemoved: Int,
        val cacheFilesRemoved: Int,
        val expiredCacheFilesRemoved: Int,
        val tempFilesRemoved: Int,
        val logFilesRemoved: Int,
        val bytesFreed: Long,
        val databaseOptimized: Boolean
    )
    
    /**
     * Performs comprehensive storage cleanup
     */
    suspend fun performFullCleanup(): CleanupResult = withContext(Dispatchers.IO) {
        var totalBytesFreed = 0L
        var orphanedImagesRemoved = 0
        var cacheFilesRemoved = 0
        var expiredCacheFilesRemoved = 0
        var tempFilesRemoved = 0
        var logFilesRemoved = 0
        var databaseOptimized = false
        
        try {
            // Clean up orphaned images
            val cards = getCardsUseCase.getAllCards().first()
            val referencedPaths = cards.flatMap { card ->
                listOfNotNull(card.frontImagePath, card.backImagePath)
            }
            orphanedImagesRemoved = imageFileManager.cleanupOrphanedImages(referencedPaths)
            
            // Clean up cache files
            val cacheCleanup = cleanupCacheFiles()
            cacheFilesRemoved = cacheCleanup.first
            totalBytesFreed += cacheCleanup.second
            
            // Clean up expired cache files
            expiredCacheFilesRemoved = cacheManager.clearExpiredCache()
            
            // Clean up temporary files
            val tempCleanup = cleanupTempFiles()
            tempFilesRemoved = tempCleanup.first
            totalBytesFreed += tempCleanup.second
            
            // Clean up old log files
            val logCleanup = cleanupLogFiles()
            logFilesRemoved = logCleanup.first
            totalBytesFreed += logCleanup.second
            
            // Optimize database if needed
            databaseOptimized = optimizeDatabaseIfNeeded()
            
            // Update cleanup timestamp
            val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(Date())
            preferencesManager.setStorageCleanupLastRun(currentTime)
            
        } catch (e: Exception) {
            // Log error but don't throw to allow partial cleanup results
        }
        
        CleanupResult(
            orphanedImagesRemoved = orphanedImagesRemoved,
            cacheFilesRemoved = cacheFilesRemoved,
            expiredCacheFilesRemoved = expiredCacheFilesRemoved,
            tempFilesRemoved = tempFilesRemoved,
            logFilesRemoved = logFilesRemoved,
            bytesFreed = totalBytesFreed,
            databaseOptimized = databaseOptimized
        )
    }
    
    /**
     * Gets comprehensive storage statistics
     */
    suspend fun getStorageStatistics(): StorageStats = withContext(Dispatchers.IO) {
        try {
            val imageStats = imageFileManager.getStorageStats()
            val cacheStats = cacheManager.getCacheStatistics()
            val cacheSize = calculateDirectorySize(cacheDir)
            val tempSize = calculateDirectorySize(tempDir)
            val logSize = calculateDirectorySize(logsDir)
            val databaseSize = getDatabaseSize()
            val orphanedCount = countOrphanedFiles()
            
            val lastCleanup = preferencesManager.getStorageCleanupLastRun().first()
            val lastDbOptimization = getLastDatabaseOptimization()
            
            StorageStats(
                totalUsedBytes = imageStats["totalSize"] ?: 0L + cacheStats.totalSizeBytes + tempSize + logSize + databaseSize,
                imageStorageBytes = imageStats["imageSize"] ?: 0L,
                thumbnailStorageBytes = imageStats["thumbnailSize"] ?: 0L,
                cacheStorageBytes = cacheStats.totalSizeBytes,
                tempStorageBytes = tempSize,
                logStorageBytes = logSize,
                databaseSizeBytes = databaseSize,
                totalImageCount = (imageStats["imageCount"] ?: 0L).toInt(),
                totalThumbnailCount = (imageStats["thumbnailCount"] ?: 0L).toInt(),
                orphanedFileCount = orphanedCount,
                expiredCacheFiles = cacheStats.expiredFiles,
                lastCleanupTime = lastCleanup,
                lastDatabaseOptimization = lastDbOptimization
            )
        } catch (e: Exception) {
            // Return empty stats on error
            StorageStats(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, null, null)
        }
    }
    
    /**
     * Cleans up cache files older than MAX_CACHE_AGE_DAYS
     */
    private suspend fun cleanupCacheFiles(): Pair<Int, Long> = withContext(Dispatchers.IO) {
        var filesRemoved = 0
        var bytesFreed = 0L
        
        try {
            val cutoffTime = System.currentTimeMillis() - (MAX_CACHE_AGE_DAYS * 24 * 60 * 60 * 1000L)
            
            cacheDir.listFiles()?.forEach { file ->
                if (file.isFile && file.lastModified() < cutoffTime) {
                    val fileSize = file.length()
                    if (file.delete()) {
                        filesRemoved++
                        bytesFreed += fileSize
                    }
                }
            }
        } catch (e: Exception) {
            // Log error but continue
        }
        
        Pair(filesRemoved, bytesFreed)
    }
    
    /**
     * Cleans up temporary files older than MAX_TEMP_FILE_AGE_HOURS
     */
    private suspend fun cleanupTempFiles(): Pair<Int, Long> = withContext(Dispatchers.IO) {
        var filesRemoved = 0
        var bytesFreed = 0L
        
        try {
            val cutoffTime = System.currentTimeMillis() - (MAX_TEMP_FILE_AGE_HOURS * 60 * 60 * 1000L)
            
            tempDir.listFiles()?.forEach { file ->
                if (file.isFile && file.lastModified() < cutoffTime) {
                    val fileSize = file.length()
                    if (file.delete()) {
                        filesRemoved++
                        bytesFreed += fileSize
                    }
                }
            }
        } catch (e: Exception) {
            // Log error but continue
        }
        
        Pair(filesRemoved, bytesFreed)
    }
    
    /**
     * Cleans up log files older than MAX_LOG_AGE_DAYS
     */
    private suspend fun cleanupLogFiles(): Pair<Int, Long> = withContext(Dispatchers.IO) {
        var filesRemoved = 0
        var bytesFreed = 0L
        
        try {
            val cutoffTime = System.currentTimeMillis() - (MAX_LOG_AGE_DAYS * 24 * 60 * 60 * 1000L)
            
            logsDir.listFiles()?.forEach { file ->
                if (file.isFile && file.lastModified() < cutoffTime) {
                    val fileSize = file.length()
                    if (file.delete()) {
                        filesRemoved++
                        bytesFreed += fileSize
                    }
                }
            }
        } catch (e: Exception) {
            // Log error but continue
        }
        
        Pair(filesRemoved, bytesFreed)
    }
    
    /**
     * Optimizes database if it hasn't been optimized recently
     */
    private suspend fun optimizeDatabaseIfNeeded(): Boolean = withContext(Dispatchers.IO) {
        try {
            val lastOptimization = getLastDatabaseOptimization()
            val shouldOptimize = if (lastOptimization != null) {
                val lastOptimizationTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .parse(lastOptimization)?.time ?: 0L
                val cutoffTime = System.currentTimeMillis() - (DATABASE_VACUUM_THRESHOLD_DAYS * 24 * 60 * 60 * 1000L)
                lastOptimizationTime < cutoffTime
            } else {
                true // Never optimized before
            }
            
            if (shouldOptimize) {
                database.withTransaction {
                    // Perform VACUUM to reclaim space and optimize database
                    database.openHelper.writableDatabase.execSQL("VACUUM")
                    
                    // Analyze tables to update query planner statistics
                    database.openHelper.writableDatabase.execSQL("ANALYZE")
                    
                    // Reindex all tables
                    database.openHelper.writableDatabase.execSQL("REINDEX")
                }
                
                // Update optimization timestamp
                val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .format(Date())
                saveLastDatabaseOptimization(currentTime)
                
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Calculates the total size of a directory
     */
    private fun calculateDirectorySize(directory: File): Long {
        var size = 0L
        try {
            directory.listFiles()?.forEach { file ->
                size += if (file.isDirectory) {
                    calculateDirectorySize(file)
                } else {
                    file.length()
                }
            }
        } catch (e: Exception) {
            // Return 0 on error
        }
        return size
    }
    
    /**
     * Gets the size of the database file
     */
    private fun getDatabaseSize(): Long {
        return try {
            val dbFile = context.getDatabasePath(WalletDatabase.DATABASE_NAME)
            if (dbFile.exists()) dbFile.length() else 0L
        } catch (e: Exception) {
            0L
        }
    }
    
    /**
     * Counts orphaned files that are not referenced by any card
     */
    private suspend fun countOrphanedFiles(): Int = withContext(Dispatchers.IO) {
        try {
            val cards = getCardsUseCase.getAllCards().first()
            val referencedPaths = cards.flatMap { card ->
                listOfNotNull(card.frontImagePath, card.backImagePath)
            }.map { File(it).name }.toSet()
            
            val imageFiles = File(context.filesDir, "card_images").listFiles() ?: emptyArray()
            imageFiles.count { it.name !in referencedPaths }
        } catch (e: Exception) {
            0
        }
    }
    
    /**
     * Gets the last database optimization timestamp
     */
    private fun getLastDatabaseOptimization(): String? {
        return try {
            val prefs = context.getSharedPreferences("storage_prefs", Context.MODE_PRIVATE)
            prefs.getString("last_db_optimization", null)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Saves the last database optimization timestamp
     */
    private fun saveLastDatabaseOptimization(timestamp: String) {
        try {
            val prefs = context.getSharedPreferences("storage_prefs", Context.MODE_PRIVATE)
            prefs.edit().putString("last_db_optimization", timestamp).apply()
        } catch (e: Exception) {
            // Ignore errors
        }
    }
    
    /**
     * Creates a temporary file for processing
     */
    suspend fun createTempFile(prefix: String, suffix: String): File? = withContext(Dispatchers.IO) {
        try {
            File.createTempFile(prefix, suffix, tempDir)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Cleans up a specific temporary file
     */
    suspend fun cleanupTempFile(file: File): Boolean = withContext(Dispatchers.IO) {
        try {
            file.delete()
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Gets available storage space
     */
    fun getAvailableStorageSpace(): Long {
        return try {
            context.filesDir.freeSpace
        } catch (e: Exception) {
            0L
        }
    }
    
    /**
     * Checks if storage space is running low
     */
    fun isStorageSpaceLow(): Boolean {
        val availableSpace = getAvailableStorageSpace()
        val totalSpace = context.filesDir.totalSpace
        val usedPercentage = ((totalSpace - availableSpace).toDouble() / totalSpace.toDouble()) * 100
        return usedPercentage > 90.0 // Consider low if more than 90% used
    }
}