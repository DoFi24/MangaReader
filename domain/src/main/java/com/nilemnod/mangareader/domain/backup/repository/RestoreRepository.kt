package com.nilemnod.mangareader.domain.backup.repository

import com.nilemnod.mangareader.domain.backup.model.RestoredManga
import com.nilemnod.mangareader.domain.manga.model.Manga
import com.nilemnod.mangareader.domain.manga.model.MangaUpdate

interface RestoreRepository {

    suspend fun getMangaUrlsBySourceId(): Map<Long, Set<String>>

    /**
     * Restores [entries] all together: either every one of them is restored or none is. [update] is what to change on each
     * entry once its chapters and history are in place, and is part of the same all-or-nothing restore.
     */
    suspend fun restoreManga(entries: List<RestoredManga>, update: suspend (Manga) -> MangaUpdate)
}
