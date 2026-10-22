package com.nilemnod.mangareader.feature.upcoming

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Badge
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.nilemnod.mangareader.app.presentation.components.AppBar
import com.nilemnod.mangareader.app.presentation.components.AppBarActions
import com.nilemnod.mangareader.app.presentation.components.relativeDateText
import com.nilemnod.mangareader.app.presentation.util.isTabletUi
import com.nilemnod.mangareader.domain.manga.model.Manga
import com.nilemnod.mangareader.feature.upcoming.components.UpcomingItem
import com.nilemnod.mangareader.feature.upcoming.components.calendar.Calendar
import com.nilemnod.mangareader.i18n.MR
import com.nilemnod.mangareader.icons.materialsymbols.MaterialSymbols
import com.nilemnod.mangareader.icons.materialsymbols.rounded.FilterList
import com.nilemnod.mangareader.presentation.core.components.FastScrollLazyColumn
import com.nilemnod.mangareader.presentation.core.components.TwoPanelBox
import com.nilemnod.mangareader.presentation.core.components.material.Scaffold
import com.nilemnod.mangareader.presentation.core.components.material.padding
import com.nilemnod.mangareader.presentation.core.i18n.stringResource
import com.nilemnod.mangareader.presentation.core.theme.active
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

@Composable
fun UpcomingScreenContent(
    state: UpcomingViewModel.State,
    setSelectedYearMonth: (YearMonth) -> Unit,
    onClickUpcoming: (manga: Manga) -> Unit,
    onClickFilter: () -> Unit,
    hasActiveFilters: Boolean,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val onClickDay: (LocalDate, Int) -> Unit = { date, offset ->
        state.headerIndexes[date]?.let {
            scope.launch {
                listState.animateScrollToItem(it + offset)
            }
        }
    }
    Scaffold(
        topBar = {
            UpcomingToolbar(
                hasFilters = hasActiveFilters,
                onClickFilter = onClickFilter,
            )
        },
        modifier = modifier,
    ) { paddingValues ->
        if (isTabletUi()) {
            UpcomingScreenLargeImpl(
                listState = listState,
                items = state.items,
                events = state.events,
                paddingValues = paddingValues,
                selectedYearMonth = state.selectedYearMonth,
                setSelectedYearMonth = setSelectedYearMonth,
                onClickDay = { onClickDay(it, 0) },
                onClickUpcoming = onClickUpcoming,
            )
        } else {
            UpcomingScreenSmallImpl(
                listState = listState,
                items = state.items,
                events = state.events,
                paddingValues = paddingValues,
                selectedYearMonth = state.selectedYearMonth,
                setSelectedYearMonth = setSelectedYearMonth,
                onClickDay = { onClickDay(it, 1) },
                onClickUpcoming = onClickUpcoming,
            )
        }
    }
}

@Composable
private fun UpcomingToolbar(
    hasFilters: Boolean,
    onClickFilter: () -> Unit,
) {
    val navigator = LocalNavigator.currentOrThrow

    AppBar(
        title = stringResource(MR.strings.label_upcoming),
        navigateUp = navigator::pop,
        actions = {
            AppBarActions(
                listOf(
                    AppBar.Action(
                        title = stringResource(MR.strings.action_filter),
                        icon = MaterialSymbols.Rounded.FilterList,
                        iconTint = if (hasFilters) MaterialTheme.colorScheme.active else LocalContentColor.current,
                        onClick = onClickFilter,
                    ),
                ),
            )
        },
    )
}

@Composable
private fun DateHeading(
    date: LocalDate,
    mangaCount: Int,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = relativeDateText(date),
            modifier = Modifier
                .padding(MaterialTheme.padding.small)
                .padding(start = MaterialTheme.padding.small),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodyMedium,
        )
        Badge(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ) {
            Text("$mangaCount")
        }
    }
}

@Composable
private fun UpcomingScreenSmallImpl(
    listState: LazyListState,
    items: List<UpcomingUIModel>,
    events: Map<LocalDate, Int>,
    paddingValues: PaddingValues,
    selectedYearMonth: YearMonth,
    setSelectedYearMonth: (YearMonth) -> Unit,
    onClickDay: (LocalDate) -> Unit,
    onClickUpcoming: (manga: Manga) -> Unit,
) {
    FastScrollLazyColumn(
        contentPadding = paddingValues,
        state = listState,
    ) {
        item(key = "upcoming-calendar") {
            Calendar(
                selectedYearMonth = selectedYearMonth,
                events = events,
                setSelectedYearMonth = setSelectedYearMonth,
                onClickDay = onClickDay,
            )
        }
        items(
            items = items,
            key = { "upcoming-${it.hashCode()}" },
            contentType = {
                when (it) {
                    is UpcomingUIModel.Header -> "header"
                    is UpcomingUIModel.Item -> "item"
                }
            },
        ) { item ->
            when (item) {
                is UpcomingUIModel.Item -> {
                    UpcomingItem(
                        upcoming = item.manga,
                        onClick = { onClickUpcoming(item.manga) },
                    )
                }

                is UpcomingUIModel.Header -> {
                    DateHeading(
                        date = item.date,
                        mangaCount = item.mangaCount,
                    )
                }
            }
        }
    }
}

@Composable
private fun UpcomingScreenLargeImpl(
    listState: LazyListState,
    items: List<UpcomingUIModel>,
    events: Map<LocalDate, Int>,
    paddingValues: PaddingValues,
    selectedYearMonth: YearMonth,
    setSelectedYearMonth: (YearMonth) -> Unit,
    onClickDay: (LocalDate) -> Unit,
    onClickUpcoming: (manga: Manga) -> Unit,
) {
    TwoPanelBox(
        modifier = Modifier.padding(paddingValues),
        startContent = {
            Calendar(
                selectedYearMonth = selectedYearMonth,
                events = events,
                setSelectedYearMonth = setSelectedYearMonth,
                onClickDay = onClickDay,
            )
        },
        endContent = {
            FastScrollLazyColumn(state = listState) {
                items(
                    items = items,
                    key = { "upcoming-${it.hashCode()}" },
                    contentType = {
                        when (it) {
                            is UpcomingUIModel.Header -> "header"
                            is UpcomingUIModel.Item -> "item"
                        }
                    },
                ) { item ->
                    when (item) {
                        is UpcomingUIModel.Item -> {
                            UpcomingItem(
                                upcoming = item.manga,
                                onClick = { onClickUpcoming(item.manga) },
                            )
                        }

                        is UpcomingUIModel.Header -> {
                            DateHeading(
                                date = item.date,
                                mangaCount = item.mangaCount,
                            )
                        }
                    }
                }
            }
        },
    )
}
