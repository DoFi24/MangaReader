package com.nilemnod.mangareader.core.backup

import com.nilemnod.mangareader.core.backup.create.BackupCreateWorker
import com.nilemnod.mangareader.core.backup.restore.BackupRestoreWorker
import com.nilemnod.mangareader.domain.backup.service.BackupPreferences
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo

@ContributesTo(AppScope::class)
interface BackupGraph {
    val backupPreferences: BackupPreferences

    fun inject(backupCreateWorker: BackupCreateWorker)

    fun inject(backupRestoreWorker: BackupRestoreWorker)
}
