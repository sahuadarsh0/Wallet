package com.technitedminds.wallet.data.local.cache

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages application cache including temporary files, processed images, and other cached data.
 */
@Singleton
class CacheManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private const val PROCESSED_IMAGES_CACHE = "processed_images"
        private const val OCR_RESULTS_CACHE = "ocr_results"
        private const val THUMBNAILS_CACHE = "thumbnails_cache"
        private const val MAX_CACHE_SIZE_MB = 50L
        private const val CACHE_EXPIRY_HOURS = 24
    }
    
    private val processedImagesCache: File by lazy {
        File(context.cacheDir, PROCESSED_IMAGES_CACHE).apply { if (!exists()) mkdirs() }
    }
    
    private val ocrResultsCache: File by lazy {
        File(context.cacheDir, OCR_RESULTS_CACHE).apply { if (!exists()) mkdirs() }
    }
    
    private val thumbnailsCache: File by lazy {
        File(context.cacheDir, THUMBNAILS_CACHE).apply { if (!exists()) mkdirs() }
    }
    
    /**
     * Cache statistics
     */
    data class CacheStats(
        val totalSizeBytes: Long,
        val processedImagesCacheSize: Long,
        val ocrResultsCacheSize: Long,
        val thumbnailsCacheSize: Long,
        val totalFiles: Int,
        val expiredFiles: Int
    )
    
    /**
     * Gets cache statistics
     */
    suspend fun getCacheStatistics(): CacheStats = withContext(Dispatchers.IO) {
        try {
            val processedImagesSize = calculateDirectorySize(processedImagesCache)
            val ocrResultsSize = calculateDirectorySize(ocrResultsCache)
            val thumbnailsSize = calculateDirectorySize(thumbnailsCache)
            val totalSize = processedImagesSize + ocrResultsSize + thumbnailsSize
            
            val totalFiles = countFiles(processedImagesCache) + 
                           countFiles(ocrResultsCache) + 
                           countFiles(thumbnailsCache)
            
            val expiredFiles = countExpiredFiles()
            
            CacheStats(
                totalSizeBytes = totalSize,
                processedImagesCacheSize = processedImagesSize,
                ocrResultsCacheSize = ocrResultsSize,
                thumbnailsCacheSize = thumbnailsSize,
                totalFiles = totalFiles,
                expiredFiles = expiredFiles
            )
        } catch (e: Exception) {
            CacheStats(0, 0, 0, 0, 0, 0)
        }
    }
    
    /**
     * Clears all cache
     */
    suspend fun clearAllCache(): Int = withContext(Dispatchers.IO) {
        var filesDeleted = 0
        
        try {
            filesDeleted += clearDirectory(processedImagesCache)
            filesDeleted += clearDirectory(ocrResultsCache)
            filesDeleted += clearDirectory(thumbnailsCache)
        } catch (e: Exception) {
            // Log error but continue
        }
        
        filesDeleted
    }
    
    /**
     * Clears expired cache files
     */
    suspend fun clearExpiredCache(): Int = withContext(Dispatchers.IO) {
        var filesDeleted = 0
        val cutoffTime = System.currentTimeMillis() - (CACHE_EXPIRY_HOURS * 60 * 60 * 1000L)
        
        try {
            filesDeleted += clearExpiredFiles(processedImagesCache, cutoffTime)
            filesDeleted += clearExpiredFiles(ocrResultsCache, cutoffTime)
            filesDeleted += clearExpiredFiles(thumbnailsCache, cutoffTime)
        } catch (e: Exception) {
            // Log error but continue
        }
        
        filesDeleted
    }
    
    /**
     * Manages cache size by removing oldest files if cache exceeds limit
     */
    suspend fun manageCacheSize(): Int = withContext(Dispatchers.IO) {
        var filesDeleted = 0
        val maxCacheSizeBytes = MAX_CACHE_SIZE_MB * 1024 * 1024
        
        try {
            val currentSize = getCacheStatistics().totalSizeBytes
            
            if (currentSize > maxCacheSizeBytes) {
                // Get all cache files sorted by last modified (oldest first)
                val allFiles = mutableListOf<File>()
                addFilesToList(processedImagesCache, allFiles)
                addFilesToList(ocrResultsCache, allFiles)
                addFilesToList(thumbnailsCache, allFiles)
                
                allFiles.sortBy { it.lastModified() }
                
                var sizeToDelete = currentSize - maxCacheSizeBytes
                var deletedSize = 0L
                
                for (file in allFiles) {
                    if (deletedSize >= sizeToDelete) break
                    
                    val fileSize = file.length()
                    if (file.delete()) {
                        filesDeleted++
                        deletedSize += fileSize
                    }
                }
            }
        } catch (e: Exception) {
            // Log error but continue
        }
        
        filesDeleted
    }
    
    /**
     * Caches a processed image
     */
    suspend fun cacheProcessedImage(key: String, imageData: ByteArray): Boolean = withContext(Dispatchers.IO) {
        try {
            val cacheFile = File(processedImagesCache, "$key.jpg")
            cacheFile.writeBytes(imageData)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Retrieves a cached processed image
     */
    suspend fun getCachedProcessedImage(key: String): ByteArray? = withContext(Dispatchers.IO) {
        try {
            val cacheFile = File(processedImagesCache, "$key.jpg")
            if (cacheFile.exists() && !isFileExpired(cacheFile)) {
                cacheFile.readBytes()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Caches OCR results
     */
    suspend fun cacheOcrResult(key: String, result: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val cacheFile = File(ocrResultsCache, "$key.txt")
            cacheFile.writeText(result)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Retrieves cached OCR results
     */
    suspend fun getCachedOcrResult(key: String): String? = withContext(Dispatchers.IO) {
        try {
            val cacheFile = File(ocrResultsCache, "$key.txt")
            if (cacheFile.exists() && !isFileExpired(cacheFile)) {
                cacheFile.readText()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Checks if cache is full
     */
    suspend fun isCacheFull(): Boolean = withContext(Dispatchers.IO) {
        val maxCacheSizeBytes = MAX_CACHE_SIZE_MB * 1024 * 1024
        getCacheStatistics().totalSizeBytes >= maxCacheSizeBytes
    }
    
    /**
     * Gets cache usage percentage
     */
    suspend fun getCacheUsagePercentage(): Float = withContext(Dispatchers.IO) {
        val maxCacheSizeBytes = MAX_CACHE_SIZE_MB * 1024 * 1024
        val currentSize = getCacheStatistics().totalSizeBytes
        (currentSize.toFloat() / maxCacheSizeBytes.toFloat()) * 100f
    }
    
    // Helper methods
    
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
            // Return current size on error
        }
        return size
    }
    
    private fun countFiles(directory: File): Int {
        return try {
            directory.listFiles()?.size ?: 0
        } catch (e: Exception) {
            0
        }
    }
    
    private fun countExpiredFiles(): Int {
        val cutoffTime = System.currentTimeMillis() - (CACHE_EXPIRY_HOURS * 60 * 60 * 1000L)
        var expiredCount = 0
        
        try {
            expiredCount += countExpiredFilesInDirectory(processedImagesCache, cutoffTime)
            expiredCount += countExpiredFilesInDirectory(ocrResultsCache, cutoffTime)
            expiredCount += countExpiredFilesInDirectory(thumbnailsCache, cutoffTime)
        } catch (e: Exception) {
            // Return current count on error
        }
        
        return expiredCount
    }
    
    private fun countExpiredFilesInDirectory(directory: File, cutoffTime: Long): Int {
        return try {
            directory.listFiles()?.count { file ->
                file.isFile && file.lastModified() < cutoffTime
            } ?: 0
        } catch (e: Exception) {
            0
        }
    }
    
    private fun clearDirectory(directory: File): Int {
        var filesDeleted = 0
        try {
            directory.listFiles()?.forEach { file ->
                if (file.isFile && file.delete()) {
                    filesDeleted++
                }
            }
        } catch (e: Exception) {
            // Continue on error
        }
        return filesDeleted
    }
    
    private fun clearExpiredFiles(directory: File, cutoffTime: Long): Int {
        var filesDeleted = 0
        try {
            directory.listFiles()?.forEach { file ->
                if (file.isFile && file.lastModified() < cutoffTime && file.delete()) {
                    filesDeleted++
                }
            }
        } catch (e: Exception) {
            // Continue on error
        }
        return filesDeleted
    }
    
    private fun addFilesToList(directory: File, fileList: MutableList<File>) {
        try {
            directory.listFiles()?.forEach { file ->
                if (file.isFile) {
                    fileList.add(file)
                }
            }
        } catch (e: Exception) {
            // Continue on error
        }
    }
    
    private fun isFileExpired(file: File): Boolean {
        val cutoffTime = System.currentTimeMillis() - (CACHE_EXPIRY_HOURS * 60 * 60 * 1000L)
        return file.lastModified() < cutoffTime
    }
}