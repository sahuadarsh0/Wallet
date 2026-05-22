package com.technitedminds.wallet.domain.usecase.backup

import com.technitedminds.wallet.domain.model.backup.BackupResult
import com.technitedminds.wallet.domain.repository.BackupProgress
import com.technitedminds.wallet.domain.repository.BackupRepository
import javax.inject.Inject

/**
 * Use case for creating a fully encrypted offline backup of the wallet.
 */
class ExportBackupUseCase @Inject constructor(
    private val backupRepository: BackupRepository,
) {
    suspend operator fun invoke(
        passphrase: CharArray,
        onProgress: (BackupProgress) -> Unit = {},
    ): BackupResult = backupRepository.export(passphrase, onProgress)
}
