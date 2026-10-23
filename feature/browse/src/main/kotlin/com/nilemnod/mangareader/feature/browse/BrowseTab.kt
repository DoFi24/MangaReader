package com.nilemnod.mangareader.feature.browse

import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.nilemnod.mangareader.core.ui.components.TabbedScreen
import com.nilemnod.mangareader.core.ui.i18n.stringResource
import com.nilemnod.mangareader.core.ui.util.SplashReady
import com.nilemnod.mangareader.core.ui.util.Tab
import com.nilemnod.mangareader.feature.browse.migration.sources.migrateSourceTab
import com.nilemnod.mangareader.feature.browse.source.globalsearch.GlobalSearchScreen
import com.nilemnod.mangareader.feature.browse.source.sourcesTab
import com.nilemnod.mangareader.i18n.MR
import com.nilemnod.mangareader.icons.custommaterialsymbols.CustomMaterialSymbols
import com.nilemnod.mangareader.icons.custommaterialsymbols.edge.Browse
import com.nilemnod.mangareader.icons.custommaterialsymbols.edgefilled.Browse
import kotlinx.coroutines.flow.collectLatest

data object BrowseTab : Tab {

    override val options: TabOptions
        @Composable
        get() {
            val isSelected = LocalTabNavigator.current.current.key == key
            return TabOptions(
                index = 3u,
                title = stringResource(MR.strings.browse),
                icon = rememberVectorPainter(
                    if (isSelected) CustomMaterialSymbols.EdgeFilled.Browse else CustomMaterialSymbols.Edge.Browse,
                ),
            )
        }

    override suspend fun onReselect(navigator: Navigator) {
        navigator.push(GlobalSearchScreen())
    }

    @Composable
    override fun Content() {
        val context = LocalContext.current

        val contributions = remember { BrowseTabs.all() }
        val slots = contributions.map { it.slot() }
        val tabs = buildList {
            add(sourcesTab())
            slots.forEach { add(it.content) }
            add(migrateSourceTab())
        }

        val state = rememberPagerState { tabs.size }
        val currentSlot = slots.getOrNull(state.currentPage - 1)

        TabbedScreen(
            titleRes = MR.strings.browse,
            tabs = tabs,
            state = state,
            searchQuery = currentSlot?.searchQuery,
            onChangeSearchQuery = currentSlot?.onChangeSearchQuery ?: {},
        )
        LaunchedEffect(contributions) {
            BrowseTabs.showEvents.collectLatest { contribution ->
                val index = contributions.indexOf(contribution)
                if (index >= 0) state.scrollToPage(index + 1)
            }
        }

        LaunchedEffect(Unit) {
            (context as? SplashReady)?.ready = true
        }
    }
}
