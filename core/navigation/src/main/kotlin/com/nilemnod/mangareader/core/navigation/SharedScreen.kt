package com.nilemnod.mangareader.core.navigation

import cafe.adriel.voyager.core.registry.ScreenProvider
import cafe.adriel.voyager.core.registry.ScreenRegistry
import cafe.adriel.voyager.core.screen.Screen

interface MangaDestination

sealed interface SharedScreen : ScreenProvider {

    data class Manga(
        val mangaId: Long,
        val fromSource: Boolean = false,
    ) : SharedScreen

    data class BrowseSource(
        val sourceId: Long,
        val listingQuery: String? = null,
    ) : SharedScreen

    data class GlobalSearch(
        val query: String = "",
        val extensionFilter: String? = null,
    ) : SharedScreen

    data class MigrationConfig(
        val mangaIds: List<Long>,
    ) : SharedScreen

    data class WebView(
        val url: String,
        val initialTitle: String? = null,
        val sourceId: Long? = null,
    ) : SharedScreen

    data class SourcePreferences(
        val sourceId: Long,
    ) : SharedScreen

    data class ExtensionStores(
        val repoUrl: String? = null,
    ) : SharedScreen

    data object Stats : SharedScreen

    data object Category : SharedScreen

    data object DownloadQueue : SharedScreen

    data object Upcoming : SharedScreen
}

fun SharedScreen.screen(): Screen = ScreenRegistry.get(this)
