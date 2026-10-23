package com.nilemnod.mangareader.domain.manga.interactor

import com.nilemnod.mangareader.domain.manga.repository.MangaRepository
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow

@Inject
class GetExcludedScanlators(
    private val mangaRepository: MangaRepository,
) {

    suspend fun await(mangaId: Long): Set<String> {
        return mangaRepository.getExcludedScanlators(mangaId)
    }

    fun subscribe(mangaId: Long): Flow<Set<String>> {
        return mangaRepository.getExcludedScanlatorsAsFlow(mangaId)
    }
}
