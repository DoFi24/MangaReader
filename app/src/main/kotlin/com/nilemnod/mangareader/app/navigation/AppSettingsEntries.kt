package com.nilemnod.mangareader.app.navigation

import com.nilemnod.mangareader.app.presentation.more.settings.screen.SettingsAdvancedScreen
import com.nilemnod.mangareader.app.presentation.more.settings.screen.SettingsDataScreen
import com.nilemnod.mangareader.app.presentation.more.settings.screen.SettingsReaderScreen
import com.nilemnod.mangareader.app.presentation.more.settings.screen.about.AboutScreen
import com.nilemnod.mangareader.core.ui.i18n.stringResource
import com.nilemnod.mangareader.feature.settings.SettingsEntries
import com.nilemnod.mangareader.feature.settings.SettingsEntry
import com.nilemnod.mangareader.feature.settings.SettingsScreen
import com.nilemnod.mangareader.i18n.MR
import com.nilemnod.mangareader.icons.materialsymbols.MaterialSymbols
import com.nilemnod.mangareader.icons.materialsymbols.automirroredrounded.ChromeReaderMode
import com.nilemnod.mangareader.icons.materialsymbols.rounded.Code
import com.nilemnod.mangareader.icons.materialsymbols.rounded.Info
import com.nilemnod.mangareader.icons.materialsymbols.rounded.Storage

object AppSettingsEntries {

    fun register() {
        SettingsEntries.register(
            SettingsEntry(
                order = SettingsEntries.ORDER_READER,
                titleRes = MR.strings.pref_category_reader,
                subtitleRes = MR.strings.pref_reader_summary,
                icon = MaterialSymbols.AutoMirroredRounded.ChromeReaderMode,
                screen = SettingsReaderScreen,
            ),
        )
        SettingsEntries.register(
            SettingsEntry(
                order = SettingsEntries.ORDER_DATA,
                titleRes = MR.strings.label_data_storage,
                subtitleRes = MR.strings.pref_backup_summary,
                icon = MaterialSymbols.Rounded.Storage,
                screen = SettingsDataScreen,
                destination = SettingsScreen.Destination.DataAndStorage.id,
            ),
        )
        SettingsEntries.register(
            SettingsEntry(
                order = SettingsEntries.ORDER_ADVANCED,
                titleRes = MR.strings.pref_category_advanced,
                subtitleRes = MR.strings.pref_advanced_summary,
                icon = MaterialSymbols.Rounded.Code,
                screen = SettingsAdvancedScreen,
            ),
        )
        SettingsEntries.register(
            SettingsEntry(
                order = SettingsEntries.ORDER_ABOUT,
                titleRes = MR.strings.pref_category_about,
                formatSubtitle = {
                    "${stringResource(MR.strings.app_name)} ${AboutScreen.getVersionName(withBuildDate = false)}"
                },
                icon = MaterialSymbols.Rounded.Info,
                screen = AboutScreen,
                destination = SettingsScreen.Destination.About.id,
            ),
        )
    }
}
