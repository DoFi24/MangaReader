package com.nilemnod.mangareader.core.migration.migrations

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import com.nilemnod.mangareader.core.migration.Migration
import com.nilemnod.mangareader.core.migration.MigrationContext
import com.nilemnod.mangareader.domain.library.service.LibraryPreferences

@Inject
@ContributesIntoSet(AppScope::class)
class ChapterNameSuffixMigration(
    private val preference: LibraryPreferences,
) : Migration {
    override val version: Float = 34f

    override suspend fun invoke(migrationContext: MigrationContext): Boolean {
        if (migrationContext.previousVersion in 13..33) {
            preference.enableChapterNameHash.set(true)
        }
        return true
    }
}
