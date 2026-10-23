package com.nilemnod.mangareader.domain.base.service

import com.nilemnod.mangareader.core.common.preference.Preference
import com.nilemnod.mangareader.core.common.preference.PreferenceStore
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@Inject
@SingleIn(AppScope::class)
class AppStatePreferences(
    preferenceStore: PreferenceStore,
) {

    val shownOnboardingFlow: Preference<Boolean> = preferenceStore.getBoolean(
        Preference.appStateKey("onboarding_complete"),
        false,
    )

    val installationId: Preference<String> = preferenceStore.getString(Preference.appStateKey("installation_id"), "")

    val skippedAppUpdate: Preference<String> = preferenceStore.getString(
        Preference.appStateKey("skipped_app_update"),
        "",
    )
}
