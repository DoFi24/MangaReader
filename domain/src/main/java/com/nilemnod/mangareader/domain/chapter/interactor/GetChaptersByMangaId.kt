package com.nilemnod.mangareader.domain.chapter.interactor

import com.nilemnod.mangareader.core.common.util.system.logcat
import com.nilemnod.mangareader.domain.chapter.model.Chapter
import com.nilemnod.mangareader.domain.chapter.repository.ChapterRepository
import dev.zacsweers.metro.Inject
import logcat.LogPriority

@Inject
class GetChaptersByMangaId(
    private val chapterRepository: ChapterRepository,
) {

    suspend fun await(mangaId: Long, applyScanlatorFilter: Boolean = false): List<Chapter> {
        return try {
            chapterRepository.getChapterByMangaId(mangaId, applyScanlatorFilter)
        } catch (e: Exception) {
            logcat(LogPriority.ERROR, e)
            emptyList()
        }
    }
}
