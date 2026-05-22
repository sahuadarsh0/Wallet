package com.technitedminds.wallet.presentation.screens.backup

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.technitedminds.wallet.presentation.constants.AppConstants

/**
 * Settings entry for offline backup/restore. Self-contained: owns its own
 * ViewModel, dialog stack, file picker, and share-sheet launch. Drop into
 * the existing `SettingsScreen` inside a `SettingsSection` block.
 *
 * @param onShowMessage forwarded to the host snackbar.
 */
@Composable
fun BackupAndRestoreSection(
    onShowMessage: (String) -> Unit,
    viewModel: BackupViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri: Uri? ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            }
            viewModel.onRestoreFilePicked(uri)
        } else {
            viewModel.dismissDialog()
        }
    }

    BackupRow(
        title = "Back up your wallet",
        subtitle = "Encrypt and save a copy you control to Downloads, then share to any cloud you trust.",
        icon = Icons.Default.Backup,
        onClick = { viewModel.showExportDialog() },
    )
    BackupRow(
        title = "Restore from a backup",
        subtitle = "Pick a .wallet file from any storage app — Drive, Downloads, USB, etc.",
        icon = Icons.Default.Restore,
        onClick = {
            viewModel.showImportPicker()
            openDocumentLauncher.launch(arrayOf("application/octet-stream", "*/*"))
        },
    )

    when (uiState.dialog) {
        BackupDialog.None -> Unit
        BackupDialog.PickRestoreFile -> Unit

        BackupDialog.ExportPassphrase -> ExportPassphraseDialog(
            onConfirm = viewModel::startExport,
            onDismiss = viewModel::dismissDialog,
        )
        BackupDialog.RestorePassphrase -> RestorePassphraseDialog(
            onConfirm = viewModel::startPreview,
            onDismiss = viewModel::dismissDialog,
        )
        BackupDialog.RestorePreview -> uiState.pendingPreview?.let { preview ->
            RestorePreviewDialog(
                preview = preview,
                onConfirm = viewModel::startRestore,
                onDismiss = viewModel::dismissDialog,
            )
        }
        BackupDialog.ProgressExport -> BackupProgressDialog(
            title = "Creating backup",
            phase = uiState.progress,
            onCancel = null,
        )
        BackupDialog.ProgressDecrypt -> BackupProgressDialog(
            title = "Reading backup",
            phase = uiState.progress,
            onCancel = null,
        )
        BackupDialog.ProgressRestore -> BackupProgressDialog(
            title = "Restoring backup",
            phase = uiState.progress,
            onCancel = null,
        )
    }

    val message = uiState.transientMessage
    LaunchedEffect(message) {
        if (!message.isNullOrBlank()) {
            onShowMessage(message)
            viewModel.clearMessage()
        }
    }

    val shareUri = uiState.pendingShareUri
    val shareName = uiState.pendingShareDisplayName
    LaunchedEffect(shareUri, shareName) {
        if (shareUri != null) {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/octet-stream"
                putExtra(Intent.EXTRA_STREAM, shareUri)
                if (shareName != null) putExtra(Intent.EXTRA_SUBJECT, shareName)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(intent, "Share encrypted backup")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            runCatching { context.startActivity(chooser) }
            viewModel.consumeShareUri()
        }
    }
}

@Composable
private fun BackupRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = AppConstants.Dimensions.PADDING_SMALL),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppConstants.Dimensions.SPACING_MEDIUM),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            modifier = Modifier.size(AppConstants.Dimensions.SETTINGS_ITEM_ICON_SIZE),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
