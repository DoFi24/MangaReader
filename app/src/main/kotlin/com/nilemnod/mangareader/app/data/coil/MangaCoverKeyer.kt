package com.nilemnod.mangareader.app.data.coil

import coil3.key.Keyer
import coil3.request.Options
import com.nilemnod.mangareader.app.domain.manga.model.hasCustomCover
import com.nilemnod.mangareader.core.storage.CoverCache
import com.nilemnod.mangareader.domain.manga.model.MangaCover
import com.nilemnod.mangareader.domain.manga.model.Manga as DomainManga

class MangaKeyer : Keyer<DomainManga> {
    override fun key(data: DomainManga, options: Options): String {
        return if (data.favorite && data.hasCustomCover()) {
            "${data.id};${data.coverLastModified}"
        } else {
            "${data.thumbnailUrl};${data.coverLastModified}"
        }
    }
}

class MangaCoverKeyer(
    private val coverCache: CoverCache,
) : Keyer<MangaCover> {
    override fun key(data: MangaCover, options: Options): String {
        return if (data.isMangaFavorite && coverCache.hasCustomCover(data.mangaId)) {
            "${data.mangaId};${data.lastModified}"
        } else {
            "${data.url};${data.lastModified}"
        }
    }
}
