package com.nilemnod.mangareader.domain.base.service

import com.nilemnod.mangareader.core.common.preference.Preference
import com.nilemnod.mangareader.core.common.preference.PreferenceStore
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@Inject
@SingleIn(AppScope::class)
class ModePreferences(
    preferenceStore: PreferenceStore,
) {

    val downloadedOnly: Preference<Boolean> = preferenceStore.getBoolean(
        Preference.appStateKey("pref_downloaded_only"),
        false,
    )

    val incognitoMode: Preference<Boolean> = preferenceStore.getBoolean(Preference.appStateKey("incognito_mode"), false)
}
