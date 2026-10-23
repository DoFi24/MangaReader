package com.nilemnod.mangareader.core.migration.migrations

import com.nilemnod.mangareader.app.domain.base.BasePreferences
import com.nilemnod.mangareader.core.common.FeatureFlags
import com.nilemnod.mangareader.core.migration.Migration
import com.nilemnod.mangareader.core.migration.MigrationContext
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import kotlin.uuid.ExperimentalUuidApi

@Inject
@ContributesIntoSet(AppScope::class)
class InstallationIdMigration(
    private val basePreferences: BasePreferences,
) : Migration {
    override val version: Float = Migration.ALWAYS

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun invoke(migrationContext: MigrationContext): Boolean {
        val installationId = basePreferences.installationId
        if (!installationId.isSet()) installationId.set(FeatureFlags.newInstallationId())
        return true
    }
}
