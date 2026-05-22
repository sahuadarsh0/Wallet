package com.technitedminds.wallet.presentation.screens.backup

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.technitedminds.wallet.domain.model.backup.BackupFailureReason
import com.technitedminds.wallet.domain.model.backup.BackupResult
import com.technitedminds.wallet.domain.model.backup.RestoreConflictStrategy
import com.technitedminds.wallet.domain.model.backup.RestorePreview
import com.technitedminds.wallet.domain.model.backup.RestoreResult
import com.technitedminds.wallet.domain.repository.BackupProgress
import com.technitedminds.wallet.domain.usecase.backup.ExportBackupUseCase
import com.technitedminds.wallet.domain.usecase.backup.ImportBackupUseCase
import com.technitedminds.wallet.domain.usecase.backup.PreviewBackupUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Drives the offline backup/restore section of Settings. The ViewModel never
 * holds the user's passphrase past the boundary of a single export/restore
 * call — passphrases enter the ViewModel as `CharArray`, are forwarded to the
 * use case, and the use case wipes them in its `finally` block.
 */
@HiltViewModel
class BackupViewModel @Inject constructor(
    private val exportBackup: ExportBackupUseCase,
    private val previewBackup: PreviewBackupUseCase,
    private val importBackup: ImportBackupUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(BackupUiState())
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    fun showExportDialog() {
        _uiState.update { it.copy(dialog = BackupDialog.ExportPassphrase) }
    }

    fun showImportPicker() {
        _uiState.update { it.copy(dialog = BackupDialog.PickRestoreFile) }
    }

    fun dismissDialog() {
        _uiState.update { it.copy(dialog = BackupDialog.None) }
    }

    fun clearMessage() {
        _uiState.update { it.copy(transientMessage = null) }
    }

    fun consumeShareUri() {
        _uiState.update { it.copy(pendingShareUri = null, pendingShareDisplayName = null) }
    }

    fun startExport(passphrase: CharArray) {
        if (_uiState.value.busy) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    busy = true,
                    progress = BackupProgress.GATHERING_DATA,
                    dialog = BackupDialog.ProgressExport,
                )
            }
            val result = runCatching {
                exportBackup(passphrase) { phase ->
                    _uiState.update { it.copy(progress = phase) }
                }
            }.getOrElse { error ->
                BackupResult.Failure(BackupFailureReason.UNKNOWN, error.message)
            }

            when (result) {
                is BackupResult.Success -> _uiState.update {
                    it.copy(
                        busy = false,
                        progress = null,
                        dialog = BackupDialog.None,
                        pendingShareUri = result.fileUri,
                        pendingShareDisplayName = result.displayName,
                        transientMessage = "Backup saved to Downloads/CardVault as ${result.displayName}.",
                    )
                }
                is BackupResult.Failure -> _uiState.update {
                    it.copy(
                        busy = false,
                        progress = null,
                        dialog = BackupDialog.None,
                        transientMessage = friendlyExportError(result),
                    )
                }
            }
        }
    }

    /**
     * The user just picked a `.wallet` file. We stash the URI and ask for the
     * passphrase; we don't touch the file until they enter one and tap Continue.
     */
    fun onRestoreFilePicked(uri: Uri) {
        _uiState.update {
            it.copy(
                pendingRestoreUri = uri,
                dialog = BackupDialog.RestorePassphrase,
            )
        }
    }

    fun startPreview(passphrase: CharArray) {
        val uri = _uiState.value.pendingRestoreUri ?: return
        if (_uiState.value.busy) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(busy = true, dialog = BackupDialog.ProgressDecrypt)
            }
            // The preview use case wipes whatever CharArray it receives in its
            // `finally` block. We need the passphrase a second time for the actual
            // restore, so hand the use case a throwaway copy and stash a separate
            // copy in state. Both copies are wiped at their respective consumption
            // points (use case zeros the throwaway; [startRestore] hands the stash
            // to import which zeros it there).
            val forUseCase = passphrase.copyOf()
            val forRestore = passphrase.copyOf()
            passphrase.fill('\u0000')

            val outcome = runCatching { previewBackup(uri, forUseCase) }
            outcome.onSuccess { preview ->
                _uiState.update {
                    it.copy(
                        busy = false,
                        pendingPreview = preview,
                        dialog = BackupDialog.RestorePreview,
                        pendingPassphrase = forRestore,
                    )
                }
            }.onFailure { e ->
                forRestore.fill('\u0000')
                _uiState.update {
                    it.copy(
                        busy = false,
                        dialog = BackupDialog.None,
                        pendingRestoreUri = null,
                        pendingPreview = null,
                        transientMessage = friendlyRestoreError(e),
                    )
                }
            }
        }
    }

    fun startRestore(strategy: RestoreConflictStrategy) {
        val uri = _uiState.value.pendingRestoreUri ?: return
        val passphrase = _uiState.value.pendingPassphrase ?: return
        // Hand passphrase to use case; null it out of state immediately.
        _uiState.update { it.copy(pendingPassphrase = null) }
        viewModelScope.launch {
            _uiState.update {
                it.copy(busy = true, progress = BackupProgress.READING, dialog = BackupDialog.ProgressRestore)
            }
            val outcome = runCatching {
                importBackup(uri, passphrase, strategy) { phase ->
                    _uiState.update { it.copy(progress = phase) }
                }
            }
            outcome.onSuccess { result ->
                _uiState.update {
                    it.copy(
                        busy = false,
                        progress = null,
                        dialog = BackupDialog.None,
                        pendingRestoreUri = null,
                        pendingPreview = null,
                        transientMessage = formatRestoreSuccess(result),
                    )
                }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        busy = false,
                        progress = null,
                        dialog = BackupDialog.None,
                        pendingRestoreUri = null,
                        pendingPreview = null,
                        transientMessage = friendlyRestoreError(e),
                    )
                }
            }
        }
    }

    private fun friendlyExportError(failure: BackupResult.Failure): String = when (failure.reason) {
        BackupFailureReason.STORAGE_FULL ->
            "Couldn't save the backup — your device is out of free space."
        BackupFailureReason.IO_ERROR ->
            "Couldn't write the backup file. ${failure.message ?: ""}".trim()
        BackupFailureReason.CRYPTO_ERROR ->
            "Encryption failed while creating the backup."
        BackupFailureReason.CANCELLED ->
            "Backup was cancelled."
        BackupFailureReason.UNKNOWN ->
            "Backup failed. ${failure.message ?: ""}".trim()
    }

    private fun friendlyRestoreError(e: Throwable): String {
        val msg = e.message.orEmpty()
        return when {
            msg.contains("password", ignoreCase = true) ||
                msg.contains("tampered", ignoreCase = true) ->
                "That password didn't work, or the file has been tampered with."
            msg.contains("Not a CardVault", ignoreCase = true) ->
                "This file isn't a CardVault backup."
            msg.contains("newer", ignoreCase = true) ->
                "This backup was made with a newer version of CardVault. Please update the app."
            msg.contains("manifest", ignoreCase = true) ||
                msg.contains("corrupted", ignoreCase = true) ->
                "The backup file is corrupted and can't be opened."
            msg.isBlank() -> "Restore failed."
            else -> "Restore failed: $msg"
        }
    }

    private fun formatRestoreSuccess(result: RestoreResult): String {
        val parts = mutableListOf<String>()
        if (result.cardsAdded > 0) parts += "${result.cardsAdded} card${if (result.cardsAdded == 1) "" else "s"} added"
        if (result.cardsOverwritten > 0) parts += "${result.cardsOverwritten} replaced"
        if (result.cardsSkipped > 0) parts += "${result.cardsSkipped} skipped"
        if (result.categoriesAdded > 0) parts += "${result.categoriesAdded} categor${if (result.categoriesAdded == 1) "y" else "ies"} added"
        return if (parts.isEmpty()) "Nothing to restore." else "Restored: " + parts.joinToString(", ") + "."
    }
}

