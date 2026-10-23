package com.nilemnod.mangareader.domain.source.api

class FilterList(val filters: List<SourceFilter> = emptyList()) : List<SourceFilter> by filters

sealed interface SourceFilter {

    val name: String

    data class Header(override val name: String) : SourceFilter

    data class Separator(override val name: String = "") : SourceFilter

    data class Select(
        override val name: String,
        val values: List<String>,
        val selected: Int = 0,
    ) : SourceFilter

    data class Text(
        override val name: String,
        val value: String = "",
    ) : SourceFilter

    data class CheckBox(
        override val name: String,
        val checked: Boolean = false,
    ) : SourceFilter

    data class TriState(
        override val name: String,
        val state: State = State.IGNORE,
    ) : SourceFilter {

        enum class State { IGNORE, INCLUDE, EXCLUDE }
    }

    data class Group(
        override val name: String,
        val filters: List<SourceFilter>,
    ) : SourceFilter

    data class Sort(
        override val name: String,
        val values: List<String>,
        val selection: Selection? = null,
    ) : SourceFilter {

        data class Selection(val index: Int, val ascending: Boolean)
    }
}
