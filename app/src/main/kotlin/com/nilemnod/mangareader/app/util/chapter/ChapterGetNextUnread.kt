package com.nilemnod.mangareader.app.util.chapter

import com.nilemnod.mangareader.app.domain.chapter.model.applyFilters
import com.nilemnod.mangareader.core.download.DownloadManager
import com.nilemnod.mangareader.domain.chapter.model.Chapter
import com.nilemnod.mangareader.domain.manga.model.Manga

/**
 * Gets next unread chapter with filters and sorting applied
 */
fun List<Chapter>.getNextUnread(
    manga: Manga,
    downloadManager: DownloadManager,
    downloadedOnly: Boolean,
): Chapter? {
    return applyFilters(manga, downloadManager, downloadedOnly).let { chapters ->
        if (manga.sortDescending()) {
            chapters.findLast { !it.read }
        } else {
            chapters.find { !it.read }
        }
    }
}
