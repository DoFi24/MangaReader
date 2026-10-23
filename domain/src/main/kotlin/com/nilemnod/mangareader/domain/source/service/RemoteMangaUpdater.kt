package com.nilemnod.mangareader.domain.source.service

import com.nilemnod.mangareader.domain.manga.model.Manga
import com.nilemnod.mangareader.domain.source.api.ContentSource
import com.nilemnod.mangareader.domain.source.models.RemoteMangaUpdate
import kotlin.time.Instant

interface RemoteMangaUpdater {

    suspend operator fun invoke(
        manga: Manga,
        fetchDetails: Boolean = false,
        fetchChapters: Boolean = false,
        manualFetch: Boolean = false,
        fetchWindow: ClosedRange<Instant>? = null,
    ): Result<RemoteMangaUpdate>

    suspend operator fun invoke(
        source: ContentSource,
        manga: Manga,
        fetchDetails: Boolean = false,
        fetchChapters: Boolean = false,
        manualFetch: Boolean = false,
        fetchWindow: ClosedRange<Instant>? = null,
    ): Result<RemoteMangaUpdate>
}
