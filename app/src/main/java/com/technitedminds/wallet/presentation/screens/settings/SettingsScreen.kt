package com.technitedminds.wallet.presentation.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.technitedminds.wallet.data.local.preferences.ThemeMode
import com.technitedminds.wallet.presentation.components.common.ConfirmationDialog
import com.technitedminds.wallet.presentation.constants.AppConstants
import com.technitedminds.wallet.ui.theme.WalletTheme

/**
 * Settings screen for app configuration
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Local state for dialogs
    var showPrivacyPolicy by remember { mutableStateOf(false) }
    var showOpenSourceLicenses by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(AppConstants.NavigationLabels.SETTINGS) }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // App Statistics Section
            SettingsSection(
                title = AppConstants.StatisticsLabels.APP_STATISTICS,
                icon = Icons.Default.Analytics
            ) {
                StatisticItem(
                    label = AppConstants.StatisticsLabels.TOTAL_CARDS,
                    value = uiState.totalCards.toString(),
                    icon = Icons.Default.CreditCard
                )
                
                StatisticItem(
                    label = AppConstants.StatisticsLabels.CATEGORIES,
                    value = uiState.totalCategories.toString(),
                    icon = Icons.Default.Category
                )
                
                StatisticItem(
                    label = AppConstants.StatisticsLabels.STORAGE_USED,
                    value = "${String.format(AppConstants.Storage.DECIMAL_FORMAT_PATTERN, uiState.storageUsedMB)} ${AppConstants.StatisticsLabels.MB_UNIT}",
                    icon = Icons.Default.Storage
                )
                
                StatisticItem(
                    label = AppConstants.StatisticsLabels.AVAILABLE_STORAGE,
                    value = "${String.format(AppConstants.Storage.DECIMAL_FORMAT_PATTERN, uiState.availableStorageMB)} ${AppConstants.StatisticsLabels.MB_UNIT}",
                    icon = Icons.Default.Storage
                )
                
                if (uiState.orphanedFileCount > 0) {
                    StatisticItem(
                        label = AppConstants.StatisticsLabels.ORPHANED_FILES,
                        value = uiState.orphanedFileCount.toString(),
                        icon = Icons.Default.Warning
                    )
                }
                
                uiState.lastCleanupTime?.let { lastCleanup ->
                    StatisticItem(
                        label = AppConstants.StatisticsLabels.LAST_CLEANUP,
                        value = lastCleanup,
                        icon = Icons.Default.Schedule
                    )
                }
            }
            
            // Theme Section
            SettingsSection(
                title = AppConstants.StatisticsLabels.APPEARANCE,
                icon = Icons.Default.Palette
            ) {
                ThemeSelector(
                    selectedTheme = uiState.themeMode,
                    onThemeSelected = viewModel::updateThemeMode
                )
            }
            
            // Category Management Section
            SettingsSection(
                title = AppConstants.StatisticsLabels.CATEGORY_MANAGEMENT,
                icon = Icons.Default.Category
            ) {
                SettingsItem(
                    title = AppConstants.UIText.RESET_DEFAULT_CATEGORIES_TITLE,
                    subtitle = AppConstants.UIText.RESET_DEFAULT_CATEGORIES_SUBTITLE,
                    icon = Icons.Default.RestoreFromTrash,
                    onClick = viewModel::showResetDialog
                )
            }
            
            // Storage Management Section
            SettingsSection(
                title = AppConstants.StatisticsLabels.STORAGE_MANAGEMENT,
                icon = Icons.Default.Storage
            ) {
                SettingsItem(
                    title = AppConstants.UIText.CLEAN_UP_STORAGE_TITLE,
                    subtitle = if (uiState.orphanedFileCount > 0) {
                        String.format(AppConstants.UIText.CLEAN_UP_STORAGE_SUBTITLE_WITH_FILES, uiState.orphanedFileCount)
                    } else {
                        AppConstants.UIText.CLEAN_UP_STORAGE_SUBTITLE_DEFAULT
                    },
                    icon = Icons.Default.CleaningServices,
                    onClick = viewModel::showCleanupDialog
                )
                
                if (uiState.isStorageSpaceLow) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = AppConstants.ContentDescriptions.WARNING_ICON,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(AppConstants.Dimensions.ICON_SIZE_SMALL)
                        )
                        Text(
                            text = AppConstants.ErrorMessages.STORAGE_LOW_WARNING,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            // App Information Section
            SettingsSection(
                title = AppConstants.StatisticsLabels.APP_INFORMATION,
                icon = Icons.Default.Info
            ) {
                AppInfoItem(
                    label = AppConstants.StatisticsLabels.VERSION,
                    value = getAppVersion(context)
                )
                
                AppInfoItem(
                    label = AppConstants.StatisticsLabels.BUILD,
                    value = getBuildType()
                )
                
                SettingsItem(
                    title = AppConstants.DialogText.PRIVACY_POLICY_TITLE,
                    subtitle = AppConstants.UIText.PRIVACY_POLICY_SUBTITLE,
                    icon = Icons.Default.PrivacyTip,
                    onClick = { showPrivacyPolicy = true }
                )
                
                SettingsItem(
                    title = AppConstants.DialogText.OPEN_SOURCE_LICENSES_TITLE,
                    subtitle = AppConstants.UIText.OPEN_SOURCE_LICENSES_SUBTITLE,
                    icon = Icons.Default.Code,
                    onClick = { showOpenSourceLicenses = true }
                )
            }
        }
    }
    
    // Reset confirmation dialog
    ConfirmationDialog(
        isVisible = uiState.showResetDialog,
        title = AppConstants.DialogText.RESET_CATEGORIES_TITLE,
        message = AppConstants.DialogText.RESET_CATEGORIES_CONTENT,
        confirmText = AppConstants.DialogText.RESET_BUTTON,
        onConfirm = viewModel::resetDefaultCategories,
        onDismiss = viewModel::hideResetDialog
    )
    
    // Cleanup confirmation dialog
    ConfirmationDialog(
        isVisible = uiState.showCleanupDialog,
        title = AppConstants.DialogText.CLEAN_UP_STORAGE_TITLE,
        message = AppConstants.DialogText.CLEANUP_DIALOG_CONTENT,
        confirmText = if (uiState.cleanupInProgress) AppConstants.DialogText.CLEANING_BUTTON else AppConstants.DialogText.CLEAN_UP_BUTTON,
        onConfirm = viewModel::performStorageCleanup,
        onDismiss = viewModel::hideCleanupDialog
    )
    
    // Privacy Policy dialog
    if (showPrivacyPolicy) {
        AlertDialog(
            onDismissRequest = { showPrivacyPolicy = false },
            title = { Text(AppConstants.DialogText.PRIVACY_POLICY_TITLE) },
            text = {
                Text(
                    text = AppConstants.DialogText.PRIVACY_POLICY_CONTENT,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyPolicy = false }) {
                    Text(AppConstants.DialogText.CLOSE_BUTTON)
                }
            }
        )
    }
    
    // Open Source Licenses dialog
    if (showOpenSourceLicenses) {
        AlertDialog(
            onDismissRequest = { showOpenSourceLicenses = false },
            title = { Text(AppConstants.DialogText.OPEN_SOURCE_LICENSES_TITLE) },
            text = {
                Text(
                    text = AppConstants.DialogText.OPEN_SOURCE_LICENSES_CONTENT,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = { showOpenSourceLicenses = false }) {
                    Text(AppConstants.DialogText.CLOSE_BUTTON)
                }
            }
        )
    }
    
    // Error handling
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            snackbarHostState.showSnackbar(
                message = "${AppConstants.ErrorMessages.ERROR_PREFIX}$error",
                actionLabel = AppConstants.DialogText.DISMISS_BUTTON,
                duration = SnackbarDuration.Long
            )
            viewModel.clearError()
        }
    }
    
    // Cleanup result handling
    uiState.cleanupResult?.let { resultMessage ->
        LaunchedEffect(resultMessage) {
            snackbarHostState.showSnackbar(
                message = resultMessage,
                actionLabel = AppConstants.DialogText.OK_BUTTON,
                duration = SnackbarDuration.Long
            )
            viewModel.clearCleanupResult()
        }
    }
}

/**
 * Settings section with title and icon
 */
