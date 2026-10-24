package com.nilemnod.mangareader.feature.deeplink

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.nilemnod.mangareader.core.navigation.SharedIntent
import com.nilemnod.mangareader.core.navigation.SharedScreen
import com.nilemnod.mangareader.core.navigation.intent
import com.nilemnod.mangareader.core.navigation.screen
import com.nilemnod.mangareader.core.ui.components.AppBar
import com.nilemnod.mangareader.core.ui.components.material.Scaffold
import com.nilemnod.mangareader.core.ui.i18n.stringResource
import com.nilemnod.mangareader.core.ui.screens.LoadingScreen
import com.nilemnod.mangareader.core.ui.util.Screen
import com.nilemnod.mangareader.i18n.MR
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel

class DeepLinkScreen(
    val query: String = "",
) : Screen() {

    @Composable
    override fun Content() {
        val context = LocalContext.current
        val navigator = LocalNavigator.currentOrThrow

        val viewModel = assistedMetroViewModel<DeepLinkViewModel, DeepLinkViewModel.Factory> { create(query = query) }
        val state by viewModel.state.collectAsState()
        Scaffold(
            topBar = { scrollBehavior ->
                AppBar(
                    title = stringResource(MR.strings.action_search_hint),
                    navigateUp = navigator::pop,
                    scrollBehavior = scrollBehavior,
                )
            },
        ) { contentPadding ->
            when (state) {
                is DeepLinkViewModel.State.Loading -> {
                    LoadingScreen(Modifier.padding(contentPadding))
                }
                is DeepLinkViewModel.State.NoResults -> {
                    navigator.replace(SharedScreen.GlobalSearch(query).screen())
                }
                is DeepLinkViewModel.State.Result -> {
                    val resultState = state as DeepLinkViewModel.State.Result
                    if (resultState.chapterId == null) {
                        navigator.replace(
                            SharedScreen.Manga(resultState.manga.id, true).screen(),
                        )
                    } else {
                        navigator.pop()
                        SharedIntent.Reader(resultState.manga.id, resultState.chapterId)
                            .intent(context)
                            .also(context::startActivity)
                    }
                }
            }
        }
    }
}
