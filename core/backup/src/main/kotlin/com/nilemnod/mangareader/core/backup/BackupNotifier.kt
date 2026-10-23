package com.nilemnod.mangareader.core.backup

import androidx.core.app.NotificationCompat
import com.hippo.unifile.UniFile

interface BackupNotifier {

    val backupProgressId: Int

    val restoreProgressId: Int

    fun showBackupProgress(): NotificationCompat.Builder

    fun showBackupError(error: String?)

    fun showBackupComplete(file: UniFile)

    fun dismissBackupProgress()

    fun showRestoreProgress(
        content: String = "",
        progress: Int = 0,
        maxAmount: Int = 100,
        sync: Boolean = false,
    ): NotificationCompat.Builder

    fun showRestoreError(error: String?)

    fun showRestoreComplete(
        time: Long,
        errorCount: Int,
        path: String?,
        file: String?,
        sync: Boolean,
    )

    fun dismissRestoreProgress()
}
