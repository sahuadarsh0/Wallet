package com.technitedminds.wallet.presentation.screens.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import android.content.ClipData
import android.content.ClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.technitedminds.wallet.data.local.preferences.ThemeMode
import com.technitedminds.wallet.presentation.components.common.ConfirmationDialog
import com.technitedminds.wallet.presentation.components.common.ScreenGradientBackground
import com.technitedminds.wallet.presentation.constants.AppConstants
import com.technitedminds.wallet.presentation.screens.security.AppLockScreen
import com.technitedminds.wallet.presentation.screens.security.AppLockViewModel
import com.technitedminds.wallet.presentation.screens.security.PinScreenMode
import com.technitedminds.wallet.ui.theme.BackgroundPattern
import com.technitedminds.wallet.ui.theme.FolderTheme
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
    onNavigateToCategories: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lockState by appLockViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val clipboardManager = context.getSystemService(ClipboardManager::class.java)
    
    // Local state for dialogs
    var showPrivacyPolicy by remember { mutableStateOf(false) }
    var showTermsOfService by remember { mutableStateOf(false) }
    var showOpenSourceLicenses by remember { mutableStateOf(false) }
    var showPinSetup by remember { mutableStateOf(false) }
    var showRecoveryCodeDisplay by remember { mutableStateOf<String?>(null) }
    var showTimeoutPicker by remember { mutableStateOf(false) }
    var showAppearanceDialog by remember { mutableStateOf(false) }
    
    ScreenGradientBackground(modifier = modifier) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(AppConstants.NavigationLabels.SETTINGS) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                    ),
                )
            },
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            },
            containerColor = Color.Transparent,
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .imePadding()
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
            
            // Theme Section — single summary row that opens a confirmation
            // dialog. Nothing is committed until the user taps Apply, which
            // matches the user's expectation that this should feel "enclosed"
            // rather than three loose pickers stacked together.
            SettingsSection(
                title = AppConstants.StatisticsLabels.APPEARANCE,
                icon = Icons.Default.Palette
            ) {
                AppearanceSummaryRow(
                    themeMode = uiState.themeMode,
                    folderTheme = uiState.folderTheme,
                    backgroundPattern = uiState.backgroundPattern,
                    onClick = { showAppearanceDialog = true },
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
                    title = AppConstants.UIText.MANAGE_CATEGORIES_TITLE,
                    subtitle = AppConstants.UIText.MANAGE_CATEGORIES_SUBTITLE,
                    icon = Icons.Default.Category,
                    onClick = onNavigateToCategories
                )
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
    
    if (showPrivacyPolicy) {
        LegalDocumentDialog(
            title = AppConstants.DialogText.PRIVACY_POLICY_TITLE,
            icon = Icons.Default.PrivacyTip,
            accentColor = MaterialTheme.colorScheme.primary,
            content = AppConstants.DialogText.PRIVACY_POLICY_CONTENT,
            onDismiss = { showPrivacyPolicy = false },
        )
    }

    if (showTermsOfService) {
        LegalDocumentDialog(
            title = AppConstants.DialogText.TERMS_OF_SERVICE_TITLE,
            icon = Icons.Default.Description,
            accentColor = MaterialTheme.colorScheme.tertiary,
            content = AppConstants.DialogText.TERMS_OF_SERVICE_CONTENT,
            onDismiss = { showTermsOfService = false },
        )
    }

    if (showOpenSourceLicenses) {
        LegalDocumentDialog(
            title = AppConstants.DialogText.OPEN_SOURCE_LICENSES_TITLE,
            icon = Icons.Default.Code,
            accentColor = MaterialTheme.colorScheme.secondary,
            content = AppConstants.DialogText.OPEN_SOURCE_LICENSES_CONTENT,
            onDismiss = { showOpenSourceLicenses = false },
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
                clipboardManager?.setPrimaryClip(ClipData.newPlainText("Recovery Code", code))
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

    // Appearance customization dialog — selections are local until the user
    // taps Apply, giving them a confirmation step instead of mutating the
    // whole app the moment they touch a swatch.
    if (showAppearanceDialog) {
        AppearanceDialog(
            initialThemeMode = uiState.themeMode,
            initialFolderTheme = uiState.folderTheme,
            initialBackgroundPattern = uiState.backgroundPattern,
            onApply = { themeMode, folderTheme, pattern ->
                if (themeMode != uiState.themeMode) viewModel.updateThemeMode(themeMode)
                if (folderTheme != uiState.folderTheme) viewModel.updateFolderTheme(folderTheme)
                if (pattern != uiState.backgroundPattern) viewModel.updateBackgroundPattern(pattern)
                showAppearanceDialog = false
            },
            onDismiss = { showAppearanceDialog = false },
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
                contentDescription = title,
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
            contentDescription = title,
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
            contentDescription = "Navigate",
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
            contentDescription = label,
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
                    contentDescription = when (theme) {
                        ThemeMode.LIGHT -> "Light theme"
                        ThemeMode.DARK -> "Dark theme"
                        ThemeMode.SYSTEM -> "System theme"
                    },
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
 * Single-row summary that lives inside the Appearance section card. Shows a
 * tiny stack of mini-swatches representing the current selections plus a
 * chevron, and opens the [AppearanceDialog] when tapped. This keeps the
 * settings screen quiet — three controls are tucked behind one row instead
 * of stacked open in the layout.
 */
@Composable
private fun AppearanceSummaryRow(
    themeMode: ThemeMode,
    folderTheme: FolderTheme,
    backgroundPattern: BackgroundPattern,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val themeLabel = when (themeMode) {
        ThemeMode.LIGHT -> "Light"
        ThemeMode.DARK -> "Dark"
        ThemeMode.SYSTEM -> "System"
    }
    val summary = "$themeLabel · ${folderTheme.displayName} · ${backgroundPattern.displayName}"

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Stacked mini-swatches preview the active folder theme palette so
        // users see what's currently active at a glance.
        Row(horizontalArrangement = Arrangement.spacedBy((-6).dp)) {
            folderTheme.palette.take(3).forEach { gradient ->
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(gradient))
                        .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape),
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Customize appearance",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = summary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Modal bottom sheet that hosts all three appearance controls (theme mode,
 * folder theme and background pattern) with explicit Cancel / Apply actions.
 * Selections are kept in dialog-local state until the user confirms, so they
 * can audition combinations without committing.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppearanceDialog(
    initialThemeMode: ThemeMode,
    initialFolderTheme: FolderTheme,
    initialBackgroundPattern: BackgroundPattern,
    onApply: (ThemeMode, FolderTheme, BackgroundPattern) -> Unit,
    onDismiss: () -> Unit,
) {
    var draftThemeMode by remember { mutableStateOf(initialThemeMode) }
    var draftFolderTheme by remember { mutableStateOf(initialFolderTheme) }
    var draftPattern by remember { mutableStateOf(initialBackgroundPattern) }
    val sheetState = androidx.compose.material3.rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    )

    androidx.compose.material3.ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Appearance",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Preview a combo, then tap Apply when you're happy.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            AppearanceSubsectionLabel(text = "App theme")
            ThemeSelector(
                selectedTheme = draftThemeMode,
                onThemeSelected = { draftThemeMode = it },
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            AppearanceSubsectionLabel(text = "Folder theme")
            FolderThemePicker(
                selected = draftFolderTheme,
                onSelect = { draftFolderTheme = it },
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            AppearanceSubsectionLabel(text = "Background pattern")
            BackgroundPatternPicker(
                selected = draftPattern,
                onSelect = { draftPattern = it },
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        onApply(draftThemeMode, draftFolderTheme, draftPattern)
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Apply")
                }
            }
        }
    }
}

/**
 * Compact label used as a sub-header inside the Appearance card. Keeps the
 * three picker rows visually grouped without giving each one the heavy
 * weight of a full settings section header.
 */
@Composable
private fun AppearanceSubsectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(bottom = 4.dp),
    )
}

/**
 * Horizontal preview row of folder theme palettes. Each chip renders a
 * miniature gradient swatch + label so users can pick visually instead of
 * reading enum names. The currently selected option is outlined with the
 * primary tint and pulled forward via a subtle shadow tone.
 */
@Composable
private fun FolderThemePicker(
    selected: FolderTheme,
    onSelect: (FolderTheme) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(FolderTheme.entries.toList()) { theme ->
                FolderThemeSwatch(
                    theme = theme,
                    isSelected = theme == selected,
                    onClick = { onSelect(theme) },
                )
            }
        }
        Text(
            text = selected.description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun FolderThemeSwatch(
    theme: FolderTheme,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
    }
    val borderWidth = if (isSelected) 2.dp else 1.dp

    // Use the first three palette gradients to show the theme's range.
    val previewStops = remember(theme) {
        theme.palette.take(3).flatten().ifEmpty { theme.allCardsGradient }
    }

    Column(
        modifier = Modifier
            .width(88.dp)
            .clickable(onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Brush.linearGradient(previewStops))
                .border(borderWidth, borderColor, RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center,
        ) {
            // Three-dot palette indicator using the theme's palette pairs to
            // hint at how categories will be colored.
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                theme.palette.take(3).forEach { pair ->
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(pair.first())
                            .border(0.5.dp, Color.White.copy(alpha = 0.4f), CircleShape),
                    )
                }
            }
        }
        Text(
            text = theme.displayName,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

/**
 * Background pattern selector. Renders four pill options inline with a tiny
 * preview canvas so users can see what each option does without enabling it
 * first.
 */
@Composable
private fun BackgroundPatternPicker(
    selected: BackgroundPattern,
    onSelect: (BackgroundPattern) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            BackgroundPattern.entries.forEach { pattern ->
                BackgroundPatternChip(
                    pattern = pattern,
                    isSelected = pattern == selected,
                    onClick = { onSelect(pattern) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
        Text(
            text = selected.description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun BackgroundPatternChip(
    pattern: BackgroundPattern,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
    }
    val borderWidth = if (isSelected) 2.dp else 1.dp
    val previewBg = MaterialTheme.colorScheme.surfaceContainerHighest
    val previewInk = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .border(borderWidth, borderColor, RoundedCornerShape(14.dp))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        // Live miniature of the actual pattern so users see a real preview
        // instead of an emoji approximation. The preview canvas matches the
        // logic used at runtime in ScreenGradientBackground.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(previewBg),
        ) {
            androidx.compose.foundation.Canvas(
                modifier = Modifier.fillMaxSize(),
            ) {
                val ink = previewInk.copy(alpha = 0.45f)
                when (pattern) {
                    BackgroundPattern.NONE -> Unit
                    BackgroundPattern.DOTS -> {
                        val s = 10f
                        var row = 0
                        var y = s / 2f
                        while (y < size.height) {
                            val xo = if (row % 2 == 0) 0f else s / 2f
                            var x = s / 2f + xo
                            while (x < size.width) {
                                drawCircle(ink, radius = 1.4f, center = Offset(x, y))
                                x += s
                            }
                            y += s; row += 1
                        }
                    }
                    BackgroundPattern.GRID -> {
                        val s = 9f
                        var x = 0f
                        while (x < size.width) {
                            drawLine(ink, Offset(x, 0f), Offset(x, size.height), strokeWidth = 0.8f)
                            x += s
                        }
                        var y = 0f
                        while (y < size.height) {
                            drawLine(ink, Offset(0f, y), Offset(size.width, y), strokeWidth = 0.8f)
                            y += s
                        }
                    }
                    BackgroundPattern.TOPO -> {
                        val cx = size.width * 0.3f
                        val cy = size.height * 0.6f
                        var r = 6f
                        while (r < maxOf(size.width, size.height)) {
                            drawCircle(
                                color = ink,
                                radius = r,
                                center = Offset(cx, cy),
                                style = Stroke(width = 0.9f),
                            )
                            r += 8f
                        }
                    }
                }
            }
        }
        Text(
            text = pattern.displayName,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
        )
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
            contentDescription = title,
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
                        contentDescription = "Recovery code",
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
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
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
        com.technitedminds.wallet.BuildConfig.BUILD_TYPE
    } catch (e: Exception) {
        AppConstants.StatisticsLabels.UNKNOWN
    }
}

/**
 * Premium full-screen dialog for legal documents (Privacy Policy, ToS, Licenses).
 * Features a glassmorphic header with icon, scrollable styled content,
 * and a gradient close button.
 */
@Composable
private fun LegalDocumentDialog(
    title: String,
    icon: ImageVector,
    accentColor: Color,
    content: String,
    onDismiss: () -> Unit,
) {
    val sections = remember(content) { parseLegalSections(content) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // ── Header ──────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    accentColor.copy(alpha = 0.15f),
                                    Color.Transparent,
                                ),
                            ),
                        )
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            accentColor.copy(alpha = 0.25f),
                                            accentColor.copy(alpha = 0.08f),
                                        ),
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(26.dp),
                            )
                        }

                        Spacer(Modifier.width(16.dp))

                        Column {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = "Technited Minds",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                )

                // ── Scrollable content ──────────────────────────────
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    sections.forEach { section ->
                        when (section) {
                            is LegalSection.Title -> {
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = section.text,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = accentColor,
                                )
                                Spacer(Modifier.height(4.dp))
                            }
                            is LegalSection.Body -> {
                                Text(
                                    text = section.text,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                                    lineHeight = 22.sp,
                                )
                            }
                            is LegalSection.Bullet -> {
                                Row(modifier = Modifier.padding(start = 8.dp, top = 2.dp, bottom = 2.dp)) {
                                    Text(
                                        text = "•",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = accentColor,
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = section.text,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                                        lineHeight = 22.sp,
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                )

                // ── Footer ──────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Text(
                            text = "Got It",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}

private sealed interface LegalSection {
    data class Title(val text: String) : LegalSection
    data class Body(val text: String) : LegalSection
    data class Bullet(val text: String) : LegalSection
}

private fun parseLegalSections(raw: String): List<LegalSection> {
    val result = mutableListOf<LegalSection>()
    val lines = raw.trimIndent().lines()

    for (line in lines) {
        val trimmed = line.trim()
        when {
            trimmed.isBlank() -> {}
            trimmed.startsWith("•") || trimmed.startsWith("-") -> {
                result += LegalSection.Bullet(trimmed.removePrefix("•").removePrefix("-").trim())
            }
            trimmed.matches(Regex("^\\d+\\.\\s+.+")) -> {
                result += LegalSection.Title(trimmed)
            }
            trimmed.endsWith("Policy") || trimmed.endsWith("Service") ||
                trimmed.startsWith("Last updated") || trimmed.startsWith("Published by") -> {
                result += LegalSection.Body(trimmed)
            }
            else -> {
                result += LegalSection.Body(trimmed)
            }
        }
    }
    return result
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    WalletTheme {
        SettingsScreen()
    }
}