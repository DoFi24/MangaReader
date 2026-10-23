package com.nilemnod.mangareader.domain.chapter.service

import com.nilemnod.mangareader.core.common.preference.Preference
import com.nilemnod.mangareader.core.common.preference.PreferenceStore
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@Inject
@SingleIn(AppScope::class)
class ChapterPreferences(
    preferenceStore: PreferenceStore,
) {

    val skipFiltered: Preference<Boolean> = preferenceStore.getBoolean("skip_filtered", true)
}
