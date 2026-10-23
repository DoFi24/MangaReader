package com.nilemnod.mangareader.app.source.interactor

import com.nilemnod.mangareader.app.domain.chapter.interactor.SyncChaptersWithSource
import com.nilemnod.mangareader.core.common.util.system.logcat
import com.nilemnod.mangareader.core.download.DownloadManager
import com.nilemnod.mangareader.core.storage.CoverCache
import com.nilemnod.mangareader.domain.chapter.model.toSourceChapter
import com.nilemnod.mangareader.domain.chapter.repository.ChapterRepository
import com.nilemnod.mangareader.domain.library.service.LibraryPreferences
import com.nilemnod.mangareader.domain.manga.model.Manga
import com.nilemnod.mangareader.domain.manga.model.MangaRemoteUpdate
import com.nilemnod.mangareader.domain.manga.model.toSourceManga
import com.nilemnod.mangareader.domain.manga.repository.MangaRepository
import com.nilemnod.mangareader.domain.source.api.ContentSource
import com.nilemnod.mangareader.domain.source.api.SourceManga
import com.nilemnod.mangareader.domain.source.models.RemoteMangaUpdate
import com.nilemnod.mangareader.domain.source.service.RemoteMangaUpdater
import com.nilemnod.mangareader.domain.source.service.SourceManager
import com.nilemnod.mangareader.source.local.isLocal
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import logcat.LogPriority
import kotlin.time.Clock
import kotlin.time.Instant

@Inject
@ContributesBinding(AppScope::class)
class UpdateMangaFromRemote(
    private val sourceManager: SourceManager,
    private val chapterRepository: ChapterRepository,
    private val mangaRepository: MangaRepository,
    private val syncChaptersWithSource: SyncChaptersWithSource,
    private val coverCache: CoverCache,
    private val libraryPreferences: LibraryPreferences,
    private val downloadManager: DownloadManager,
) : RemoteMangaUpdater {
    override suspend operator fun invoke(
        manga: Manga,
        fetchDetails: Boolean,
        fetchChapters: Boolean,
        manualFetch: Boolean,
        fetchWindow: ClosedRange<Instant>?,
    ): Result<RemoteMangaUpdate> {
        val source = sourceManager.getOrStub(manga.source)
        return invoke(
            source = source,
            manga = manga,
            fetchDetails = fetchDetails,
            fetchChapters = fetchChapters,
            manualFetch = manualFetch,
        )
    }

    override suspend operator fun invoke(
        source: ContentSource,
        manga: Manga,
        fetchDetails: Boolean,
        fetchChapters: Boolean,
        manualFetch: Boolean,
        fetchWindow: ClosedRange<Instant>?,
    ): Result<RemoteMangaUpdate> = withContext(Dispatchers.IO) {
        try {
            val chapters = chapterRepository.getChapterByMangaId(manga.id)
                .sortedBy { it.sourceOrder }
            val update = source.refresh(
                manga = manga.toSourceManga(),
                chapters = chapters.map { it.toSourceChapter() },
                fetchDetails = fetchDetails,
                fetchChapters = fetchChapters,
            )
            awaitUpdateFromSource(manga, update.manga, manualFetch)
            val newChapters = syncChaptersWithSource.await(
                rawSourceChapters = update.chapters,
                manga = manga,
                source = source,
                manualFetch = manualFetch,
                fetchWindow = fetchWindow,
            )
            val updatedManga = mangaRepository.getMangaById(manga.id)

            Result.success(RemoteMangaUpdate(manga = updatedManga, newChapters = newChapters))
        } catch (e: Exception) {
            logcat(LogPriority.ERROR, e)
            Result.failure(e)
        }
    }

    private suspend fun awaitUpdateFromSource(
        localManga: Manga,
        remoteManga: SourceManga,
        manualFetch: Boolean,
    ): Boolean {
        val remoteTitle = try {
            remoteManga.title
        } catch (_: UninitializedPropertyAccessException) {
            ""
        }

        // if the manga isn't a favorite (or 'update titles' preference is enabled), set its title from source and update in db
        val title =
            if (remoteTitle.isNotEmpty() && (!localManga.favorite || libraryPreferences.updateMangaTitles.get())) {
                remoteTitle
            } else {
                null
            }

        val coverLastModified = when {
            // Never refresh covers if the url is empty to avoid "losing" existing covers
            remoteManga.thumbnailUrl.isNullOrEmpty() -> null
            !manualFetch && localManga.thumbnailUrl == remoteManga.thumbnailUrl -> null
            localManga.isLocal() -> Clock.System.now()
            coverCache.hasCustomCover(localManga.id) -> {
                coverCache.deleteFromCache(localManga, false)
                null
            }
            else -> {
                coverCache.deleteFromCache(localManga, false)
                Clock.System.now()
            }
        }

        val thumbnailUrl = remoteManga.thumbnailUrl?.takeIf { it.isNotEmpty() }

        val success = mangaRepository.updateRemote(
            MangaRemoteUpdate(
                id = localManga.id,
                title = title,
                coverLastModified = coverLastModified,
                author = remoteManga.author,
                artist = remoteManga.artist,
                description = remoteManga.description,
                genre = remoteManga.genres.takeIf { it.isNotEmpty() },
                thumbnailUrl = thumbnailUrl,
                status = remoteManga.status.code,
                updateStrategy = remoteManga.updateStrategy,
                initialized = true,
                memo = remoteManga.memo,
            ),
        )
        if (success && title != null) {
            downloadManager.renameManga(localManga, title)
        }
        return success
    }
}
