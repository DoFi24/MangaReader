package com.nilemnod.mangareader.app.domain.source.interactor

import dev.zacsweers.metro.Inject
import com.nilemnod.mangareader.app.domain.source.service.SourcePreferences

@Inject
class SetMigrateSorting(
    private val preferences: SourcePreferences,
) {

    fun await(mode: Mode, direction: Direction) {
        preferences.migrationSortingMode.set(mode)
        preferences.migrationSortingDirection.set(direction)
    }

    enum class Mode {
        ALPHABETICAL,
        TOTAL,
    }

    enum class Direction {
        ASCENDING,
        DESCENDING,
    }
}
