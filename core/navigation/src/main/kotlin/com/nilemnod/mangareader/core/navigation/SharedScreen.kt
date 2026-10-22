package com.nilemnod.mangareader.core.navigation

import cafe.adriel.voyager.core.registry.ScreenProvider
import cafe.adriel.voyager.core.registry.ScreenRegistry
import cafe.adriel.voyager.core.screen.Screen

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

    data object Category : SharedScreen
}

fun SharedScreen.screen(): Screen = ScreenRegistry.get(this)
