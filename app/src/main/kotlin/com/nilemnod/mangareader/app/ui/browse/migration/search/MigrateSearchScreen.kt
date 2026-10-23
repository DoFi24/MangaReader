package com.nilemnod.mangareader.app.ui.browse.migration.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.nilemnod.mangareader.app.presentation.browse.MigrateSearchScreen
import com.nilemnod.mangareader.app.ui.browse.source.globalsearch.SearchViewModel
import com.nilemnod.mangareader.core.navigation.MangaDestination
import com.nilemnod.mangareader.core.navigation.SharedScreen
import com.nilemnod.mangareader.core.navigation.screen
import com.nilemnod.mangareader.core.ui.util.Screen
import com.nilemnod.mangareader.feature.migration.dialog.MigrateMangaDialog
import com.nilemnod.mangareader.feature.migration.list.MigrationListScreen
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel

class MigrateSearchScreen(private val mangaId: Long) : Screen() {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        val viewModel =
            assistedMetroViewModel<MigrateSearchViewModel, MigrateSearchViewModel.Factory> { create(mangaId = mangaId) }
        val state by viewModel.state.collectAsState()

        MigrateSearchScreen(
            state = state,
            fromSourceId = state.from?.source,
            navigateUp = navigator::pop,
            onChangeSearchQuery = viewModel::updateSearchQuery,
            onSearch = { viewModel.search() },
            getManga = { viewModel.getManga(it) },
            onChangeSearchFilter = viewModel::setSourceFilter,
            onToggleResults = viewModel::toggleFilterResults,
            onClickSource = { navigator.push(MigrateSourceSearchScreen(state.from!!, it.id, state.searchQuery)) },
            onClickItem = {
                val migrateListScreen = navigator.items
                    .filterIsInstance<MigrationListScreen>()
                    .lastOrNull()

                if (migrateListScreen == null) {
                    viewModel.setMigrateDialog(mangaId, it)
                } else {
                    migrateListScreen.addMatchOverride(current = mangaId, target = it.id)
                    navigator.popUntil { screen -> screen is MigrationListScreen }
                }
            },
            onLongClickItem = { navigator.push(SharedScreen.Manga(it.id, true).screen()) },
        )

        when (val dialog = state.dialog) {
            is SearchViewModel.Dialog.Migrate -> {
                MigrateMangaDialog(
                    current = dialog.current,
                    target = dialog.target,
                    // Initiated from the context of [dialog.current] so we show [dialog.target].
                    onClickTitle = { navigator.push(SharedScreen.Manga(dialog.target.id, true).screen()) },
                    onDismissRequest = { viewModel.clearDialog() },
                    onComplete = {
                        if (navigator.lastItem is MangaDestination) {
                            val lastItem = navigator.lastItem
                            navigator.popUntil { navigator.items.contains(lastItem) }
                            navigator.push(SharedScreen.Manga(dialog.target.id).screen())
                        } else {
                            navigator.replace(SharedScreen.Manga(dialog.target.id).screen())
                        }
                    },
                )
            }
            else -> {}
        }
    }
}
