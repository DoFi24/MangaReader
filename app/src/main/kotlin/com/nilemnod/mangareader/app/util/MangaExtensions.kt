package com.nilemnod.mangareader.app.util

import com.nilemnod.mangareader.app.data.cache.CoverCache
import com.nilemnod.mangareader.domain.manga.interactor.UpdateManga
import com.nilemnod.mangareader.domain.manga.model.Manga
import com.nilemnod.mangareader.source.local.image.LocalCoverManager
import com.nilemnod.mangareader.source.local.isLocal
import java.io.InputStream

fun Manga.removeCovers(coverCache: CoverCache) {
    if (isLocal()) return
    coverCache.deleteFromCache(this, true)
}

suspend fun Manga.editCover(
    coverManager: LocalCoverManager,
    stream: InputStream,
    updateManga: UpdateManga,
    coverCache: CoverCache,
) {
    if (isLocal()) {
        coverManager.update(url, stream)
        updateManga.awaitUpdateCoverLastModified(id)
    } else if (favorite) {
        coverCache.setCustomCoverToCache(this, stream)
        updateManga.awaitUpdateCoverLastModified(id)
    }
}
