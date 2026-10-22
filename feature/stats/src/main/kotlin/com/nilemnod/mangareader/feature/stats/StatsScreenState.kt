package com.nilemnod.mangareader.feature.stats

import androidx.compose.runtime.Immutable
import com.nilemnod.mangareader.feature.stats.data.StatsData
sealed interface StatsScreenState {
    @Immutable
    data object Loading : StatsScreenState

    @Immutable
    data class Success(
        val overview: StatsData.Overview,
        val titles: StatsData.Titles,
        val chapters: StatsData.Chapters,
    ) : StatsScreenState
}
