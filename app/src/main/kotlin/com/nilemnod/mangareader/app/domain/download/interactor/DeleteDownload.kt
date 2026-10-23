package com.nilemnod.mangareader.app.domain.download.interactor

import com.nilemnod.mangareader.core.download.DownloadManager
import com.nilemnod.mangareader.domain.chapter.model.Chapter
import com.nilemnod.mangareader.domain.manga.model.Manga
import com.nilemnod.mangareader.domain.source.service.SourceManager
import dev.zacsweers.metro.Inject

@Inject
class DeleteDownload(
    private val sourceManager: SourceManager,
    private val downloadManager: DownloadManager,
) {

    suspend fun awaitAll(manga: Manga, vararg chapters: Chapter) {
        sourceManager.get(manga.source)?.let { source ->
            downloadManager.deleteChapters(chapters.toList(), manga, source)
        }
    }
}
