package com.nilemnod.mangareader.domain.upcoming.interactor

import com.nilemnod.mangareader.domain.manga.model.Manga
import com.nilemnod.mangareader.domain.manga.repository.MangaRepository
import com.nilemnod.mangareader.domain.source.api.MangaStatus
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow

@Inject
class GetUpcomingManga(
    private val mangaRepository: MangaRepository,
) {

    private val includedStatuses = setOf(
        MangaStatus.ONGOING.code,
        MangaStatus.PUBLISHING_FINISHED.code,
    )

    suspend fun subscribe(
        excludedCategories: List<Long>,
        includedCategories: List<Long>,
    ): Flow<List<Manga>> {
        return mangaRepository.getUpcomingManga(
            includedStatuses,
            excludedCategories = excludedCategories,
            includedCategories = includedCategories,
        )
    }
}
