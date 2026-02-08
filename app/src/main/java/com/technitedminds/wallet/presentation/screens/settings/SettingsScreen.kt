package com.technitedminds.wallet.presentation.screens.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.technitedminds.wallet.data.local.preferences.ThemeMode
import com.technitedminds.wallet.presentation.components.common.ConfirmationDialog
import com.technitedminds.wallet.presentation.constants.AppConstants
import com.technitedminds.wallet.presentation.screens.security.AppLockScreen
import com.technitedminds.wallet.presentation.screens.security.AppLockViewModel
import com.technitedminds.wallet.presentation.screens.security.PinScreenMode
import com.technitedminds.wallet.ui.theme.WalletTheme

/**
 * Settings screen for app configuration
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
    appLockViewModel: AppLockViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lockState by appLockViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val clipboardManager = LocalClipboardManager.current
    
    // Local state for dialogs
    var showPrivacyPolicy by remember { mutableStateOf(false) }
    var showTermsOfService by remember { mutableStateOf(false) }
    var showOpenSourceLicenses by remember { mutableStateOf(false) }
    var showPinSetup by remember { mutableStateOf(false) }
    var showRecoveryCodeDisplay by remember { mutableStateOf<String?>(null) }
    var showTimeoutPicker by remember { mutableStateOf(false) }
    
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
            
            // Privacy & Security Section
            SettingsSection(
                title = AppConstants.SecurityLabels.PRIVACY_AND_SECURITY,
                icon = Icons.Default.Security
            ) {
                // App Lock toggle
                SettingsToggleItem(
                    title = AppConstants.SecurityLabels.APP_LOCK,
                    subtitle = AppConstants.SecurityLabels.APP_LOCK_SUBTITLE,
                    icon = Icons.Default.Lock,
                    checked = lockState.isLockEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled && !lockState.pinConfigured) {
                            // Must set PIN before enabling
                            showPinSetup = true
                        } else {
                            appLockViewModel.toggleAppLock(enabled)
                        }
                    }
                )

                // Biometric toggle (only when lock is enabled + hardware available)
                if (lockState.isLockEnabled && lockState.pinConfigured) {
                    SettingsToggleItem(
                        title = AppConstants.SecurityLabels.BIOMETRIC_UNLOCK,
                        subtitle = if (lockState.biometricAvailable)
                            AppConstants.SecurityLabels.BIOMETRIC_UNLOCK_SUBTITLE
                        else
                            AppConstants.SecurityLabels.BIOMETRIC_NOT_AVAILABLE,
                        icon = Icons.Default.Fingerprint,
                        checked = lockState.canUseBiometric,
                        enabled = lockState.biometricAvailable,
                        onCheckedChange = { appLockViewModel.toggleBiometric(it) }
                    )
                }

                // Set / Change PIN
                if (lockState.isLockEnabled) {
                    SettingsItem(
                        title = if (lockState.pinConfigured)
                            AppConstants.SecurityLabels.CHANGE_PIN
                        else
                            AppConstants.SecurityLabels.SET_PIN,
                        subtitle = if (lockState.pinConfigured)
                            AppConstants.SecurityLabels.CHANGE_PIN_SUBTITLE
                        else
                            AppConstants.SecurityLabels.SET_PIN_SUBTITLE,
                        icon = Icons.Default.Pin,
                        onClick = { showPinSetup = true }
                    )
                }

                // Backup code
                if (lockState.isLockEnabled && lockState.pinConfigured) {
                    SettingsItem(
                        title = AppConstants.SecurityLabels.BACKUP_CODE,
                        subtitle = AppConstants.SecurityLabels.BACKUP_CODE_SUBTITLE,
                        icon = Icons.Default.Key,
                        onClick = {
                            appLockViewModel.regenerateRecoveryCode { code ->
                                showRecoveryCodeDisplay = code
                            }
                        }
                    )
                }

                // Lock timeout
                if (lockState.isLockEnabled && lockState.pinConfigured) {
                    SettingsItem(
                        title = AppConstants.SecurityLabels.LOCK_TIMEOUT,
                        subtitle = when (lockState.lockTimeout) {
                            0 -> AppConstants.SecurityLabels.TIMEOUT_IMMEDIATE
                            1 -> AppConstants.SecurityLabels.TIMEOUT_1_MIN
                            5 -> AppConstants.SecurityLabels.TIMEOUT_5_MIN
                            15 -> AppConstants.SecurityLabels.TIMEOUT_15_MIN
                            else -> "After ${lockState.lockTimeout} minutes"
                        },
                        icon = Icons.Default.Timer,
                        onClick = { showTimeoutPicker = true }
                    )
                }
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
                    title = AppConstants.DialogText.TERMS_OF_SERVICE_TITLE,
                    subtitle = AppConstants.UIText.TERMS_OF_SERVICE_SUBTITLE,
                    icon = Icons.Default.Description,
                    onClick = { showTermsOfService = true }
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

    // Terms of Service dialog
    if (showTermsOfService) {
        AlertDialog(
            onDismissRequest = { showTermsOfService = false },
            title = { Text(AppConstants.DialogText.TERMS_OF_SERVICE_TITLE) },
            text = {
                Text(
                    text = AppConstants.DialogText.TERMS_OF_SERVICE_CONTENT,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = { showTermsOfService = false }) {
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
    
    // PIN setup — reuses the same full-screen PIN screen in Setup mode
    if (showPinSetup) {
        Dialog(
            onDismissRequest = { showPinSetup = false },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false,
            ),
        ) {
            AppLockScreen(
                mode = PinScreenMode.Setup(
                    onPinConfirmed = { pin ->
                        showPinSetup = false
                        appLockViewModel.setupNewPin(pin) { code ->
                            showRecoveryCodeDisplay = code
                        }
                    },
                    onCancel = { showPinSetup = false },
                ),
            )
        }
    }
    
    // Recovery code display dialog
    showRecoveryCodeDisplay?.let { code ->
        RecoveryCodeDisplayDialog(
            code = code,
            onDismiss = { showRecoveryCodeDisplay = null },
            onCopy = {
                clipboardManager.setText(AnnotatedString(code))
                Toast.makeText(context, AppConstants.SecurityLabels.CODE_COPIED, Toast.LENGTH_SHORT).show()
            }
        )
    }
    
    // Timeout picker dialog
    if (showTimeoutPicker) {
        TimeoutPickerDialog(
            currentTimeout = lockState.lockTimeout,
            onSelect = { minutes ->
                appLockViewModel.updateLockTimeout(minutes)
                showTimeoutPicker = false
            },
            onDismiss = { showTimeoutPicker = false }
        )
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
 * Settings toggle item with switch
 */
