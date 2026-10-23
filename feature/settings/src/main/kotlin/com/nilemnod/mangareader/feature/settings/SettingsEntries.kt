package com.nilemnod.mangareader.feature.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import cafe.adriel.voyager.core.screen.Screen
import com.nilemnod.mangareader.core.ui.i18n.stringResource
import com.nilemnod.mangareader.feature.settings.about.AboutScreen
import com.nilemnod.mangareader.feature.settings.screen.SearchableSettings
import com.nilemnod.mangareader.feature.settings.screen.SettingsAdvancedScreen
import com.nilemnod.mangareader.feature.settings.screen.SettingsAppearanceScreen
import com.nilemnod.mangareader.feature.settings.screen.SettingsBrowseScreen
import com.nilemnod.mangareader.feature.settings.screen.SettingsDataScreen
import com.nilemnod.mangareader.feature.settings.screen.SettingsDownloadScreen
import com.nilemnod.mangareader.feature.settings.screen.SettingsLibraryScreen
import com.nilemnod.mangareader.feature.settings.screen.SettingsReaderScreen
import com.nilemnod.mangareader.feature.settings.screen.SettingsSecurityScreen
import com.nilemnod.mangareader.i18n.MR
import com.nilemnod.mangareader.icons.custommaterialsymbols.CustomMaterialSymbols
import com.nilemnod.mangareader.icons.custommaterialsymbols.edge.About
import com.nilemnod.mangareader.icons.custommaterialsymbols.edge.Appearance
import com.nilemnod.mangareader.icons.custommaterialsymbols.edge.Browse
import com.nilemnod.mangareader.icons.custommaterialsymbols.edge.Download
import com.nilemnod.mangareader.icons.custommaterialsymbols.edge.Library
import com.nilemnod.mangareader.icons.custommaterialsymbols.edge.ReaderLtr
import com.nilemnod.mangareader.icons.custommaterialsymbols.edge.Security
import com.nilemnod.mangareader.icons.custommaterialsymbols.edge.Storage
import com.nilemnod.mangareader.icons.materialsymbols.MaterialSymbols
import com.nilemnod.mangareader.icons.materialsymbols.rounded.Code
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
            icon = CustomMaterialSymbols.Edge.Appearance,
            screen = SettingsAppearanceScreen,
        ),
        SettingsEntry(
            order = ORDER_LIBRARY,
            titleRes = MR.strings.pref_category_library,
            subtitleRes = MR.strings.pref_library_summary,
            icon = CustomMaterialSymbols.Edge.Library,
            screen = SettingsLibraryScreen,
        ),
        SettingsEntry(
            order = ORDER_READER,
            titleRes = MR.strings.pref_category_reader,
            subtitleRes = MR.strings.pref_reader_summary,
            icon = CustomMaterialSymbols.Edge.ReaderLtr,
            screen = SettingsReaderScreen,
        ),
        SettingsEntry(
            order = ORDER_DOWNLOADS,
            titleRes = MR.strings.pref_category_downloads,
            subtitleRes = MR.strings.pref_downloads_summary,
            icon = CustomMaterialSymbols.Edge.Download,
            screen = SettingsDownloadScreen,
        ),
        SettingsEntry(
            order = ORDER_BROWSE,
            titleRes = MR.strings.browse,
            subtitleRes = MR.strings.pref_browse_summary,
            icon = CustomMaterialSymbols.Edge.Browse,
            screen = SettingsBrowseScreen,
        ),
        SettingsEntry(
            order = ORDER_DATA,
            titleRes = MR.strings.label_data_storage,
            subtitleRes = MR.strings.pref_backup_summary,
            icon = CustomMaterialSymbols.Edge.Storage,
            screen = SettingsDataScreen,
            destination = SettingsScreen.Destination.DataAndStorage.id,
        ),
        SettingsEntry(
            order = ORDER_SECURITY,
            titleRes = MR.strings.pref_category_security,
            subtitleRes = MR.strings.pref_security_summary,
            icon = CustomMaterialSymbols.Edge.Security,
            screen = SettingsSecurityScreen,
        ),
        SettingsEntry(
            order = ORDER_ADVANCED,
            titleRes = MR.strings.pref_category_advanced,
            subtitleRes = MR.strings.pref_advanced_summary,
            icon = MaterialSymbols.Rounded.Code,
            screen = SettingsAdvancedScreen,
        ),
        SettingsEntry(
            order = ORDER_ABOUT,
            titleRes = MR.strings.pref_category_about,
            formatSubtitle = {
                val context = LocalContext.current
                "${stringResource(MR.strings.app_name)} ${AboutScreen.getVersionName(context, withBuildDate = false)}"
            },
            icon = CustomMaterialSymbols.Edge.About,
            screen = AboutScreen,
            destination = SettingsScreen.Destination.About.id,
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
