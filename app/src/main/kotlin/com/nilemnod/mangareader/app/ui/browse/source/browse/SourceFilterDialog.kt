package com.nilemnod.mangareader.app.ui.browse.source.browse

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nilemnod.mangareader.core.common.preference.TriState
import com.nilemnod.mangareader.core.ui.components.AdaptiveSheet
import com.nilemnod.mangareader.core.ui.components.CheckboxItem
import com.nilemnod.mangareader.core.ui.components.CollapsibleBox
import com.nilemnod.mangareader.core.ui.components.HeadingItem
import com.nilemnod.mangareader.core.ui.components.SelectItem
import com.nilemnod.mangareader.core.ui.components.SortItem
import com.nilemnod.mangareader.core.ui.components.TextItem
import com.nilemnod.mangareader.core.ui.components.TriStateItem
import com.nilemnod.mangareader.core.ui.components.material.Button
import com.nilemnod.mangareader.core.ui.i18n.stringResource
import com.nilemnod.mangareader.domain.source.api.FilterList
import com.nilemnod.mangareader.domain.source.api.SourceFilter
import com.nilemnod.mangareader.i18n.MR

@Composable
fun SourceFilterDialog(
    onDismissRequest: () -> Unit,
    filters: FilterList,
    onReset: () -> Unit,
    onFilter: () -> Unit,
    onUpdate: (FilterList) -> Unit,
) {
    AdaptiveSheet(onDismissRequest = onDismissRequest) {
        LazyColumn {
            stickyHeader {
                Row(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.background)
                        .padding(8.dp),
                ) {
                    TextButton(onClick = onReset) {
                        Text(
                            text = stringResource(MR.strings.action_reset),
                            style = LocalTextStyle.current.copy(
                                color = MaterialTheme.colorScheme.primary,
                            ),
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Button(onClick = {
                        onFilter()
                        onDismissRequest()
                    }) {
                        Text(stringResource(MR.strings.action_filter))
                    }
                }
                HorizontalDivider()
            }

            itemsIndexed(filters.filters) { index, filter ->
                FilterItem(filter) { changed ->
                    onUpdate(FilterList(filters.filters.toMutableList().also { it[index] = changed }))
                }
            }
        }
    }
}

@Composable
private fun FilterItem(filter: SourceFilter, onChange: (SourceFilter) -> Unit) {
    when (filter) {
        is SourceFilter.Header -> {
            HeadingItem(filter.name)
        }
        is SourceFilter.Separator -> {
            HorizontalDivider()
        }
        is SourceFilter.CheckBox -> {
            CheckboxItem(
                label = filter.name,
                checked = filter.checked,
            ) {
                onChange(filter.copy(checked = !filter.checked))
            }
        }
        is SourceFilter.TriState -> {
            TriStateItem(
                label = filter.name,
                state = filter.state.toTriState(),
            ) {
                onChange(filter.copy(state = filter.state.toTriState().next().toFilterState()))
            }
        }
        is SourceFilter.Text -> {
            TextItem(
                label = filter.name,
                value = filter.value,
            ) {
                onChange(filter.copy(value = it))
            }
        }
        is SourceFilter.Select -> {
            SelectItem(
                label = filter.name,
                options = filter.values.toTypedArray(),
                selectedIndex = filter.selected,
            ) {
                onChange(filter.copy(selected = it))
            }
        }
        is SourceFilter.Sort -> {
            CollapsibleBox(
                heading = filter.name,
            ) {
                Column {
                    filter.values.mapIndexed { index, item ->
                        val selection = filter.selection
                        val sortAscending = selection?.ascending?.takeIf { index == selection.index }
                        SortItem(
                            label = item,
                            sortDescending = if (sortAscending != null) !sortAscending else null,
                            onClick = {
                                val ascending = if (index == selection?.index) {
                                    !selection.ascending
                                } else {
                                    selection?.ascending ?: true
                                }
                                onChange(filter.copy(selection = SourceFilter.Sort.Selection(index, ascending)))
                            },
                        )
                    }
                }
            }
        }
        is SourceFilter.Group -> {
            CollapsibleBox(
                heading = filter.name,
            ) {
                Column {
                    filter.filters.forEachIndexed { index, child ->
                        FilterItem(child) { changed ->
                            onChange(filter.copy(filters = filter.filters.toMutableList().also { it[index] = changed }))
                        }
                    }
                }
            }
        }
    }
}

private fun SourceFilter.TriState.State.toTriState(): TriState {
    return when (this) {
        SourceFilter.TriState.State.IGNORE -> TriState.DISABLED
        SourceFilter.TriState.State.INCLUDE -> TriState.ENABLED_IS
        SourceFilter.TriState.State.EXCLUDE -> TriState.ENABLED_NOT
    }
}

private fun TriState.toFilterState(): SourceFilter.TriState.State {
    return when (this) {
        TriState.DISABLED -> SourceFilter.TriState.State.IGNORE
        TriState.ENABLED_IS -> SourceFilter.TriState.State.INCLUDE
        TriState.ENABLED_NOT -> SourceFilter.TriState.State.EXCLUDE
    }
}
