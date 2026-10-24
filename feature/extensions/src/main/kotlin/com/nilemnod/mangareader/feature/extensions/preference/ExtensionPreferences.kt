package com.nilemnod.mangareader.feature.extensions.preference

import android.content.Context
import com.nilemnod.mangareader.core.common.preference.PreferenceStore
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@Inject
@SingleIn(AppScope::class)
class ExtensionPreferences(
    context: Context,
    preferenceStore: PreferenceStore,
) {

    val installer: ExtensionInstallerPreference = ExtensionInstallerPreference(context, preferenceStore)
}
