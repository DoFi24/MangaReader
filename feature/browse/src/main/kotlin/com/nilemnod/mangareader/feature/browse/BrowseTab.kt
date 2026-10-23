package com.nilemnod.mangareader.feature.browse

import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.nilemnod.mangareader.core.ui.components.TabbedScreen
import com.nilemnod.mangareader.core.ui.i18n.stringResource
import com.nilemnod.mangareader.core.ui.util.SplashReady
import com.nilemnod.mangareader.core.ui.util.Tab
import com.nilemnod.mangareader.core.ui.util.rememberAnimatedVectorPainter
import com.nilemnod.mangareader.feature.browse.R
import com.nilemnod.mangareader.feature.browse.migration.sources.migrateSourceTab
import com.nilemnod.mangareader.feature.browse.source.globalsearch.GlobalSearchScreen
import com.nilemnod.mangareader.feature.browse.source.sourcesTab
import com.nilemnod.mangareader.i18n.MR
import kotlinx.coroutines.flow.collectLatest

data object BrowseTab : Tab {

    override val options: TabOptions
        @Composable
        get() {
            val isSelected = LocalTabNavigator.current.current.key == key
            val enter = AnimatedImageVector.animatedVectorResource(R.drawable.animated_browse_enter)
            val exit = AnimatedImageVector.animatedVectorResource(R.drawable.animated_browse_exit)
            return TabOptions(
                index = 3u,
                title = stringResource(MR.strings.browse),
                icon = rememberAnimatedVectorPainter(enter, exit, isSelected),
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
