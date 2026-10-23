package com.nilemnod.mangareader.app.navigation

import cafe.adriel.voyager.core.registry.ScreenRegistry
import com.nilemnod.mangareader.app.ui.browse.source.browse.BrowseSourceScreen
import com.nilemnod.mangareader.app.ui.browse.source.globalsearch.GlobalSearchScreen
import com.nilemnod.mangareader.app.ui.manga.MangaScreen
import com.nilemnod.mangareader.app.ui.reader.ReaderActivity
import com.nilemnod.mangareader.core.navigation.IntentRegistry
import com.nilemnod.mangareader.core.navigation.SharedIntent
import com.nilemnod.mangareader.core.navigation.SharedScreen
import com.nilemnod.mangareader.feature.categories.CategoryScreen

object AppNavigation {

    fun register() {
        ScreenRegistry {
            register<SharedScreen.Manga> { MangaScreen(it.mangaId, it.fromSource) }
            register<SharedScreen.BrowseSource> { BrowseSourceScreen(it.sourceId, it.listingQuery) }
            register<SharedScreen.GlobalSearch> { GlobalSearchScreen(it.query, it.extensionFilter) }
            register<SharedScreen.Category> { CategoryScreen() }
        }
        IntentRegistry.register<SharedIntent.Reader> { context, intent ->
            ReaderActivity.newIntent(context, intent.mangaId, intent.chapterId)
        }
    }
}
