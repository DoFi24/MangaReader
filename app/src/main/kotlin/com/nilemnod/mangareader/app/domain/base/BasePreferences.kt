package com.nilemnod.mangareader.app.domain.base

import android.content.Context
import com.nilemnod.mangareader.core.common.preference.Preference
import com.nilemnod.mangareader.core.common.preference.PreferenceStore
import com.nilemnod.mangareader.domain.base.service.AppStatePreferences
import com.nilemnod.mangareader.domain.base.service.ModePreferences
import com.nilemnod.mangareader.i18n.MR
import dev.icerock.moko.resources.StringResource
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@Inject
@SingleIn(AppScope::class)
class BasePreferences(
    val context: Context,
    preferenceStore: PreferenceStore,
    modePreferences: ModePreferences,
    appStatePreferences: AppStatePreferences,
) {

    val downloadedOnly: Preference<Boolean> = modePreferences.downloadedOnly

    val incognitoMode: Preference<Boolean> = modePreferences.incognitoMode

    val extensionInstaller: ExtensionInstallerPreference = ExtensionInstallerPreference(context, preferenceStore)

    val shownOnboardingFlow: Preference<Boolean> = appStatePreferences.shownOnboardingFlow

    enum class ExtensionInstaller(val titleRes: StringResource, val requiresSystemPermission: Boolean) {
        LEGACY(MR.strings.ext_installer_legacy, true),
        PACKAGEINSTALLER(MR.strings.ext_installer_packageinstaller, true),
        SHIZUKU(MR.strings.ext_installer_shizuku, false),
        PRIVATE(MR.strings.ext_installer_private, false),
    }

    val highQualityRenderer: Preference<Boolean> = preferenceStore.getBoolean("pref_high_quality_renderer_key", false)

    val installationId: Preference<String> = appStatePreferences.installationId

    val skippedAppUpdate: Preference<String> = appStatePreferences.skippedAppUpdate
}
