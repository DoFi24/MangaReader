package com.nilemnod.mangareader.app.data.backup.create.creators

import dev.zacsweers.metro.Inject
import com.nilemnod.mangareader.app.data.backup.create.BackupOptions
import com.nilemnod.mangareader.app.data.backup.models.BackupHistory
import com.nilemnod.mangareader.app.data.backup.models.BackupManga
import com.nilemnod.mangareader.app.data.backup.models.toBackupChapter
import com.nilemnod.mangareader.app.ui.reader.setting.ReadingMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import com.nilemnod.mangareader.core.common.extensions.toByteArray
import com.nilemnod.mangareader.core.common.extensions.toEpochMillisOrZero
import com.nilemnod.mangareader.domain.category.repository.CategoryRepository
import com.nilemnod.mangareader.domain.chapter.repository.ChapterRepository
import com.nilemnod.mangareader.domain.history.repository.HistoryRepository
import com.nilemnod.mangareader.domain.manga.model.Manga
import com.nilemnod.mangareader.domain.manga.repository.MangaRepository

@Inject
class MangaBackupCreator(
    private val mangaRepository: MangaRepository,
    private val chapterRepository: ChapterRepository,
    private val categoryRepository: CategoryRepository,
    private val historyRepository: HistoryRepository,
) {

    /**
     * Builds the entries a chunk at a time, so only one chunk's chapters are held at once.
     */
    operator fun invoke(mangas: List<Manga>, options: BackupOptions): Flow<BackupManga> = flow {
        mangas.chunked(CHUNK_SIZE).forEach { chunk ->
            backupMangas(chunk, options).forEach { emit(it) }
        }
    }

    private suspend fun backupMangas(mangas: List<Manga>, options: BackupOptions): List<BackupManga> {
        val mangaIds = mangas.map { it.id }

        val excludedScanlatorsByMangaId = mangaRepository.getExcludedScanlatorsByMangaIds(mangaIds)
        // History is backed up by chapter URL, so it needs the chapters too
        val chaptersByMangaId = if (options.chapters || options.history) {
            chapterRepository.getChaptersByMangaIds(mangaIds)
        } else {
            emptyMap()
        }
        val categoriesByMangaId = if (options.categories) {
            categoryRepository.getCategoriesByMangaIds(mangaIds)
        } else {
            emptyMap()
        }
        val historyByMangaId = if (options.history) historyRepository.getHistoryByMangaIds(mangaIds) else emptyMap()

        return mangas.map { manga ->
            val mangaObject = manga.toBackupManga()
            val chapters = chaptersByMangaId[manga.id].orEmpty()

            mangaObject.excludedScanlators = excludedScanlatorsByMangaId[manga.id].orEmpty()

            if (options.chapters) {
                mangaObject.chapters = chapters.map { it.toBackupChapter() }
            }

            if (options.categories) {
                mangaObject.categories = categoriesByMangaId[manga.id].orEmpty().map { it.order }
            }

            if (options.history) {
                val chapterUrlsById = chapters.associate { it.id to it.url }
                mangaObject.history = historyByMangaId[manga.id].orEmpty().mapNotNull { history ->
                    // A chapter removed since its history was read takes that history with it
                    val url = chapterUrlsById[history.chapterId] ?: return@mapNotNull null
                    BackupHistory(url, history.readAt.toEpochMillisOrZero(), history.readDuration)
                }
            }

            mangaObject
        }
    }

    private companion object {
        const val CHUNK_SIZE = 100
    }
}

private fun Manga.toBackupManga() =
    BackupManga(
        url = this.url,
        title = this.title,
        artist = this.artist,
        author = this.author,
        description = this.description,
        genre = this.genre.orEmpty(),
        status = this.status.toInt(),
        thumbnailUrl = this.thumbnailUrl,
        favorite = this.favorite,
        source = this.source,
        dateAdded = this.favoriteAt.toEpochMillisOrZero(),
        viewer = (this.viewerFlags.toInt() and ReadingMode.MASK),
        viewer_flags = this.viewerFlags.toInt(),
        chapterFlags = this.chapterFlags.toInt(),
        updateStrategy = this.updateStrategy,
        notes = this.notes,
        initialized = this.initialized,
        memo = this.memo.toByteArray(),
        nextUpdate = this.nextUpdate.toEpochMillisOrZero(),
    )