@Composable
private fun SettingsSection(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier.padding(AppConstants.Dimensions.PADDING_LARGE),
        verticalArrangement = Arrangement.spacedBy(AppConstants.Dimensions.SPACING_MEDIUM)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppConstants.Dimensions.SPACING_SMALL)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(AppConstants.Dimensions.ICON_SIZE_MEDIUM),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(AppConstants.Dimensions.PADDING_LARGE),
                verticalArrangement = Arrangement.spacedBy(AppConstants.Dimensions.SPACING_SMALL),
                content = content
            )
        }
    }
}

/**
 * Individual settings item
 */
@Composable
private fun SettingsItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = AppConstants.Dimensions.PADDING_SMALL),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppConstants.Dimensions.SPACING_MEDIUM)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(AppConstants.Dimensions.SETTINGS_ITEM_ICON_SIZE),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        trailing?.invoke() ?: Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Statistics item (non-clickable)
 */
@Composable
private fun StatisticItem(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = AppConstants.Dimensions.PADDING_SMALL),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppConstants.Dimensions.SPACING_MEDIUM)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(AppConstants.Dimensions.SETTINGS_ITEM_ICON_SIZE),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

/**
 * App information item (non-clickable)
 */
@Composable
private fun AppInfoItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Theme selector component
 */
