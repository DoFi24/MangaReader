package com.nilemnod.mangareader.feature.updates

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.util.fastAll
import androidx.compose.ui.util.fastAny
import com.nilemnod.mangareader.core.mangaui.ChapterDownloadAction
import com.nilemnod.mangareader.core.mangaui.MangaBottomActionMenu
import com.nilemnod.mangareader.core.ui.components.AppBar
import com.nilemnod.mangareader.core.ui.components.AppBarActions
import com.nilemnod.mangareader.core.ui.components.FastScrollLazyColumn
import com.nilemnod.mangareader.core.ui.components.material.PullRefresh
import com.nilemnod.mangareader.core.ui.components.material.Scaffold
import com.nilemnod.mangareader.core.ui.i18n.stringResource
import com.nilemnod.mangareader.core.ui.screens.EmptyIllustration
import com.nilemnod.mangareader.core.ui.screens.EmptyScreen
import com.nilemnod.mangareader.core.ui.screens.EmptyScreenAction
import com.nilemnod.mangareader.core.ui.screens.ListSkeleton
import com.nilemnod.mangareader.domain.download.model.Download
import com.nilemnod.mangareader.i18n.MR
import com.nilemnod.mangareader.icons.custommaterialsymbols.CustomMaterialSymbols
import com.nilemnod.mangareader.icons.custommaterialsymbols.edge.Filter
import com.nilemnod.mangareader.icons.custommaterialsymbols.edge.InvertSelection
import com.nilemnod.mangareader.icons.custommaterialsymbols.edge.Refresh
import com.nilemnod.mangareader.icons.custommaterialsymbols.edge.SelectAll
import com.nilemnod.mangareader.icons.materialsymbols.MaterialSymbols
import com.nilemnod.mangareader.icons.materialsymbols.rounded.CalendarMonth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlin.time.Duration.Companion.seconds

