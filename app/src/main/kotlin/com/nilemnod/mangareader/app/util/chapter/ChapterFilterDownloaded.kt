package com.nilemnod.mangareader.app.util.chapter

import com.nilemnod.mangareader.core.download.DownloadCache
import com.nilemnod.mangareader.domain.chapter.model.Chapter
import com.nilemnod.mangareader.domain.manga.model.Manga
import com.nilemnod.mangareader.source.local.isLocal

/**
 * Returns a copy of the list with not downloaded chapters removed.
 */
fun List<Chapter>.filterDownloaded(manga: Manga, downloadCache: DownloadCache): List<Chapter> {
    if (manga.isLocal()) return this

    return filter { downloadCache.isChapterDownloaded(it.name, it.scanlator, it.url, manga.title, manga.source) }
}
