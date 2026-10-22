package com.nilemnod.mangareader.core.migration.migrations

import android.content.Context
import com.nilemnod.mangareader.app.data.backup.create.BackupCreateWorker
import com.nilemnod.mangareader.core.migration.Migration
import com.nilemnod.mangareader.core.migration.MigrationContext
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject

@Inject
@ContributesIntoSet(AppScope::class)
class SetupBackupCreateMigration(
    private val context: Context,
) : Migration {
    override val version: Float = Migration.ALWAYS

    override suspend fun invoke(migrationContext: MigrationContext): Boolean {
        BackupCreateWorker.setupTask(context)
        return true
    }
}
