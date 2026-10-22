package com.nilemnod.mangareader.domain.upcoming.service

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import com.nilemnod.mangareader.core.common.preference.Preference
import com.nilemnod.mangareader.core.common.preference.PreferenceStore
import com.nilemnod.mangareader.core.common.preference.getLongArray

@Inject
@SingleIn(AppScope::class)
class UpcomingPreferences(
    preferenceStore: PreferenceStore,
) {

    val filterIncludedCategories: Preference<List<Long>> = preferenceStore.getLongArray(
        "pref_filter_upcoming_included_categories",
        emptyList(),
    )

    val filterExcludedCategories: Preference<List<Long>> = preferenceStore.getLongArray(
        "pref_filter_upcoming_excluded_categories",
        emptyList(),
    )
}
