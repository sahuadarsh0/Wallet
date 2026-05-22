package com.technitedminds.wallet.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages user preferences using DataStore. Handles app settings, default categories,
 * and user configuration.
 */
@Singleton
class SimplePreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private const val PREFERENCES_NAME = "wallet_preferences"
        
        // Preference keys
        private val THEME_MODE = stringPreferencesKey("theme_mode")
        private val DEFAULT_CATEGORIES_INITIALIZED = booleanPreferencesKey("default_categories_initialized")
        private val LAST_BACKUP_TIME = stringPreferencesKey("last_backup_time")
        private val AUTO_BACKUP_ENABLED = booleanPreferencesKey("auto_backup_enabled")
        private val CAMERA_FLASH_ENABLED = booleanPreferencesKey("camera_flash_enabled")
        private val OCR_CONFIDENCE_THRESHOLD = intPreferencesKey("ocr_confidence_threshold")
        private val CARD_FLIP_ANIMATION_ENABLED = booleanPreferencesKey("card_flip_animation_enabled")
        private val SHOW_CARD_NUMBERS = booleanPreferencesKey("show_card_numbers")
        private val BIOMETRIC_AUTH_ENABLED = booleanPreferencesKey("biometric_auth_enabled")
        private val APP_LOCK_ENABLED = booleanPreferencesKey("app_lock_enabled")
        private val APP_LOCK_TIMEOUT = intPreferencesKey("app_lock_timeout")
        private val PIN_HASH = stringPreferencesKey("pin_hash")
        private val PIN_SALT = stringPreferencesKey("pin_salt")
        private val RECOVERY_CODE_HASH = stringPreferencesKey("recovery_code_hash")
        private val RECOVERY_CODE_SALT = stringPreferencesKey("recovery_code_salt")
        private val LAST_UNLOCK_EPOCH = longPreferencesKey("last_unlock_epoch")
        private val FAILED_PIN_ATTEMPTS = intPreferencesKey("failed_pin_attempts")
        private val LOCKOUT_UNTIL_EPOCH = longPreferencesKey("lockout_until_epoch")
        private val FIRST_LAUNCH = booleanPreferencesKey("first_launch")
        private val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        private val STORAGE_CLEANUP_LAST_RUN = stringPreferencesKey("storage_cleanup_last_run")
        private val PREFERRED_CARD_ASPECT_RATIO = stringPreferencesKey("preferred_card_aspect_ratio")
        private val DEFAULT_CARD_CATEGORY = stringPreferencesKey("default_card_category")
        private val LAST_DB_OPTIMIZATION = stringPreferencesKey("last_db_optimization")
        private val FOLDER_THEME = stringPreferencesKey("folder_theme")
        private val BACKGROUND_PATTERN = stringPreferencesKey("background_pattern")
    }
    
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = PREFERENCES_NAME)
    
    // Theme preferences
    suspend fun setThemeMode(themeMode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE] = themeMode.name
        }
    }
    
    fun getThemeMode(): Flow<ThemeMode> {
        return context.dataStore.data.map { preferences ->
            val themeName = preferences[THEME_MODE] ?: ThemeMode.SYSTEM.name
            try {
                ThemeMode.valueOf(themeName)
            } catch (e: IllegalArgumentException) {
                ThemeMode.SYSTEM
            }
        }
    }
    
    // Default categories initialization
    suspend fun setDefaultCategoriesInitialized(initialized: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DEFAULT_CATEGORIES_INITIALIZED] = initialized
        }
    }
    
    fun isDefaultCategoriesInitialized(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[DEFAULT_CATEGORIES_INITIALIZED] ?: false
        }
    }
    
    // Backup preferences
    suspend fun setLastBackupTime(timestamp: String) {
        context.dataStore.edit { preferences ->
            preferences[LAST_BACKUP_TIME] = timestamp
        }
    }
    
    fun getLastBackupTime(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[LAST_BACKUP_TIME]
        }
    }
    
    suspend fun setAutoBackupEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[AUTO_BACKUP_ENABLED] = enabled
        }
    }
    
    fun isAutoBackupEnabled(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[AUTO_BACKUP_ENABLED] ?: false
        }
    }
    
    // Camera preferences
    suspend fun setCameraFlashEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[CAMERA_FLASH_ENABLED] = enabled
        }
    }
    
    fun isCameraFlashEnabled(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[CAMERA_FLASH_ENABLED] ?: false
        }
    }
    
    suspend fun setPreferredCardAspectRatio(aspectRatio: String) {
        context.dataStore.edit { preferences ->
            preferences[PREFERRED_CARD_ASPECT_RATIO] = aspectRatio
        }
    }
    
    fun getPreferredCardAspectRatio(): Flow<String> {
        return context.dataStore.data.map { preferences ->
            preferences[PREFERRED_CARD_ASPECT_RATIO] ?: "16:9"
        }
    }
    
    // OCR preferences
    suspend fun setOcrConfidenceThreshold(threshold: Int) {
        context.dataStore.edit { preferences ->
            preferences[OCR_CONFIDENCE_THRESHOLD] = threshold
        }
    }
    
    fun getOcrConfidenceThreshold(): Flow<Int> {
        return context.dataStore.data.map { preferences ->
            preferences[OCR_CONFIDENCE_THRESHOLD] ?: 70
        }
    }
    
    // UI preferences
    suspend fun setCardFlipAnimationEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[CARD_FLIP_ANIMATION_ENABLED] = enabled
        }
    }
    
    fun isCardFlipAnimationEnabled(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[CARD_FLIP_ANIMATION_ENABLED] ?: true
        }
    }
    
    suspend fun setShowCardNumbers(show: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SHOW_CARD_NUMBERS] = show
        }
    }
    
    fun shouldShowCardNumbers(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[SHOW_CARD_NUMBERS] ?: false
        }
    }
    
    // Security preferences
    suspend fun setBiometricAuthEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[BIOMETRIC_AUTH_ENABLED] = enabled
        }
    }
    
    fun isBiometricAuthEnabled(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[BIOMETRIC_AUTH_ENABLED] ?: false
        }
    }
    
    suspend fun setAppLockTimeout(timeoutMinutes: Int) {
        context.dataStore.edit { preferences ->
            preferences[APP_LOCK_TIMEOUT] = timeoutMinutes
        }
    }
    
    fun getAppLockTimeout(): Flow<Int> {
        return context.dataStore.data.map { preferences ->
            preferences[APP_LOCK_TIMEOUT] ?: 0 // Default 0 = immediate
        }
    }
    
    // App lock enable/disable
    suspend fun setAppLockEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[APP_LOCK_ENABLED] = enabled
        }
    }
    
    fun isAppLockEnabled(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[APP_LOCK_ENABLED] ?: false
        }
    }
    
    // PIN hash storage
    suspend fun setPinHash(hash: String) {
        context.dataStore.edit { preferences ->
            preferences[PIN_HASH] = hash
        }
    }
    
    fun getPinHash(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[PIN_HASH]
        }
    }
    
    suspend fun setPinSalt(salt: String) {
        context.dataStore.edit { preferences ->
            preferences[PIN_SALT] = salt
        }
    }
    
    fun getPinSalt(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[PIN_SALT]
        }
    }
    
    // Recovery code hash storage
    suspend fun setRecoveryCodeHash(hash: String) {
        context.dataStore.edit { preferences ->
            preferences[RECOVERY_CODE_HASH] = hash
        }
    }
    
    fun getRecoveryCodeHash(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[RECOVERY_CODE_HASH]
        }
    }
    
    suspend fun setRecoveryCodeSalt(salt: String) {
        context.dataStore.edit { preferences ->
            preferences[RECOVERY_CODE_SALT] = salt
        }
    }
    
    fun getRecoveryCodeSalt(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[RECOVERY_CODE_SALT]
        }
    }
    
    // Last unlock timestamp
    suspend fun setLastUnlockEpoch(epoch: Long) {
        context.dataStore.edit { preferences ->
            preferences[LAST_UNLOCK_EPOCH] = epoch
        }
    }
    
    fun getLastUnlockEpoch(): Flow<Long> {
        return context.dataStore.data.map { preferences ->
            preferences[LAST_UNLOCK_EPOCH] ?: 0L
        }
    }
    
    // Rate limiting preferences
    suspend fun setFailedPinAttempts(attempts: Int) {
        context.dataStore.edit { preferences ->
            preferences[FAILED_PIN_ATTEMPTS] = attempts
        }
    }
    
    suspend fun getFailedPinAttempts(): Int {
        return context.dataStore.data.map { preferences ->
            preferences[FAILED_PIN_ATTEMPTS] ?: 0
        }.first()
    }
    
    suspend fun setLockoutUntilEpoch(epoch: Long) {
        context.dataStore.edit { preferences ->
            preferences[LOCKOUT_UNTIL_EPOCH] = epoch
        }
    }
    
    suspend fun getLockoutUntilEpoch(): Long {
        return context.dataStore.data.map { preferences ->
            preferences[LOCKOUT_UNTIL_EPOCH] ?: 0L
        }.first()
    }
    
    // Clear all security-related preferences (for data wipe)
    suspend fun clearSecurityPreferences() {
        context.dataStore.edit { preferences ->
            preferences.remove(APP_LOCK_ENABLED)
            preferences.remove(BIOMETRIC_AUTH_ENABLED)
            preferences.remove(PIN_HASH)
            preferences.remove(PIN_SALT)
            preferences.remove(RECOVERY_CODE_HASH)
            preferences.remove(RECOVERY_CODE_SALT)
            preferences.remove(LAST_UNLOCK_EPOCH)
            preferences.remove(APP_LOCK_TIMEOUT)
            preferences.remove(FAILED_PIN_ATTEMPTS)
            preferences.remove(LOCKOUT_UNTIL_EPOCH)
        }
    }
    
    // App lifecycle preferences
    suspend fun setFirstLaunch(isFirstLaunch: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[FIRST_LAUNCH] = isFirstLaunch
        }
    }
    
    fun isFirstLaunch(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[FIRST_LAUNCH] ?: true
        }
    }
    
    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED] = completed
        }
    }
    
    fun isOnboardingCompleted(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[ONBOARDING_COMPLETED] ?: false
        }
    }
    
    // Storage management preferences
    suspend fun setStorageCleanupLastRun(timestamp: String) {
        context.dataStore.edit { preferences ->
            preferences[STORAGE_CLEANUP_LAST_RUN] = timestamp
        }
    }
    
    fun getStorageCleanupLastRun(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[STORAGE_CLEANUP_LAST_RUN]
        }
    }
    
    // Default category preference
    suspend fun setDefaultCardCategory(categoryId: String) {
        context.dataStore.edit { preferences ->
            preferences[DEFAULT_CARD_CATEGORY] = categoryId
        }
    }
    
    fun getDefaultCardCategory(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[DEFAULT_CARD_CATEGORY]
        }
    }
    
    // Database optimization tracking
    suspend fun setLastDbOptimization(timestamp: String) {
        context.dataStore.edit { preferences ->
            preferences[LAST_DB_OPTIMIZATION] = timestamp
        }
    }

    fun getLastDbOptimization(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[LAST_DB_OPTIMIZATION]
        }
    }

    // Folder theme preferences (persisted as enum name; defaults handled by callers)
    suspend fun setFolderTheme(themeName: String) {
        context.dataStore.edit { preferences ->
            preferences[FOLDER_THEME] = themeName
        }
    }

    fun getFolderTheme(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[FOLDER_THEME]
        }
    }

    suspend fun setBackgroundPattern(patternName: String) {
        context.dataStore.edit { preferences ->
            preferences[BACKGROUND_PATTERN] = patternName
        }
    }

    fun getBackgroundPattern(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[BACKGROUND_PATTERN]
        }
    }

    // Utility methods
    suspend fun clearAllPreferences() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
    
    suspend fun resetToDefaults() {
        context.dataStore.edit { preferences ->
            preferences.clear()
            // Set default values
            preferences[THEME_MODE] = ThemeMode.SYSTEM.name
            preferences[CARD_FLIP_ANIMATION_ENABLED] = true
            preferences[CAMERA_FLASH_ENABLED] = false
            preferences[OCR_CONFIDENCE_THRESHOLD] = 70
            preferences[APP_LOCK_TIMEOUT] = 0
            preferences[APP_LOCK_ENABLED] = false
            preferences[SHOW_CARD_NUMBERS] = false
            preferences[BIOMETRIC_AUTH_ENABLED] = false
            preferences[AUTO_BACKUP_ENABLED] = false
            preferences[PREFERRED_CARD_ASPECT_RATIO] = "16:9"
        }
    }
    
    // Get all preferences as a map (useful for debugging or export)
    fun getAllPreferences(): Flow<Map<String, Any?>> {
        return context.dataStore.data.map { preferences ->
            preferences.asMap().mapKeys { it.key.name }
        }
    }
}

/**
 * Enum representing different theme modes for the app
 */
enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM;
    
    fun getDisplayName(): String = when (this) {
        LIGHT -> "Light"
        DARK -> "Dark"
        SYSTEM -> "System Default"
    }
}
