package com.nilemnod.mangareader.app.source.compat

import com.nilemnod.mangareader.domain.source.api.FilterList
import com.nilemnod.mangareader.domain.source.api.SourceFilter
import eu.kanade.tachiyomi.source.model.Filter
import eu.kanade.tachiyomi.source.model.FilterList as NativeFilterList

internal fun NativeFilterList.toSource(): FilterList = FilterList(list.mapNotNull { it.toSource() })

private fun Filter<*>.toSource(): SourceFilter? = when (this) {
    is Filter.Header -> SourceFilter.Header(name)
    is Filter.Separator -> SourceFilter.Separator(name)
    is Filter.Select<*> -> SourceFilter.Select(name, values.map { it.toString() }, state)
    is Filter.Text -> SourceFilter.Text(name, state)
    is Filter.CheckBox -> SourceFilter.CheckBox(name, state)
    is Filter.TriState -> SourceFilter.TriState(
        name = name,
        state = when (state) {
            Filter.TriState.STATE_INCLUDE -> SourceFilter.TriState.State.INCLUDE
            Filter.TriState.STATE_EXCLUDE -> SourceFilter.TriState.State.EXCLUDE
            else -> SourceFilter.TriState.State.IGNORE
        },
    )
    is Filter.Group<*> -> SourceFilter.Group(name, state.filterIsInstance<Filter<*>>().mapNotNull { it.toSource() })
    is Filter.Sort -> SourceFilter.Sort(
        name = name,
        values = values.toList(),
        selection = state?.let { SourceFilter.Sort.Selection(it.index, it.ascending) },
    )
}

internal fun NativeFilterList.applyFrom(filters: List<SourceFilter>) {
    list.applyFrom(filters)
}

@Suppress("UNCHECKED_CAST")
private fun List<Filter<*>>.applyFrom(filters: List<SourceFilter>) {
    val own = filters.filterNot { it is SourceFilter.Header || it is SourceFilter.Separator }
    val native = filterNot { it is Filter.Header || it is Filter.Separator }
    native.zip(own).forEach { (filter, value) ->
        when {
            filter is Filter.Select<*> && value is SourceFilter.Select -> filter.state = value.selected
            filter is Filter.Text && value is SourceFilter.Text -> filter.state = value.value
            filter is Filter.CheckBox && value is SourceFilter.CheckBox -> filter.state = value.checked
            filter is Filter.TriState && value is SourceFilter.TriState -> filter.state = when (value.state) {
                SourceFilter.TriState.State.INCLUDE -> Filter.TriState.STATE_INCLUDE
                SourceFilter.TriState.State.EXCLUDE -> Filter.TriState.STATE_EXCLUDE
                SourceFilter.TriState.State.IGNORE -> Filter.TriState.STATE_IGNORE
            }
            filter is Filter.Group<*> && value is SourceFilter.Group ->
                (filter.state as List<*>).filterIsInstance<Filter<*>>().applyFrom(value.filters)
            filter is Filter.Sort && value is SourceFilter.Sort ->
                filter.state = value.selection?.let { Filter.Sort.Selection(it.index, it.ascending) }
        }
    }
}
