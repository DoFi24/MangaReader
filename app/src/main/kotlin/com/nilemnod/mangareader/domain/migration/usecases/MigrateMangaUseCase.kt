package com.nilemnod.mangareader.domain.migration.usecases

import com.nilemnod.mangareader.app.data.cache.CoverCache
import com.nilemnod.mangareader.app.domain.manga.model.hasCustomCover
import com.nilemnod.mangareader.app.domain.source.service.SourcePreferences
import com.nilemnod.mangareader.app.source.interactor.UpdateMangaFromRemote
import com.nilemnod.mangareader.core.download.DownloadManager
import com.nilemnod.mangareader.core.metro.AppCoroutineScope
import com.nilemnod.mangareader.domain.category.interactor.GetCategories
import com.nilemnod.mangareader.domain.category.interactor.SetMangaCategories
import com.nilemnod.mangareader.domain.chapter.interactor.GetChaptersByMangaId
import com.nilemnod.mangareader.domain.chapter.interactor.UpdateChapter
import com.nilemnod.mangareader.domain.chapter.model.ChapterUpdate
import com.nilemnod.mangareader.domain.manga.interactor.UpdateManga
import com.nilemnod.mangareader.domain.manga.model.Manga
import com.nilemnod.mangareader.domain.manga.model.MangaUpdate
import com.nilemnod.mangareader.domain.migration.models.MigrationFlag
import com.nilemnod.mangareader.domain.source.service.SourceManager
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.time.Clock

@Inject
class MigrateMangaUseCase(
    @AppCoroutineScope private val scope: CoroutineScope,
    private val sourcePreferences: SourcePreferences,
    private val sourceManager: SourceManager,
    private val downloadManager: DownloadManager,
    private val updateManga: UpdateManga,
    private val getChaptersByMangaId: GetChaptersByMangaId,
    private val updateChapter: UpdateChapter,
    private val getCategories: GetCategories,
    private val setMangaCategories: SetMangaCategories,
    private val coverCache: CoverCache,
    private val updateMangaFromRemote: UpdateMangaFromRemote,
) {
    suspend operator fun invoke(current: Manga, target: Manga, replace: Boolean) {
        val targetSource = sourceManager.get(target.source) ?: return
        val currentSource = sourceManager.get(current.source)
        val flags = sourcePreferences.migrationFlags.get()

        try {
            updateMangaFromRemote(target, fetchChapters = true).getOrThrow()

            // Update chapters read, bookmark and dateFetch
            if (MigrationFlag.CHAPTER in flags) {
                val prevMangaChapters = getChaptersByMangaId.await(current.id)
                val mangaChapters = getChaptersByMangaId.await(target.id)

                val maxChapterRead = prevMangaChapters
                    .filter { it.read }
                    .maxOfOrNull { it.chapterNumber }

                val chapterUpdates = mangaChapters
                    .filter { it.isRecognizedNumber }
                    .map { mangaChapter ->
                        val prevChapter = prevMangaChapters
                            .find { it.isRecognizedNumber && it.chapterNumber == mangaChapter.chapterNumber }

                        ChapterUpdate(mangaChapter.id) {
                            if (prevChapter != null) {
                                dateFetch = prevChapter.dateFetch
                                bookmark = prevChapter.bookmark
                            }

                            if (maxChapterRead != null && mangaChapter.chapterNumber <= maxChapterRead) {
                                read = true
                            }
                        }
                    }
                updateChapter.awaitAll(chapterUpdates)
            }

            // Update categories
            if (MigrationFlag.CATEGORY in flags) {
                val categoryIds = getCategories.await(current.id).map { it.id }
                setMangaCategories.await(target.id, categoryIds)
            }

            // Delete downloaded
            if (MigrationFlag.REMOVE_DOWNLOAD in flags && currentSource != null) {
                // Not awaited, so a slow storage delete doesn't hold up the migration
                scope.launch { downloadManager.deleteManga(current, currentSource) }
            }

            // Update custom cover (recheck if custom cover exists)
            if (MigrationFlag.CUSTOM_COVER in flags && current.hasCustomCover()) {
                coverCache.getCustomCoverFile(current.id).inputStream().use {
                    coverCache.setCustomCoverToCache(target, it)
                }
            }

            val currentMangaUpdate = MangaUpdate(current.id) {
                favoriteAt = null
            }
                .takeIf { replace }
            val targetMangaUpdate = MangaUpdate(target.id) {
                favoriteAt = current.favoriteAt?.takeIf { replace } ?: Clock.System.now()
                chapterFlags = current.chapterFlags
                viewerFlags = current.viewerFlags
                if (MigrationFlag.NOTES in flags) notes = current.notes
            }

            updateManga.awaitAll(listOfNotNull(currentMangaUpdate, targetMangaUpdate))
        } catch (e: Throwable) {
            if (e is CancellationException) {
                throw e
            }
        }
    }
}
