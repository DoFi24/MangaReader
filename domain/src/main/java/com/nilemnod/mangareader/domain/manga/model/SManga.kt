package com.nilemnod.mangareader.domain.manga.model

import com.nilemnod.mangareader.domain.manga.model.Manga
import eu.kanade.tachiyomi.source.model.SManga

fun SManga.toDomainManga(sourceId: Long): Manga {
    return Manga.create().copy(
        url = url,
        title = title,
        artist = artist,
        author = author,
        description = description,
        genre = getGenres(),
        status = status.toLong(),
        thumbnailUrl = thumbnail_url,
        updateStrategy = update_strategy,
        initialized = initialized,
        memo = memo,
        source = sourceId,
    )
}