@Composable
private fun SettingsToggleItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
            .padding(vertical = AppConstants.Dimensions.PADDING_SMALL),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppConstants.Dimensions.SPACING_MEDIUM)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(AppConstants.Dimensions.SETTINGS_ITEM_ICON_SIZE),
            tint = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant
                   else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = if (enabled) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
        )
    }
}

/**
 * Recovery code display dialog — shown once after PIN setup
 */
@Composable
private fun RecoveryCodeDisplayDialog(
    code: String,
    onDismiss: () -> Unit,
    onCopy: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(MaterialTheme.colorScheme.tertiaryContainer, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Default.Key,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(28.dp),
                    )
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    AppConstants.SecurityLabels.RECOVERY_CODE_TITLE,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    AppConstants.SecurityLabels.RECOVERY_CODE_MESSAGE,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(20.dp))

                // Code display
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = code,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        letterSpacing = 2.sp,
                        modifier = Modifier.padding(16.dp),
                    )
                }

                Spacer(Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    OutlinedButton(
                        onClick = onCopy,
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(AppConstants.SecurityLabels.COPY_CODE, style = MaterialTheme.typography.labelMedium)
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                    ) {
                        Text(AppConstants.SecurityLabels.DONE, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

/**
 * Lock timeout picker dialog
 */
@Composable
private fun TimeoutPickerDialog(
    currentTimeout: Int,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val options = listOf(
        0 to AppConstants.SecurityLabels.TIMEOUT_IMMEDIATE,
        1 to AppConstants.SecurityLabels.TIMEOUT_1_MIN,
        5 to AppConstants.SecurityLabels.TIMEOUT_5_MIN,
        15 to AppConstants.SecurityLabels.TIMEOUT_15_MIN,
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(AppConstants.SecurityLabels.LOCK_TIMEOUT) },
        text = {
            Column {
                options.forEach { (minutes, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(minutes) }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        RadioButton(
                            selected = currentTimeout == minutes,
                            onClick = { onSelect(minutes) },
                        )
                        Text(label, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(AppConstants.DialogText.CANCEL_BUTTON)
            }
        },
    )
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