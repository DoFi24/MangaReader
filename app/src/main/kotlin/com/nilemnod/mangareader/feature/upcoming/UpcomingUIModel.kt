package com.nilemnod.mangareader.feature.upcoming

import com.nilemnod.mangareader.domain.manga.model.Manga
import kotlinx.datetime.LocalDate

sealed interface UpcomingUIModel {
    data class Header(val date: LocalDate, val mangaCount: Int) : UpcomingUIModel
    data class Item(val manga: Manga) : UpcomingUIModel
}