@Composable
private fun ThemeSelector(
    selectedTheme: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AppConstants.Dimensions.SPACING_SMALL)
    ) {
        ThemeMode.entries.forEach { theme ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onThemeSelected(theme) }
                    .padding(vertical = AppConstants.Dimensions.SPACING_SMALL),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppConstants.Dimensions.SPACING_MEDIUM)
            ) {
                RadioButton(
                    selected = selectedTheme == theme,
                    onClick = { onThemeSelected(theme) }
                )
                
                Icon(
                    imageVector = when (theme) {
                        ThemeMode.LIGHT -> Icons.Default.LightMode
                        ThemeMode.DARK -> Icons.Default.DarkMode
                        ThemeMode.SYSTEM -> Icons.Default.SettingsBrightness
                    },
                    contentDescription = null,
                    modifier = Modifier.size(AppConstants.Dimensions.ICON_SIZE_MEDIUM),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = when (theme) {
                            ThemeMode.LIGHT -> AppConstants.ThemeLabels.LIGHT
                            ThemeMode.DARK -> AppConstants.ThemeLabels.DARK
                            ThemeMode.SYSTEM -> AppConstants.ThemeLabels.SYSTEM_DEFAULT
                        },
                        style = MaterialTheme.typography.bodyLarge
                    )
                    
                    Text(
                        text = when (theme) {
                            ThemeMode.LIGHT -> AppConstants.ThemeLabels.LIGHT_DESCRIPTION
                            ThemeMode.DARK -> AppConstants.ThemeLabels.DARK_DESCRIPTION
                            ThemeMode.SYSTEM -> AppConstants.ThemeLabels.SYSTEM_DESCRIPTION
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Get app version from context
 */
private fun getAppVersion(context: android.content.Context): String {
    return try {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        packageInfo.versionName ?: AppConstants.StatisticsLabels.UNKNOWN
    } catch (e: Exception) {
        AppConstants.StatisticsLabels.UNKNOWN
    }
}

/**
 * Get build type from BuildConfig
 */
private fun getBuildType(): String {
    return try {
        // Use reflection to get BuildConfig.BUILD_TYPE since BuildConfig is not directly accessible
        val buildConfigClass = Class.forName(AppConstants.ClassNames.BUILD_CONFIG_CLASS)
        val buildTypeField = buildConfigClass.getField(AppConstants.ClassNames.BUILD_TYPE_FIELD)
        buildTypeField.get(null) as? String ?: AppConstants.StatisticsLabels.UNKNOWN
    } catch (e: Exception) {
        AppConstants.StatisticsLabels.UNKNOWN
    }
}

/**
 * Preview for SettingsScreen
 */
@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    WalletTheme {
        SettingsScreen()
    }
}