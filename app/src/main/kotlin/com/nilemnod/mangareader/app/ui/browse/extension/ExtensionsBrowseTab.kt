package com.nilemnod.mangareader.app.ui.browse.extension

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nilemnod.mangareader.feature.browse.BrowseTabContribution
import com.nilemnod.mangareader.feature.browse.BrowseTabSlot
import dev.zacsweers.metrox.viewmodel.metroViewModel

object ExtensionsBrowseTab : BrowseTabContribution {

    @Composable
    override fun slot(): BrowseTabSlot {
        // Hoisted for the tab bar's search field
        val extensionsViewModel = metroViewModel<ExtensionsViewModel>()
        val searchQuery by extensionsViewModel.searchQuery.collectAsStateWithLifecycle()
        return BrowseTabSlot(
            content = extensionsTab(extensionsViewModel),
            searchQuery = searchQuery,
            onChangeSearchQuery = extensionsViewModel::search,
        )
    }
}
