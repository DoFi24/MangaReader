package com.nilemnod.mangareader.domain.manga.model

import com.nilemnod.mangareader.domain.manga.model.Manga
import com.nilemnod.mangareader.domain.source.api.UpdateStrategy
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
        updateStrategy = UpdateStrategy.valueOf(update_strategy.name),
        initialized = initialized,
        memo = memo,
        source = sourceId,
    )
}
