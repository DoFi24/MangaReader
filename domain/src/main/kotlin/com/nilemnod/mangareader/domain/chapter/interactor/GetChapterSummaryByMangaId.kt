package com.nilemnod.mangareader.domain.chapter.interactor

import com.nilemnod.mangareader.domain.chapter.model.ChapterSummary
import com.nilemnod.mangareader.domain.chapter.repository.ChapterRepository
import dev.zacsweers.metro.Inject

@Inject
class GetChapterSummaryByMangaId(
    private val chapterRepository: ChapterRepository,
) {

    suspend fun await(mangaId: Long): ChapterSummary {
        return chapterRepository.getChapterSummaryByMangaId(mangaId)
    }
}
