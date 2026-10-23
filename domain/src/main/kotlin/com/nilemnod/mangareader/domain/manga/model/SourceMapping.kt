package com.nilemnod.mangareader.domain.manga.model

import com.nilemnod.mangareader.domain.source.api.MangaStatus
import com.nilemnod.mangareader.domain.source.api.SourceManga

fun Manga.toSourceManga(): SourceManga = SourceManga(
    url = url,
    title = title,
    author = author,
    artist = artist,
    description = description,
    genres = genre.orEmpty(),
    status = MangaStatus.fromCode(status),
    thumbnailUrl = thumbnailUrl,
    updateStrategy = updateStrategy,
    initialized = initialized,
    memo = memo,
)

fun SourceManga.toDomainManga(sourceId: Long): Manga {
    return Manga.create().copy(
        url = url,
        title = title,
        artist = artist,
        author = author,
        description = description,
        genre = genres.takeIf { it.isNotEmpty() },
        status = status.code,
        thumbnailUrl = thumbnailUrl,
        updateStrategy = updateStrategy,
        initialized = initialized,
        memo = memo,
        source = sourceId,
    )
}

fun Manga.copyFrom(other: SourceManga): Manga {
    return copy(
        author = other.author ?: author,
        artist = other.artist ?: artist,
        description = other.description ?: description,
        genre = other.genres.takeIf { it.isNotEmpty() } ?: genre,
        thumbnailUrl = other.thumbnailUrl ?: thumbnailUrl,
        status = other.status.code,
        updateStrategy = other.updateStrategy,
        initialized = other.initialized && initialized,
        memo = other.memo,
    )
}
