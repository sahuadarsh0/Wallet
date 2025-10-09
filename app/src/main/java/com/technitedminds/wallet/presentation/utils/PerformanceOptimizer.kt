package com.technitedminds.wallet.presentation.utils

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.technitedminds.wallet.presentation.constants.AppConstants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * Performance optimization utilities for Compose UI
 */

/**
 * Remember a derived state that only recomputes when the input changes
 * This helps avoid unnecessary recompositions
 */
@Composable
fun <T, R> rememberDerivedState(
    input: T,
    calculation: (T) -> R
): State<R> {
    return remember(input) {
        derivedStateOf { calculation(input) }
    }
}

/**
 * Optimized image size calculation for memory efficiency
 * Reduces image dimensions based on display size to save memory
 */
@Composable
fun rememberOptimizedImageSize(
    maxWidth: Dp = AppConstants.Dimensions.IMAGE_MAX_WIDTH,
    maxHeight: Dp = AppConstants.Dimensions.IMAGE_MAX_HEIGHT
): Pair<Int, Int> {
    val density = LocalDensity.current
    
    return remember(maxWidth, maxHeight, density) {
        with(density) {
            Pair(
                maxWidth.toPx().toInt(),
                maxHeight.toPx().toInt()
            )
        }
    }
}

/**
 * Debounced flow for search queries to reduce unnecessary database calls
 */
fun <T> Flow<T>.debounceDistinct(timeoutMillis: Long = AppConstants.Animation.DEBOUNCE_DELAY): Flow<T> {
    return this
        .distinctUntilChanged()
        .map { value ->
            kotlinx.coroutines.delay(timeoutMillis)
            value
        }
}

/**
 * Performance monitoring for animations
 */
@Composable
fun rememberAnimationPerformance(): AnimationPerformanceState {
    return remember { AnimationPerformanceState() }
}

class AnimationPerformanceState {
    private var frameCount = 0
    private var startTime = 0L
    
    fun startMonitoring() {
        startTime = System.currentTimeMillis()
        frameCount = 0
    }
    
    fun recordFrame() {
        frameCount++
    }
    
    fun getFPS(): Float {
        val elapsed = System.currentTimeMillis() - startTime
        return if (elapsed > 0) {
            (frameCount * AppConstants.Animation.PERFORMANCE_FRAME_MULTIPLIER) / elapsed
        } else {
            0f
        }
    }
}

/**
 * Lazy loading state for large lists
 */
@Composable
fun <T> rememberLazyLoadingState(
    items: List<T>,
    pageSize: Int = 20
): LazyLoadingState<T> {
    return remember(items, pageSize) {
        LazyLoadingState(items, pageSize)
    }
}

class LazyLoadingState<T>(
    private val allItems: List<T>,
    private val pageSize: Int
) {
    private var _loadedItems by mutableStateOf(
        allItems.take(pageSize)
    )
    
    val loadedItems: List<T> get() = _loadedItems
    val hasMore: Boolean get() = _loadedItems.size < allItems.size
    
    fun loadMore() {
        if (hasMore) {
            val nextPageEnd = minOf(_loadedItems.size + pageSize, allItems.size)
            _loadedItems = allItems.take(nextPageEnd)
        }
    }
}

/**
 * Memory-conscious bitmap loading configuration
 */
object ImageLoadingConfig {
    val THUMBNAIL_SIZE = AppConstants.Dimensions.IMAGE_THUMBNAIL_SIZE // pixels
    val CARD_PREVIEW_SIZE = AppConstants.Dimensions.IMAGE_CARD_PREVIEW_SIZE // pixels
    val FULL_SIZE_LIMIT = AppConstants.Dimensions.IMAGE_FULL_SIZE_LIMIT // pixels
    
    /**
     * Calculate appropriate sample size for image loading
     */
    fun calculateSampleSize(
        originalWidth: Int,
        originalHeight: Int,
        targetWidth: Int,
        targetHeight: Int
    ): Int {
        var sampleSize = 1
        
        if (originalHeight > targetHeight || originalWidth > targetWidth) {
            val halfHeight = originalHeight / 2
            val halfWidth = originalWidth / 2
            
            while ((halfHeight / sampleSize) >= targetHeight && 
                   (halfWidth / sampleSize) >= targetWidth) {
                sampleSize *= 2
            }
        }
        
        return sampleSize
    }
}

/**
 * Database query optimization helpers
 */
object QueryOptimization {
    /**
     * Batch size for database operations to avoid memory issues
     */
    val BATCH_SIZE = AppConstants.Storage.BATCH_SIZE
    
    /**
     * Debounce time for search queries
     */
    val SEARCH_DEBOUNCE_MS = AppConstants.Storage.SEARCH_DEBOUNCE_MS
    
    /**
     * Cache timeout for frequently accessed data
     */
    val CACHE_TIMEOUT_MS = AppConstants.Storage.CACHE_TIMEOUT_MS
}