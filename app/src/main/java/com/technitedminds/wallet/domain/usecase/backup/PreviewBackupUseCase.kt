package com.technitedminds.wallet.domain.usecase.backup

import android.net.Uri
import com.technitedminds.wallet.domain.model.backup.RestorePreview
import com.technitedminds.wallet.domain.repository.BackupRepository
import javax.inject.Inject

/**
 * Use case for inspecting a backup file before applying it.
 * Decrypts only the manifest — never modifies app state.
 */
class PreviewBackupUseCase @Inject constructor(
    private val backupRepository: BackupRepository,
) {
    suspend operator fun invoke(uri: Uri, passphrase: CharArray): RestorePreview =
        backupRepository.preview(uri, passphrase)
}