@Composable
fun UpdateScreen(
    state: UpdatesViewModel.State,
    snackbarHostState: SnackbarHostState,
    lastUpdated: Long,
    onClickCover: (UpdatesItem) -> Unit,
    onSelectAll: (Boolean) -> Unit,
    onInvertSelection: () -> Unit,
    onCalendarClicked: () -> Unit,
    onUpdateLibrary: suspend () -> Boolean,
    onDownloadChapter: (List<UpdatesItem>, ChapterDownloadAction) -> Unit,
    onMultiBookmarkClicked: (List<UpdatesItem>, bookmark: Boolean) -> Unit,
    onMultiMarkAsReadClicked: (List<UpdatesItem>, read: Boolean) -> Unit,
    onMultiDeleteClicked: (List<UpdatesItem>) -> Unit,
    onUpdateSelected: (UpdatesItem, Boolean, Boolean) -> Unit,
    onOpenChapter: (UpdatesItem) -> Unit,
    onFilterClicked: () -> Unit,
    hasActiveFilters: Boolean,
) {
    BackHandler(enabled = state.selectionMode) {
        onSelectAll(false)
    }

    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = { scrollBehavior ->
            UpdatesAppBar(
                onCalendarClicked = { onCalendarClicked() },
                onUpdateLibrary = { scope.launch { onUpdateLibrary() } },
                onFilterClicked = { onFilterClicked() },
                hasFilters = hasActiveFilters,
                actionModeCounter = state.selected.size,
                onSelectAll = { onSelectAll(true) },
                onInvertSelection = { onInvertSelection() },
                onCancelActionMode = { onSelectAll(false) },
                scrollBehavior = scrollBehavior,
            )
        },
        bottomBar = {
            UpdatesBottomBar(
                selected = state.selected,
                onDownloadChapter = onDownloadChapter,
                onMultiBookmarkClicked = onMultiBookmarkClicked,
                onMultiMarkAsReadClicked = onMultiMarkAsReadClicked,
                onMultiDeleteClicked = onMultiDeleteClicked,
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { contentPadding ->
        when {
            state.isLoading -> ListSkeleton(Modifier.padding(contentPadding))
            else -> {
                var isRefreshing by remember { mutableStateOf(false) }

                PullRefresh(
                    refreshing = isRefreshing,
                    onRefresh = {
                        scope.launch {
                            if (!onUpdateLibrary()) return@launch
                            // Fake refresh status but hide it after a second as it's a long running task
                            isRefreshing = true
                            delay(1.seconds)
                            isRefreshing = false
                        }
                    },
                    enabled = !state.selectionMode,
                    indicatorPadding = contentPadding,
                ) {
                    if (state.items.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .padding(contentPadding)
                                .fillMaxSize(),
                        ) {
                            UpdatesLastUpdatedHeader(lastUpdated = lastUpdated)
                            if (hasActiveFilters) {
                                EmptyScreen(
                                    stringRes = MR.strings.error_no_match,
                                    modifier = Modifier.weight(1f),
                                )
                            } else {
                                EmptyScreen(
                                    stringRes = MR.strings.empty_updates_text,
                                    title = stringResource(MR.strings.empty_updates_title),
                                    illustration = EmptyIllustration.NoUpdates,
                                    modifier = Modifier.weight(1f),
                                    actions = listOf(
                                        EmptyScreenAction(
                                            stringRes = MR.strings.action_update_library,
                                            icon = CustomMaterialSymbols.Edge.Refresh,
                                            onClick = { scope.launch { onUpdateLibrary() } },
                                        ),
                                    ),
                                )
                            }
                        }
                    } else {
                        FastScrollLazyColumn(
                            contentPadding = contentPadding,
                        ) {
                            updatesLastUpdatedItem(lastUpdated)

                            updatesUiItems(
                                uiModels = state.getUiModel(),
                                selectionMode = state.selectionMode,
                                onUpdateSelected = onUpdateSelected,
                                onClickCover = onClickCover,
                                onClickUpdate = onOpenChapter,
                                onDownloadChapter = onDownloadChapter,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UpdatesAppBar(
    onCalendarClicked: () -> Unit,
    onUpdateLibrary: () -> Unit,
    onFilterClicked: () -> Unit,
    hasFilters: Boolean,
    // For action mode
    actionModeCounter: Int,
    onSelectAll: () -> Unit,
    onInvertSelection: () -> Unit,
    onCancelActionMode: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
    modifier: Modifier = Modifier,
) {
    AppBar(
        modifier = modifier,
        title = stringResource(MR.strings.label_recent_updates),
        actions = {
            AppBarActions(
                listOf(
                    AppBar.Action(
                        title = stringResource(MR.strings.action_filter),
                        icon = CustomMaterialSymbols.Edge.Filter,
                        iconTint = if (hasFilters) MaterialTheme.colorScheme.primary else LocalContentColor.current,
                        onClick = onFilterClicked,
                    ),
                    AppBar.Action(
                        title = stringResource(MR.strings.action_view_upcoming),
                        icon = MaterialSymbols.Rounded.CalendarMonth,
                        onClick = onCalendarClicked,
                    ),
                    AppBar.Action(
                        title = stringResource(MR.strings.action_update_library),
                        icon = CustomMaterialSymbols.Edge.Refresh,
                        onClick = onUpdateLibrary,
                    ),
                ),
            )
        },
        actionModeCounter = actionModeCounter,
        onCancelActionMode = onCancelActionMode,
        actionModeActions = {
            AppBarActions(
                listOf(
                    AppBar.Action(
                        title = stringResource(MR.strings.action_select_all),
                        icon = CustomMaterialSymbols.Edge.SelectAll,
                        onClick = onSelectAll,
                    ),
                    AppBar.Action(
                        title = stringResource(MR.strings.action_select_inverse),
                        icon = CustomMaterialSymbols.Edge.InvertSelection,
                        onClick = onInvertSelection,
                    ),
                ),
            )
        },
        scrollBehavior = scrollBehavior,
    )
}

@Composable
private fun UpdatesBottomBar(
    selected: List<UpdatesItem>,
    onDownloadChapter: (List<UpdatesItem>, ChapterDownloadAction) -> Unit,
    onMultiBookmarkClicked: (List<UpdatesItem>, bookmark: Boolean) -> Unit,
    onMultiMarkAsReadClicked: (List<UpdatesItem>, read: Boolean) -> Unit,
    onMultiDeleteClicked: (List<UpdatesItem>) -> Unit,
) {
    MangaBottomActionMenu(
        visible = selected.isNotEmpty(),
        modifier = Modifier.fillMaxWidth(),
        onBookmarkClicked = {
            onMultiBookmarkClicked.invoke(selected, true)
        }.takeIf { selected.fastAny { !it.update.bookmark } },
        onRemoveBookmarkClicked = {
            onMultiBookmarkClicked.invoke(selected, false)
        }.takeIf { selected.fastAll { it.update.bookmark } },
        onMarkAsReadClicked = {
            onMultiMarkAsReadClicked(selected, true)
        }.takeIf { selected.fastAny { !it.update.read } },
        onMarkAsUnreadClicked = {
            onMultiMarkAsReadClicked(selected, false)
        }.takeIf { selected.fastAny { it.update.read || it.update.lastPageRead > 0 } },
        onDownloadClicked = {
            onDownloadChapter(selected, ChapterDownloadAction.START)
        }.takeIf {
            selected.fastAny { it.downloadStateProvider() != Download.State.DOWNLOADED }
        },
        onDeleteClicked = {
            onMultiDeleteClicked(selected)
        }.takeIf { selected.fastAny { it.downloadStateProvider() == Download.State.DOWNLOADED } },
    )
}

sealed interface UpdatesUiModel {
    data class Header(val date: LocalDate) : UpdatesUiModel
    data class Item(val item: UpdatesItem) : UpdatesUiModel
}
