package com.technitedminds.wallet.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.technitedminds.wallet.data.local.files.ImageFileManager
import com.technitedminds.wallet.data.local.preferences.SimplePreferencesManager
import com.technitedminds.wallet.data.local.preferences.ThemeMode
import com.technitedminds.wallet.domain.usecase.category.GetCategoriesUseCase
import com.technitedminds.wallet.domain.usecase.category.ManageCategoryUseCase
import com.technitedminds.wallet.domain.usecase.card.GetCardsUseCase
import com.technitedminds.wallet.domain.usecase.storage.StorageManagementUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject



/**
 * Settings UI state
 */
data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val totalCards: Int = 0,
    val totalCategories: Int = 0,
    val storageUsedMB: Float = 0f,
    val availableStorageMB: Float = 0f,
    val storageUsagePercentage: Float = 0f,
    val orphanedFileCount: Int = 0,
    val lastCleanupTime: String? = null,
    val isStorageSpaceLow: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showResetDialog: Boolean = false,
    val showCleanupDialog: Boolean = false,
    val cleanupInProgress: Boolean = false,
    val cleanupResult: String? = null
)

/**
 * ViewModel for settings screen
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getCardsUseCase: GetCardsUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val manageCategoryUseCase: ManageCategoryUseCase,
    private val preferencesManager: SimplePreferencesManager,
    private val imageFileManager: ImageFileManager,
    private val storageManagementUseCase: StorageManagementUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    init {
        loadStatistics()
        loadThemePreference()
    }
    
    /**
     * Load app statistics
     */
    private fun loadStatistics() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                // Get card count
                getCardsUseCase.getAllCards().collect { cards ->
                    val cardCount = cards.size
                    
                    // Get category count
                    getCategoriesUseCase().collect { categories ->
                        val categoryCount = categories.size
                        
                        // Get comprehensive storage statistics
                        storageManagementUseCase.getStorageStatistics().collect { storageStats ->
                            val storageUsedMB = storageStats.totalUsedBytes / (1024f * 1024f)
                            val availableStorageBytes = storageManagementUseCase.getAvailableStorageSpace()
                            val availableStorageMB = availableStorageBytes / (1024f * 1024f)
                            val totalStorageBytes = storageStats.totalUsedBytes + availableStorageBytes
                            val usagePercentage = storageManagementUseCase.calculateStorageUsagePercentage(
                                storageStats.totalUsedBytes, totalStorageBytes
                            )
                            
                            _uiState.value = _uiState.value.copy(
                                totalCards = cardCount,
                                totalCategories = categoryCount,
                                storageUsedMB = storageUsedMB,
                                availableStorageMB = availableStorageMB,
                                storageUsagePercentage = usagePercentage,
                                orphanedFileCount = storageStats.orphanedFileCount,
                                lastCleanupTime = storageStats.lastCleanupTime,
                                isStorageSpaceLow = storageManagementUseCase.isStorageSpaceLow(),
                                isLoading = false
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load statistics"
                )
            }
        }
    }
    
    /**
     * Load theme preference from storage
     */
    private fun loadThemePreference() {
        viewModelScope.launch {
            preferencesManager.getThemeMode().collect { themeMode ->
                _uiState.value = _uiState.value.copy(themeMode = themeMode)
            }
        }
    }
    
    /**
     * Update theme mode
     */
    fun updateThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            try {
                preferencesManager.setThemeMode(themeMode)
                _uiState.value = _uiState.value.copy(themeMode = themeMode)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to update theme"
                )
            }
        }
    }
    
    /**
     * Show reset confirmation dialog
     */
    fun showResetDialog() {
        _uiState.value = _uiState.value.copy(showResetDialog = true)
    }
    
    /**
     * Hide reset confirmation dialog
     */
    fun hideResetDialog() {
        _uiState.value = _uiState.value.copy(showResetDialog = false)
    }
    
    /**
     * Show cleanup confirmation dialog
     */
    fun showCleanupDialog() {
        _uiState.value = _uiState.value.copy(showCleanupDialog = true)
    }
    
    /**
     * Hide cleanup confirmation dialog
     */
    fun hideCleanupDialog() {
        _uiState.value = _uiState.value.copy(showCleanupDialog = false)
    }
    
    /**
     * Reset default categories
     */
    fun resetDefaultCategories() {
        viewModelScope.launch {
            try {
                manageCategoryUseCase.createDefaultCategories()
                hideResetDialog()
                loadStatistics() // Refresh statistics
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to reset categories"
                )
            }
        }
    }
    
    /**
     * Perform comprehensive storage cleanup
     */
    fun performStorageCleanup() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    cleanupInProgress = true,
                    isLoading = true
                )
                
                // Perform comprehensive cleanup
                val cleanupResult = storageManagementUseCase.performStorageCleanup()
                
                // Format cleanup result message
                val resultMessage = buildString {
                    append("Cleanup completed:\n")
                    if (cleanupResult.orphanedImagesRemoved > 0) {
                        append("• ${cleanupResult.orphanedImagesRemoved} orphaned images removed\n")
                    }
                    if (cleanupResult.cacheFilesRemoved > 0) {
                        append("• ${cleanupResult.cacheFilesRemoved} cache files removed\n")
                    }
                    if (cleanupResult.expiredCacheFilesRemoved > 0) {
                        append("• ${cleanupResult.expiredCacheFilesRemoved} expired cache files removed\n")
                    }
                    if (cleanupResult.tempFilesRemoved > 0) {
                        append("• ${cleanupResult.tempFilesRemoved} temporary files removed\n")
                    }
                    if (cleanupResult.logFilesRemoved > 0) {
                        append("• ${cleanupResult.logFilesRemoved} log files removed\n")
                    }
                    if (cleanupResult.databaseOptimized) {
                        append("• Database optimized\n")
                    }
                    if (cleanupResult.bytesFreed > 0) {
                        val freedMB = cleanupResult.bytesFreed / (1024f * 1024f)
                        append("• ${String.format("%.1f", freedMB)} MB freed")
                    }
                    
                    if (isEmpty()) {
                        append("No cleanup needed - storage is already optimized")
                    }
                }
                
                hideCleanupDialog()
                loadStatistics() // Refresh statistics
                
                _uiState.value = _uiState.value.copy(
                    cleanupInProgress = false,
                    isLoading = false,
                    cleanupResult = resultMessage
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    cleanupInProgress = false,
                    isLoading = false,
                    error = e.message ?: "Failed to cleanup storage"
                )
            }
        }
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    /**
     * Clear cleanup result message
     */
    fun clearCleanupResult() {
        _uiState.value = _uiState.value.copy(cleanupResult = null)
    }
    

}