/**
 * Backup-feature-local UI state. Kept separate from [com.technitedminds.wallet.presentation.screens.settings.SettingsUiState]
 * so the dialog stack doesn't bloat the existing settings state model.
 */
data class BackupUiState(
    val busy: Boolean = false,
    val progress: BackupProgress? = null,
    val dialog: BackupDialog = BackupDialog.None,

    val pendingRestoreUri: Uri? = null,
    val pendingPreview: RestorePreview? = null,
    val pendingPassphrase: CharArray? = null,

    val pendingShareUri: Uri? = null,
    val pendingShareDisplayName: String? = null,

    val transientMessage: String? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BackupUiState) return false
        return busy == other.busy &&
            progress == other.progress &&
            dialog == other.dialog &&
            pendingRestoreUri == other.pendingRestoreUri &&
            pendingPreview == other.pendingPreview &&
            pendingPassphrase.contentEqualsOrBothNull(other.pendingPassphrase) &&
            pendingShareUri == other.pendingShareUri &&
            pendingShareDisplayName == other.pendingShareDisplayName &&
            transientMessage == other.transientMessage
    }

    override fun hashCode(): Int {
        var result = busy.hashCode()
        result = 31 * result + (progress?.hashCode() ?: 0)
        result = 31 * result + dialog.hashCode()
        result = 31 * result + (pendingRestoreUri?.hashCode() ?: 0)
        result = 31 * result + (pendingPreview?.hashCode() ?: 0)
        result = 31 * result + (pendingPassphrase?.contentHashCode() ?: 0)
        result = 31 * result + (pendingShareUri?.hashCode() ?: 0)
        result = 31 * result + (pendingShareDisplayName?.hashCode() ?: 0)
        result = 31 * result + (transientMessage?.hashCode() ?: 0)
        return result
    }
}

private fun CharArray?.contentEqualsOrBothNull(other: CharArray?): Boolean {
    if (this == null && other == null) return true
    if (this == null || other == null) return false
    return this.contentEquals(other)
}

enum class BackupDialog {
    None,
    ExportPassphrase,
    ProgressExport,
    PickRestoreFile,
    RestorePassphrase,
    ProgressDecrypt,
    RestorePreview,
    ProgressRestore,
}
