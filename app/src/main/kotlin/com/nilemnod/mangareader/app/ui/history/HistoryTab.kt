package com.nilemnod.mangareader.app.ui.history

import android.content.Context
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.nilemnod.mangareader.app.R
import com.nilemnod.mangareader.app.di.appGraph
import com.nilemnod.mangareader.app.presentation.history.HistoryScreen
import com.nilemnod.mangareader.app.presentation.history.components.HistoryDeleteAllDialog
import com.nilemnod.mangareader.app.presentation.history.components.HistoryDeleteDialog
import com.nilemnod.mangareader.app.ui.main.MainActivity
import com.nilemnod.mangareader.app.ui.manga.MangaScreen
import com.nilemnod.mangareader.app.ui.reader.ReaderActivity
import com.nilemnod.mangareader.core.common.i18n.stringResource
import com.nilemnod.mangareader.core.mangaui.ChangeCategoryDialog
import com.nilemnod.mangareader.core.mangaui.DuplicateMangaDialog
import com.nilemnod.mangareader.core.navigation.SharedIntent
import com.nilemnod.mangareader.core.navigation.SharedScreen
import com.nilemnod.mangareader.core.navigation.intent
import com.nilemnod.mangareader.core.navigation.screen
import com.nilemnod.mangareader.core.ui.i18n.stringResource
import com.nilemnod.mangareader.core.ui.util.Tab
import com.nilemnod.mangareader.domain.chapter.model.Chapter
import com.nilemnod.mangareader.feature.categories.CategoryScreen
import com.nilemnod.mangareader.feature.migration.dialog.MigrateMangaDialog
import com.nilemnod.mangareader.i18n.MR
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow

data object HistoryTab : Tab {

    private val snackbarHostState = SnackbarHostState()

    private val resumeLastChapterReadEvent = Channel<Unit>()

    override val options: TabOptions
        @Composable
        get() {
            val isSelected = LocalTabNavigator.current.current.key == key
            val image = AnimatedImageVector.animatedVectorResource(R.drawable.animated_history)
            return TabOptions(
                index = 2u,
                title = stringResource(MR.strings.label_recent_manga),
                icon = rememberAnimatedVectorPainter(image, isSelected),
            )
        }

    override suspend fun onReselect(navigator: Navigator) {
        resumeLastChapterReadEvent.send(Unit)
    }

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val context = LocalContext.current
        val viewModel = metroViewModel<HistoryViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()

        HistoryScreen(
            state = state,
            snackbarHostState = snackbarHostState,
            onSearchQueryChange = viewModel::updateSearchQuery,
            onClickCover = { navigator.push(SharedScreen.Manga(it).screen()) },
            onClickResume = viewModel::getNextChapterForManga,
            onDialogChange = viewModel::setDialog,
            onClickFavorite = viewModel::addFavorite,
        )

        val onDismissRequest = { viewModel.setDialog(null) }
        when (val dialog = state.dialog) {
            is HistoryViewModel.Dialog.Delete -> {
                HistoryDeleteDialog(
                    onDismissRequest = onDismissRequest,
                    onDelete = { all ->
                        if (all) {
                            viewModel.removeAllFromHistory(dialog.history.mangaId)
                        } else {
                            viewModel.removeFromHistory(dialog.history)
                        }
                    },
                )
            }
            is HistoryViewModel.Dialog.DeleteAll -> {
                HistoryDeleteAllDialog(
                    onDismissRequest = onDismissRequest,
                    onDelete = viewModel::removeAllHistory,
                )
            }
            is HistoryViewModel.Dialog.DuplicateManga -> {
                DuplicateMangaDialog(
                    duplicates = dialog.duplicates,
                    onDismissRequest = onDismissRequest,
                    onConfirm = { viewModel.addFavorite(dialog.manga) },
                    onOpenManga = { navigator.push(SharedScreen.Manga(it.id).screen()) },
                    onMigrate = { viewModel.showMigrateDialog(dialog.manga, it) },
                    sourceManager = LocalContext.current.appGraph.sourceManager,
                )
            }
            is HistoryViewModel.Dialog.ChangeCategory -> {
                ChangeCategoryDialog(
                    initialSelection = dialog.initialSelection,
                    onDismissRequest = onDismissRequest,
                    onEditCategories = { navigator.push(CategoryScreen()) },
                    onConfirm = { include, _ ->
                        viewModel.moveMangaToCategoriesAndAddToLibrary(dialog.manga, include)
                    },
                )
            }
            is HistoryViewModel.Dialog.Migrate -> {
                MigrateMangaDialog(
                    current = dialog.current,
                    target = dialog.target,
                    // Initiated from the context of [dialog.target] so we show [dialog.current].
                    onClickTitle = { navigator.push(SharedScreen.Manga(dialog.current.id).screen()) },
                    onDismissRequest = onDismissRequest,
                )
            }
            null -> {}
        }

        LaunchedEffect(state.list) {
            if (state.list != null) {
                (context as? MainActivity)?.ready = true
            }
        }

        LaunchedEffect(Unit) {
            viewModel.events.collectLatest { e ->
                when (e) {
                    HistoryViewModel.Event.InternalError ->
                        snackbarHostState.showSnackbar(context.stringResource(MR.strings.internal_error))
                    HistoryViewModel.Event.HistoryCleared ->
                        snackbarHostState.showSnackbar(context.stringResource(MR.strings.clear_history_completed))
                    is HistoryViewModel.Event.OpenChapter -> openChapter(context, e.chapter)
                }
            }
        }

        LaunchedEffect(Unit) {
            resumeLastChapterReadEvent.receiveAsFlow().collectLatest {
                openChapter(context, viewModel.getNextChapter())
            }
        }
    }

    private suspend fun openChapter(context: Context, chapter: Chapter?) {
        if (chapter != null) {
            val intent = SharedIntent.Reader(chapter.mangaId, chapter.id).intent(context)
            context.startActivity(intent)
        } else {
            snackbarHostState.showSnackbar(context.stringResource(MR.strings.no_next_chapter))
        }
    }
}
