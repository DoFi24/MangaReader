package com.nilemnod.mangareader.app.source.compat

import com.nilemnod.mangareader.domain.source.api.MangaListing
import com.nilemnod.mangareader.domain.source.api.MangaStatus
import com.nilemnod.mangareader.domain.source.api.SourceChapter
import com.nilemnod.mangareader.domain.source.api.SourceManga
import com.nilemnod.mangareader.domain.source.api.UpdateStrategy
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import com.nilemnod.mangareader.domain.source.api.Page as SourcePage
import eu.kanade.tachiyomi.source.model.Page as NativePage
import eu.kanade.tachiyomi.source.model.UpdateStrategy as NativeUpdateStrategy

internal fun SManga.toSource(): SourceManga = SourceManga(
    url = runCatching { url }.getOrDefault(""),
    title = runCatching { title }.getOrDefault(""),
    author = author,
    artist = artist,
    description = description,
    genres = getGenres().orEmpty(),
    status = MangaStatus.fromCode(status.toLong()),
    thumbnailUrl = thumbnail_url,
    updateStrategy = when (update_strategy) {
        NativeUpdateStrategy.ALWAYS_UPDATE -> UpdateStrategy.ALWAYS_UPDATE
        NativeUpdateStrategy.ONLY_FETCH_ONCE -> UpdateStrategy.ONLY_FETCH_ONCE
    },
    initialized = initialized,
    memo = memo,
)

internal fun SourceManga.toNative(): SManga = SManga.create().also {
    it.url = url
    it.title = title
    it.author = author
    it.artist = artist
    it.description = description
    it.genre = genres.takeIf { genres -> genres.isNotEmpty() }?.joinToString(", ")
    it.status = status.code.toInt()
    it.thumbnail_url = thumbnailUrl
    it.update_strategy = when (updateStrategy) {
        UpdateStrategy.ALWAYS_UPDATE -> NativeUpdateStrategy.ALWAYS_UPDATE
        UpdateStrategy.ONLY_FETCH_ONCE -> NativeUpdateStrategy.ONLY_FETCH_ONCE
    }
    it.initialized = initialized
    it.memo = memo
}

internal fun SChapter.toSource(): SourceChapter = SourceChapter(
    url = runCatching { url }.getOrDefault(""),
    name = runCatching { name }.getOrDefault(""),
    chapterNumber = chapter_number.toDouble(),
    scanlator = scanlator,
    dateUpload = date_upload,
    memo = memo,
)

internal fun SourceChapter.toNative(): SChapter = SChapter.create().also {
    it.url = url
    it.name = name
    it.chapter_number = chapterNumber.toFloat()
    it.scanlator = scanlator
    it.date_upload = dateUpload
    it.memo = memo
}

internal fun MangasPage.toSource(): MangaListing = MangaListing(
    mangas = mangas.map { it.toSource() },
    hasNextPage = hasNextPage,
)

internal fun NativePage.toSource(): SourcePage = SourcePage(index = index, url = url, imageUrl = imageUrl)
