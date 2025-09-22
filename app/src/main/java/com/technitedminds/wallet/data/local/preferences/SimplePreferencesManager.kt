package com.technitedminds.wallet.data.local.preferences

import android.content.Context
import android.content.SharedPreferences
import com.technitedminds.wallet.domain.model.Category
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Simple preferences manager using SharedPreferences. Provides basic app settings and user
 * preferences.
 */
@Singleton
class SimplePreferencesManager
@Inject
constructor(@ApplicationContext private val context: Context) {

    companion object {
        private const val PREFS_NAME = "wallet_preferences"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        private const val KEY_DEFAULT_CATEGORIES_INITIALIZED = "default_categories_initialized"
        private const val KEY_FIRST_LAUNCH_TIMESTAMP = "first_launch_timestamp"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_SHOW_CARD_NUMBERS = "show_card_numbers"
        private const val KEY_DEFAULT_CATEGORY_ID = "default_category_id"
        private const val KEY_AUTO_OCR_ENABLED = "auto_ocr_enabled"
    }

    private val prefs: SharedPreferences =
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** Checks if onboarding has been completed */
    fun isOnboardingCompleted(): Boolean {
        return prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }

    /** Marks onboarding as completed */
    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply()
    }

    /** Checks if default categories have been initialized */
    fun areDefaultCategoriesInitialized(): Boolean {
        return prefs.getBoolean(KEY_DEFAULT_CATEGORIES_INITIALIZED, false)
    }

    /** Marks default categories as initialized */
    fun setDefaultCategoriesInitialized(initialized: Boolean) {
        prefs.edit().putBoolean(KEY_DEFAULT_CATEGORIES_INITIALIZED, initialized).apply()
    }

    /** Gets the first launch timestamp */
    fun getFirstLaunchTimestamp(): Long {
        return prefs.getLong(KEY_FIRST_LAUNCH_TIMESTAMP, 0L)
    }

    /** Sets the first launch timestamp */
    fun setFirstLaunchTimestamp(timestamp: Long) {
        prefs.edit().putLong(KEY_FIRST_LAUNCH_TIMESTAMP, timestamp).apply()
    }

    /** Checks if biometric authentication is enabled */
    fun isBiometricEnabled(): Boolean {
        return prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)
    }

    /** Sets biometric authentication preference */
    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }

    /** Gets the theme mode */
    fun getThemeMode(): String {
        return prefs.getString(KEY_THEME_MODE, "system") ?: "system"
    }

    /** Sets the theme mode */
    fun setThemeMode(themeMode: String) {
        prefs.edit().putString(KEY_THEME_MODE, themeMode).apply()
    }

    /** Checks if card numbers should be shown */
    fun shouldShowCardNumbers(): Boolean {
        return prefs.getBoolean(KEY_SHOW_CARD_NUMBERS, true)
    }

    /** Sets whether to show card numbers */
    fun setShowCardNumbers(show: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_CARD_NUMBERS, show).apply()
    }

    /** Gets the default category ID */
    fun getDefaultCategoryId(): String {
        return prefs.getString(KEY_DEFAULT_CATEGORY_ID, Category.DEFAULT_CATEGORY_ID)
                ?: Category.DEFAULT_CATEGORY_ID
    }

    /** Sets the default category ID */
    fun setDefaultCategoryId(categoryId: String) {
        prefs.edit().putString(KEY_DEFAULT_CATEGORY_ID, categoryId).apply()
    }

    /** Checks if auto OCR is enabled */
    fun isAutoOcrEnabled(): Boolean {
        return prefs.getBoolean(KEY_AUTO_OCR_ENABLED, true)
    }

    /** Sets auto OCR preference */
    fun setAutoOcrEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_OCR_ENABLED, enabled).apply()
    }

    /** Initializes default categories if not already done */
    fun initializeDefaultCategoriesIfNeeded(): List<Category> {
        return if (!areDefaultCategoriesInitialized()) {
            val defaultCategories = createDefaultCategories()
            setDefaultCategoriesInitialized(true)
            defaultCategories
        } else {
            emptyList()
        }
    }

    /** Creates default categories */
    private fun createDefaultCategories(): List<Category> {
        val currentTime = System.currentTimeMillis()

        return listOf(
                Category(
                        id = Category.DEFAULT_CATEGORY_ID,
                        name = "Uncategorized",
                        iconResId = android.R.drawable.ic_menu_gallery,
                        colorHex = "#757575",
                        isDefault = true,
                        createdAt = currentTime,
                        updatedAt = currentTime
                ),
                Category(
                        id = Category.CREDIT_CARDS_CATEGORY_ID,
                        name = "Credit Cards",
                        iconResId = android.R.drawable.ic_menu_gallery,
                        colorHex = "#2196F3",
                        isDefault = true,
                        createdAt = currentTime,
                        updatedAt = currentTime
                ),
                Category(
                        id = Category.DEBIT_CARDS_CATEGORY_ID,
                        name = "Debit Cards",
                        iconResId = android.R.drawable.ic_menu_gallery,
                        colorHex = "#4CAF50",
                        isDefault = true,
                        createdAt = currentTime,
                        updatedAt = currentTime
                ),
                Category(
                        id = Category.GIFT_CARDS_CATEGORY_ID,
                        name = "Gift Cards",
                        iconResId = android.R.drawable.ic_menu_gallery,
                        colorHex = "#FF9800",
                        isDefault = true,
                        createdAt = currentTime,
                        updatedAt = currentTime
                ),
                Category(
                        id = Category.MEMBERSHIP_CATEGORY_ID,
                        name = "Membership",
                        iconResId = android.R.drawable.ic_menu_gallery,
                        colorHex = "#9C27B0",
                        isDefault = true,
                        createdAt = currentTime,
                        updatedAt = currentTime
                )
        )
    }
}
