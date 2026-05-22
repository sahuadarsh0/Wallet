package com.technitedminds.wallet.domain.usecase.backup

import android.net.Uri
import com.technitedminds.wallet.domain.model.backup.RestoreConflictStrategy
import com.technitedminds.wallet.domain.model.backup.RestoreResult
import com.technitedminds.wallet.domain.repository.BackupProgress
import com.technitedminds.wallet.domain.repository.BackupRepository
import javax.inject.Inject

/**
 * Use case for restoring a wallet backup with the user-chosen conflict strategy.
 */
class ImportBackupUseCase @Inject constructor(
    private val backupRepository: BackupRepository,
) {
    suspend operator fun invoke(
        uri: Uri,
        passphrase: CharArray,
        strategy: RestoreConflictStrategy,
        onProgress: (BackupProgress) -> Unit = {},
    ): RestoreResult = backupRepository.restore(uri, passphrase, strategy, onProgress)
}
