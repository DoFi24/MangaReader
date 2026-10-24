package com.nilemnod.mangareader.app.domain.base

import android.content.Context
import com.nilemnod.mangareader.core.common.preference.Preference
import com.nilemnod.mangareader.domain.base.service.AppStatePreferences
import com.nilemnod.mangareader.domain.base.service.ModePreferences
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@Inject
@SingleIn(AppScope::class)
class BasePreferences(
    val context: Context,
    modePreferences: ModePreferences,
    appStatePreferences: AppStatePreferences,
) {

    val downloadedOnly: Preference<Boolean> = modePreferences.downloadedOnly

    val incognitoMode: Preference<Boolean> = modePreferences.incognitoMode

    val shownOnboardingFlow: Preference<Boolean> = appStatePreferences.shownOnboardingFlow

    val installationId: Preference<String> = appStatePreferences.installationId

    val skippedAppUpdate: Preference<String> = appStatePreferences.skippedAppUpdate
}
