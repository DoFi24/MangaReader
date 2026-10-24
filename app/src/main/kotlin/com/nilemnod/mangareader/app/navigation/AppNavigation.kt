package com.nilemnod.mangareader.app.navigation

import android.content.Intent
import cafe.adriel.voyager.core.registry.ScreenRegistry
import com.nilemnod.mangareader.app.presentation.more.settings.screen.about.OpenSourceLicensesScreen
import com.nilemnod.mangareader.app.ui.main.MainActivity
import com.nilemnod.mangareader.app.ui.manga.MangaCoverDialogHost
import com.nilemnod.mangareader.core.common.Constants
import com.nilemnod.mangareader.core.navigation.DialogRegistry
import com.nilemnod.mangareader.core.navigation.IntentRegistry
import com.nilemnod.mangareader.core.navigation.SharedDialog
import com.nilemnod.mangareader.core.navigation.SharedIntent
import com.nilemnod.mangareader.core.navigation.SharedScreen
import com.nilemnod.mangareader.core.ui.components.BrowseTabs
import com.nilemnod.mangareader.feature.browse.migration.config.MigrationConfigScreen
import com.nilemnod.mangareader.feature.browse.migration.dialog.MigrateMangaDialog
import com.nilemnod.mangareader.feature.browse.source.browse.BrowseSourceScreen
import com.nilemnod.mangareader.feature.browse.source.globalsearch.GlobalSearchScreen
import com.nilemnod.mangareader.feature.categories.CategoryScreen
import com.nilemnod.mangareader.feature.deeplink.DeepLinkScreen
import com.nilemnod.mangareader.feature.downloads.DownloadQueueScreen
import com.nilemnod.mangareader.feature.extensions.sourcesettings.SourcePreferencesScreen
import com.nilemnod.mangareader.feature.extensions.stores.ExtensionStoresScreen
import com.nilemnod.mangareader.feature.extensions.ui.ExtensionsBrowseTab
import com.nilemnod.mangareader.feature.manga.MangaScreen
import com.nilemnod.mangareader.feature.reader.legacy.ReaderActivity
import com.nilemnod.mangareader.feature.stats.StatsScreen
import com.nilemnod.mangareader.feature.upcoming.UpcomingScreen
import com.nilemnod.mangareader.feature.webview.WebViewActivity
import com.nilemnod.mangareader.feature.webview.WebViewScreen

object AppNavigation {

    fun register() {
        BrowseTabs.register(ExtensionsBrowseTab)
        ScreenRegistry {
            register<SharedScreen.Manga> { MangaScreen(it.mangaId, it.fromSource) }
            register<SharedScreen.BrowseSource> { BrowseSourceScreen(it.sourceId, it.listingQuery) }
            register<SharedScreen.GlobalSearch> { GlobalSearchScreen(it.query, it.extensionFilter) }
            register<SharedScreen.MigrationConfig> { MigrationConfigScreen(it.mangaIds) }
            register<SharedScreen.WebView> { WebViewScreen(it.url, it.initialTitle, it.sourceId) }
            register<SharedScreen.ExtensionStores> { ExtensionStoresScreen(it.repoUrl) }
            register<SharedScreen.DeepLink> { DeepLinkScreen(it.query) }
            register<SharedScreen.Stats> { StatsScreen() }
            register<SharedScreen.SourcePreferences> { SourcePreferencesScreen(it.sourceId) }
            register<SharedScreen.Category> { CategoryScreen() }
            register<SharedScreen.DownloadQueue> { DownloadQueueScreen }
            register<SharedScreen.Upcoming> { UpcomingScreen() }
            register<SharedScreen.OpenSourceLicenses> { OpenSourceLicensesScreen() }
        }
        DialogRegistry.register<SharedDialog.MangaCover> { MangaCoverDialogHost(it.mangaId, it.onDismissRequest) }
        DialogRegistry.register<SharedDialog.MigrateManga> {
            MigrateMangaDialog(
                current = it.current,
                target = it.target,
                onClickTitle = it.onClickTitle,
                onDismissRequest = it.onDismissRequest,
            )
        }
        IntentRegistry.register<SharedIntent.Reader> { context, intent ->
            ReaderActivity.newIntent(context, intent.mangaId, intent.chapterId)
        }
        IntentRegistry.register<SharedIntent.WebView> { context, intent ->
            WebViewActivity.newIntent(context, intent.url, intent.sourceId, intent.title)
        }
        IntentRegistry.register<SharedIntent.Manga> { context, intent ->
            Intent(context, MainActivity::class.java).apply {
                action = Constants.SHORTCUT_MANGA
                putExtra(Constants.MANGA_EXTRA, intent.mangaId)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
        }
    }
}
