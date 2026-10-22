package com.nilemnod.mangareader.domain.manga.interactor

import dev.zacsweers.metro.Inject
import com.nilemnod.mangareader.domain.manga.model.MangaUpdate
import com.nilemnod.mangareader.domain.manga.repository.MangaRepository

@Inject
class UpdateMangaNotes(
    private val mangaRepository: MangaRepository,
) {

    suspend operator fun invoke(mangaId: Long, notes: String): Boolean {
        return mangaRepository.update(
            MangaUpdate(mangaId) {
                this.notes = notes
            },
        )
    }
}
