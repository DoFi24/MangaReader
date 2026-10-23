package com.nilemnod.mangareader.core.migration.migrations

import com.nilemnod.mangareader.core.common.preference.PreferenceStore
import com.nilemnod.mangareader.core.migration.Migration
import com.nilemnod.mangareader.core.migration.MigrationContext
import com.nilemnod.mangareader.domain.extension.model.ContentWarning
import com.nilemnod.mangareader.domain.source.service.SourcePreferences
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject

@Inject
@ContributesIntoSet(AppScope::class)
class ContentWarningMigration(
    private val preferenceStore: PreferenceStore,
    private val sourcePreferences: SourcePreferences,
) : Migration {
    override val version: Float = 30f

    override suspend fun invoke(migrationContext: MigrationContext): Boolean {
        val showNsfwSource = preferenceStore.getBoolean("show_nsfw_source", true)
        if (!showNsfwSource.isSet()) return true

        if (!showNsfwSource.get()) {
            sourcePreferences.enabledContentWarnings.set(setOf(ContentWarning.SAFE))
        }
        showNsfwSource.delete()

        return true
    }
}
