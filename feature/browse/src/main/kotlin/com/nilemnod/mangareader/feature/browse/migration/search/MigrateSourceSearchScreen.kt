package com.nilemnod.mangareader.feature.browse.migration.search

import androidx.compose.material3.Icon
import androidx.compose.material3.SmallExtendedFloatingActionButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.animateFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.nilemnod.mangareader.core.navigation.HomeTab
import com.nilemnod.mangareader.core.navigation.HomeTabRouter
import com.nilemnod.mangareader.core.navigation.SharedScreen
import com.nilemnod.mangareader.core.navigation.screen
import com.nilemnod.mangareader.core.ui.components.SearchToolbar
import com.nilemnod.mangareader.core.ui.components.material.Scaffold
import com.nilemnod.mangareader.core.ui.i18n.stringResource
import com.nilemnod.mangareader.core.ui.screens.LoadingScreen
import com.nilemnod.mangareader.core.ui.util.Screen
import com.nilemnod.mangareader.core.ui.util.collectAsLazyPagingItems
import com.nilemnod.mangareader.domain.manga.model.Manga
import com.nilemnod.mangareader.feature.browse.migration.dialog.MigrateMangaDialog
import com.nilemnod.mangareader.feature.browse.migration.list.MigrationListScreen
import com.nilemnod.mangareader.feature.browse.presentation.BrowseSourceContent
import com.nilemnod.mangareader.feature.browse.source.browse.BrowseSourceViewModel
import com.nilemnod.mangareader.feature.browse.source.browse.SourceFilterDialog
import com.nilemnod.mangareader.i18n.MR
import com.nilemnod.mangareader.icons.materialsymbols.MaterialSymbols
import com.nilemnod.mangareader.icons.materialsymbols.rounded.FilterList
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import eu.kanade.tachiyomi.source.online.HttpSource
import kotlinx.coroutines.launch

data class MigrateSourceSearchScreen(
    private val currentManga: Manga,
    private val sourceId: Long,
    private val query: String?,
) : Screen() {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()

        val viewModel =
            assistedMetroViewModel<BrowseSourceViewModel, BrowseSourceViewModel.Factory> {
                create(sourceId = sourceId, listingQuery = query)
            }
        val state by viewModel.state.collectAsState()

        val source = state.source
        if (source == null) {
            LoadingScreen()
            return
        }

        val snackbarHostState = remember { SnackbarHostState() }

        Scaffold(
            topBar = { scrollBehavior ->
                SearchToolbar(
                    searchQuery = state.toolbarQuery ?: "",
                    onChangeSearchQuery = viewModel::setToolbarQuery,
                    onClickCloseSearch = navigator::pop,
                    onSearch = viewModel::search,
                    scrollBehavior = scrollBehavior,
                )
            },
            floatingActionButton = {
                SmallExtendedFloatingActionButton(
                    text = { Text(text = stringResource(MR.strings.action_filter)) },
                    icon = { Icon(MaterialSymbols.Rounded.FilterList, contentDescription = null) },
                    onClick = viewModel::openFilterSheet,
                    modifier = Modifier.animateFloatingActionButton(
                        visible = state.filters.isNotEmpty(),
                        alignment = Alignment.BottomEnd,
                    ),
                )
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        ) { paddingValues ->
            val openMigrateDialog: (Manga) -> Unit = {
                val migrateListScreen = navigator.items
                    .filterIsInstance<MigrationListScreen>()
                    .lastOrNull()

                if (migrateListScreen == null) {
                    viewModel.setDialog(BrowseSourceViewModel.Dialog.Migrate(target = it, current = currentManga))
                } else {
                    migrateListScreen.addMatchOverride(current = currentManga.id, target = it.id)
                    navigator.popUntil { screen -> screen is MigrationListScreen }
                }
            }
            BrowseSourceContent(
                source = source,
                mangaList = viewModel.mangaPagerFlowFlow.collectAsLazyPagingItems(),
                columns = viewModel.getColumnsPreference(LocalConfiguration.current.orientation),
                displayMode = viewModel.displayMode,
                snackbarHostState = snackbarHostState,
                contentPadding = paddingValues,
                onWebViewClick = {
                    val web = source?.web ?: return@BrowseSourceContent
                    navigator.push(
                        SharedScreen.WebView(
                            url = web.homeUrl,
                            initialTitle = source.name,
                            sourceId = source.id,
                        ).screen(),
                    )
                },
                onMangaClick = openMigrateDialog,
                onMangaLongClick = { navigator.push(SharedScreen.Manga(it.id, true).screen()) },
            )
        }

        val onDismissRequest = { viewModel.setDialog(null) }
        when (val dialog = state.dialog) {
            is BrowseSourceViewModel.Dialog.Filter -> {
                SourceFilterDialog(
                    onDismissRequest = onDismissRequest,
                    filters = state.filters,
                    onReset = viewModel::resetFilters,
                    onFilter = { viewModel.search(filters = state.filters) },
                    onUpdate = viewModel::setFilters,
                )
            }
            is BrowseSourceViewModel.Dialog.Migrate -> {
                MigrateMangaDialog(
                    current = currentManga,
                    target = dialog.target,
                    // Initiated from the context of [currentManga] so we show [dialog.target].
                    onClickTitle = { navigator.push(SharedScreen.Manga(dialog.target.id).screen()) },
                    onDismissRequest = onDismissRequest,
                    onComplete = {
                        scope.launch {
                            navigator.popUntilRoot()
                            HomeTabRouter.open(HomeTab.Browse())
                            navigator.push(SharedScreen.Manga(dialog.target.id).screen())
                        }
                    },
                )
            }
            else -> {}
        }
    }
}
