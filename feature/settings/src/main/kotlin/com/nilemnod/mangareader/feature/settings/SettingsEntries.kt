package com.nilemnod.mangareader.feature.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import cafe.adriel.voyager.core.screen.Screen
import com.nilemnod.mangareader.core.ui.i18n.stringResource
import com.nilemnod.mangareader.feature.settings.screen.SearchableSettings
import com.nilemnod.mangareader.feature.settings.screen.SettingsAppearanceScreen
import com.nilemnod.mangareader.feature.settings.screen.SettingsBrowseScreen
import com.nilemnod.mangareader.feature.settings.screen.SettingsDownloadScreen
import com.nilemnod.mangareader.feature.settings.screen.SettingsLibraryScreen
import com.nilemnod.mangareader.feature.settings.screen.SettingsSecurityScreen
import com.nilemnod.mangareader.i18n.MR
import com.nilemnod.mangareader.icons.materialsymbols.MaterialSymbols
import com.nilemnod.mangareader.icons.materialsymbols.rounded.CollectionsBookmark
import com.nilemnod.mangareader.icons.materialsymbols.rounded.Download
import com.nilemnod.mangareader.icons.materialsymbols.rounded.Explore
import com.nilemnod.mangareader.icons.materialsymbols.rounded.Palette
import com.nilemnod.mangareader.icons.materialsymbols.rounded.Security
import dev.icerock.moko.resources.StringResource
import java.util.concurrent.CopyOnWriteArrayList

class SettingsEntry(
    val order: Int,
    val titleRes: StringResource,
    val subtitleRes: StringResource? = null,
    val formatSubtitle: @Composable () -> String? = { subtitleRes?.let { stringResource(it) } },
    val icon: ImageVector,
    val screen: Screen,
    val destination: Int? = null,
)

object SettingsEntries {

    private val registered = CopyOnWriteArrayList<SettingsEntry>()

    private val builtIn = listOf(
        SettingsEntry(
            order = ORDER_APPEARANCE,
            titleRes = MR.strings.pref_category_appearance,
            subtitleRes = MR.strings.pref_appearance_summary,
            icon = MaterialSymbols.Rounded.Palette,
            screen = SettingsAppearanceScreen,
        ),
        SettingsEntry(
            order = ORDER_LIBRARY,
            titleRes = MR.strings.pref_category_library,
            subtitleRes = MR.strings.pref_library_summary,
            icon = MaterialSymbols.Rounded.CollectionsBookmark,
            screen = SettingsLibraryScreen,
        ),
        SettingsEntry(
            order = ORDER_DOWNLOADS,
            titleRes = MR.strings.pref_category_downloads,
            subtitleRes = MR.strings.pref_downloads_summary,
            icon = MaterialSymbols.Rounded.Download,
            screen = SettingsDownloadScreen,
        ),
        SettingsEntry(
            order = ORDER_BROWSE,
            titleRes = MR.strings.browse,
            subtitleRes = MR.strings.pref_browse_summary,
            icon = MaterialSymbols.Rounded.Explore,
            screen = SettingsBrowseScreen,
        ),
        SettingsEntry(
            order = ORDER_SECURITY,
            titleRes = MR.strings.pref_category_security,
            subtitleRes = MR.strings.pref_security_summary,
            icon = MaterialSymbols.Rounded.Security,
            screen = SettingsSecurityScreen,
        ),
    )

    fun register(entry: SettingsEntry) {
        registered += entry
    }

    fun all(): List<SettingsEntry> = (builtIn + registered).sortedBy { it.order }

    fun searchable(): List<SearchableSettings> = all().mapNotNull { it.screen as? SearchableSettings }

    fun destination(id: Int): Screen? = all().firstOrNull { it.destination == id }?.screen

    const val ORDER_APPEARANCE = 10
    const val ORDER_LIBRARY = 20
    const val ORDER_READER = 30
    const val ORDER_DOWNLOADS = 40
    const val ORDER_BROWSE = 50
    const val ORDER_DATA = 60
    const val ORDER_SECURITY = 70
    const val ORDER_ADVANCED = 80
    const val ORDER_ABOUT = 90
}
