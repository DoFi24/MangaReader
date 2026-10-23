package com.nilemnod.mangareader.feature.reader.legacy

import com.nilemnod.mangareader.core.reader.ReaderOrientation
import com.nilemnod.mangareader.core.reader.ReadingMode
import com.nilemnod.mangareader.domain.manga.model.MangaUpdate
import com.nilemnod.mangareader.domain.manga.repository.MangaRepository
import dev.zacsweers.metro.Inject

@Inject
class SetMangaViewerFlags(
    private val mangaRepository: MangaRepository,
) {

    suspend fun awaitSetReadingMode(id: Long, flag: Long) {
        val manga = mangaRepository.getMangaById(id)
        mangaRepository.update(
            MangaUpdate(id) {
                viewerFlags = manga.viewerFlags.setFlag(flag, ReadingMode.MASK.toLong())
            },
        )
    }

    suspend fun awaitSetOrientation(id: Long, flag: Long) {
        val manga = mangaRepository.getMangaById(id)
        mangaRepository.update(
            MangaUpdate(id) {
                viewerFlags = manga.viewerFlags.setFlag(flag, ReaderOrientation.MASK.toLong())
            },
        )
    }

    private fun Long.setFlag(flag: Long, mask: Long): Long {
        return this and mask.inv() or (flag and mask)
    }
}
