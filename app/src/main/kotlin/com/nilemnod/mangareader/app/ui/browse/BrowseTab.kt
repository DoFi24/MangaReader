package com.nilemnod.mangareader.app.ui.browse

import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.nilemnod.mangareader.app.R
import com.nilemnod.mangareader.app.ui.browse.extension.ExtensionsViewModel
import com.nilemnod.mangareader.app.ui.browse.extension.extensionsTab
import com.nilemnod.mangareader.app.ui.browse.migration.sources.migrateSourceTab
import com.nilemnod.mangareader.app.ui.browse.source.globalsearch.GlobalSearchScreen
import com.nilemnod.mangareader.app.ui.browse.source.sourcesTab
import com.nilemnod.mangareader.app.ui.main.MainActivity
import com.nilemnod.mangareader.core.ui.components.TabbedScreen
import com.nilemnod.mangareader.core.ui.i18n.stringResource
import com.nilemnod.mangareader.core.ui.util.Tab
import com.nilemnod.mangareader.core.ui.util.rememberAnimatedVectorPainter
import com.nilemnod.mangareader.i18n.MR
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow

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

    private val switchToExtensionTabChannel = Channel<Unit>(1, BufferOverflow.DROP_OLDEST)

    fun showExtension() {
        switchToExtensionTabChannel.trySend(Unit)
    }

    @Composable
    override fun Content() {
        val context = LocalContext.current

        // Hoisted for extensions tab's search bar
        val extensionsViewModel = metroViewModel<ExtensionsViewModel>()
        val extensionsSearchQuery by extensionsViewModel.searchQuery.collectAsStateWithLifecycle()

        val tabs = listOf(
            sourcesTab(),
            extensionsTab(extensionsViewModel),
            migrateSourceTab(),
        )

        val state = rememberPagerState { tabs.size }

        TabbedScreen(
            titleRes = MR.strings.browse,
            tabs = tabs,
            state = state,
            searchQuery = extensionsSearchQuery,
            onChangeSearchQuery = extensionsViewModel::search,
        )
        LaunchedEffect(Unit) {
            switchToExtensionTabChannel.receiveAsFlow()
                .collectLatest { state.scrollToPage(1) }
        }

        LaunchedEffect(Unit) {
            (context as? MainActivity)?.ready = true
        }
    }
}
