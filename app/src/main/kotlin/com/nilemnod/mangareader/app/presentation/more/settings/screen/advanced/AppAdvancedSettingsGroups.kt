package com.nilemnod.mangareader.app.presentation.more.settings.screen.advanced

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import com.nilemnod.mangareader.app.domain.base.BasePreferences
import com.nilemnod.mangareader.app.domain.extension.interactor.TrustExtension
import com.nilemnod.mangareader.app.util.system.isReleaseBuildType
import com.nilemnod.mangareader.app.util.system.isShizukuInstalled
import com.nilemnod.mangareader.core.ui.i18n.stringResource
import com.nilemnod.mangareader.feature.settings.Preference
import com.nilemnod.mangareader.feature.settings.screen.advanced.AdvancedSettingsGroups
import com.nilemnod.mangareader.i18n.MR
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

@Inject
@ContributesBinding(AppScope::class)
class AppAdvancedSettingsGroups(
    private val basePreferences: BasePreferences,
    private val trustExtension: TrustExtension,
) : AdvancedSettingsGroups {

    @Composable
    override fun groups(): List<Preference.PreferenceGroup> = listOf(
        getReaderGroup(),
        getExtensionsGroup(),
    )

    @Composable
    private fun getReaderGroup(): Preference.PreferenceGroup {
        return Preference.PreferenceGroup(
            title = stringResource(MR.strings.pref_category_reader),
            preferenceItems = listOf(
                Preference.PreferenceItem.SwitchPreference(
                    preference = basePreferences.highQualityRenderer,
                    title = stringResource(MR.strings.pref_high_quality_renderer),
                ),
            ),
        )
    }

    @Composable
    private fun getExtensionsGroup(): Preference.PreferenceGroup {
        val context = LocalContext.current
        val uriHandler = LocalUriHandler.current
        val extensionInstallerPref = basePreferences.extensionInstaller
        var shizukuMissing by rememberSaveable { mutableStateOf(false) }

        if (shizukuMissing) {
            val dismiss = { shizukuMissing = false }
            AlertDialog(
                onDismissRequest = dismiss,
                title = { Text(text = stringResource(MR.strings.ext_installer_shizuku)) },
                text = { Text(text = stringResource(MR.strings.ext_installer_shizuku_unavailable_dialog)) },
                dismissButton = {
                    TextButton(onClick = dismiss) {
                        Text(text = stringResource(MR.strings.action_cancel))
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            dismiss()
                            uriHandler.openUri("https://shizuku.rikka.app/download")
                        },
                    ) {
                        Text(text = stringResource(MR.strings.action_ok))
                    }
                },
            )
        }
        return Preference.PreferenceGroup(
            title = stringResource(MR.strings.label_extensions),
            preferenceItems = listOf(
                Preference.PreferenceItem.ListPreference(
                    preference = extensionInstallerPref,
                    entries = extensionInstallerPref.entries
                        .filter {
                            // TODO: allow private option in stable versions once URL handling is more fleshed out
                            if (isReleaseBuildType) {
                                it != BasePreferences.ExtensionInstaller.PRIVATE
                            } else {
                                true
                            }
                        }
                        .associateWith { stringResource(it.titleRes) },
                    title = stringResource(MR.strings.ext_installer_pref),
                    onValueChanged = {
                        if (it == BasePreferences.ExtensionInstaller.SHIZUKU &&
                            !context.isShizukuInstalled
                        ) {
                            shizukuMissing = true
                            false
                        } else {
                            true
                        }
                    },
                ),
                Preference.PreferenceItem.TextPreference(
                    title = stringResource(MR.strings.ext_revoke_trust),
                    onClick = { trustExtension.revokeAll() },
                ),
            ),
        )
    }
}
