package com.nilemnod.mangareader.app.util.chapter

import com.nilemnod.mangareader.core.download.DownloadManager
import com.nilemnod.mangareader.domain.base.service.ModePreferences
import com.nilemnod.mangareader.domain.chapter.interactor.GetChaptersByMangaId
import com.nilemnod.mangareader.domain.chapter.model.Chapter
import com.nilemnod.mangareader.domain.chapter.service.NextUnreadChapterProvider
import com.nilemnod.mangareader.domain.manga.model.Manga
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Inject
@ContributesBinding(AppScope::class)
class FilteredNextUnreadChapterProvider(
    private val getChaptersByMangaId: GetChaptersByMangaId,
    private val downloadManager: DownloadManager,
    private val modePreferences: ModePreferences,
) : NextUnreadChapterProvider {

    override suspend fun get(manga: Manga): Chapter? {
        val chapters = getChaptersByMangaId.await(manga.id, applyScanlatorFilter = true)
        return withContext(Dispatchers.Default) { chapters.getNextUnread(manga, downloadManager, modePreferences.downloadedOnly.get()) }
    }
}
