package com.nilemnod.mangareader.compat.tachiyomi

import com.nilemnod.mangareader.domain.source.api.SourceFilter
import eu.kanade.tachiyomi.source.model.Filter
import eu.kanade.tachiyomi.source.model.FilterList
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class FilterMappingTest {

    private class Genre(name: String) : Filter.TriState(name)

    private class Genres(genres: List<Genre>) : Filter.Group<Genre>("Genres", genres)

    private class Order : Filter.Select<String>("Order", arrayOf("Popular", "New"))

    private class Title : Filter.Text("Title")

    private class Adult : Filter.CheckBox("Adult")

    private class Sorting : Filter.Sort("Sort", arrayOf("Name", "Date"))

    private fun nativeFilters() = FilterList(
        Filter.Header("Header"),
        Title(),
        Order(),
        Adult(),
        Filter.Separator(),
        Genres(listOf(Genre("Action"), Genre("Drama"))),
        Sorting(),
    )

    @Test
    fun `maps native filters to own model without headers losing order`() {
        val own = nativeFilters().toSource()

        own.map { it::class } shouldBe listOf(
            SourceFilter.Header::class,
            SourceFilter.Text::class,
            SourceFilter.Select::class,
            SourceFilter.CheckBox::class,
            SourceFilter.Separator::class,
            SourceFilter.Group::class,
            SourceFilter.Sort::class,
        )
        (own[2] as SourceFilter.Select).values shouldBe listOf("Popular", "New")
    }

    @Test
    fun `applies own state back to native filters`() {
        val native = nativeFilters()
        val own = native.toSource().filters.map { filter ->
            when (filter) {
                is SourceFilter.Text -> filter.copy(value = "berserk")
                is SourceFilter.Select -> filter.copy(selected = 1)
                is SourceFilter.CheckBox -> filter.copy(checked = true)
                is SourceFilter.Group -> filter.copy(
                    filters = filter.filters.mapIndexed { index, genre ->
                        (genre as SourceFilter.TriState).copy(
                            state = if (index == 0) {
                                SourceFilter.TriState.State.INCLUDE
                            } else {
                                SourceFilter.TriState.State.EXCLUDE
                            },
                        )
                    },
                )
                is SourceFilter.Sort -> filter.copy(selection = SourceFilter.Sort.Selection(1, ascending = false))
                else -> filter
            }
        }

        native.applyFrom(own)

        (native.list[1] as Filter.Text).state shouldBe "berserk"
        (native.list[2] as Filter.Select<*>).state shouldBe 1
        (native.list[3] as Filter.CheckBox).state shouldBe true
        val genres = (native.list[5] as Filter.Group<*>).state.filterIsInstance<Filter.TriState>()
        genres.map { it.state } shouldBe listOf(Filter.TriState.STATE_INCLUDE, Filter.TriState.STATE_EXCLUDE)
        (native.list[6] as Filter.Sort).state shouldBe Filter.Sort.Selection(1, false)
    }
}
