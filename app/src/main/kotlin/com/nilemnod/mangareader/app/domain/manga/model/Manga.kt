package com.nilemnod.mangareader.app.domain.manga.model

import android.content.Context
import com.nilemnod.mangareader.app.data.cache.CoverCache
import com.nilemnod.mangareader.app.di.appGraph
import com.nilemnod.mangareader.app.ui.reader.setting.ReaderOrientation
import com.nilemnod.mangareader.app.ui.reader.setting.ReadingMode
import com.nilemnod.mangareader.core.common.preference.TriState
import com.nilemnod.mangareader.domain.manga.model.Manga
import eu.kanade.tachiyomi.source.model.SManga
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

// TODO: move these into the domain model
val Manga.readingMode: Long
    get() = viewerFlags and ReadingMode.MASK.toLong()

val Manga.readerOrientation: Long
    get() = viewerFlags and ReaderOrientation.MASK.toLong()

val Manga.downloadedFilter: TriState
    get() {
        if (Injekt.get<Context>().appGraph.basePreferences.downloadedOnly.get()) return TriState.ENABLED_IS
        return when (downloadedFilterRaw) {
            Manga.CHAPTER_SHOW_DOWNLOADED -> TriState.ENABLED_IS
            Manga.CHAPTER_SHOW_NOT_DOWNLOADED -> TriState.ENABLED_NOT
            else -> TriState.DISABLED
        }
    }
fun Manga.chaptersFiltered(): Boolean {
    return unreadFilter != TriState.DISABLED ||
        downloadedFilter != TriState.DISABLED ||
        bookmarkedFilter != TriState.DISABLED
}

fun Manga.toSManga(): SManga = SManga.create().also {
    it.url = url
    it.title = title
    it.artist = artist
    it.author = author
    it.description = description
    it.genre = genre.orEmpty().joinToString()
    it.status = status.toInt()
    it.thumbnail_url = thumbnailUrl
    it.initialized = initialized
    it.memo = memo
}

fun Manga.copyFrom(other: SManga): Manga {
    val author = other.author ?: author
    val artist = other.artist ?: artist
    val description = other.description ?: description
    val genres = if (other.genre != null) {
        other.getGenres()
    } else {
        genre
    }
    val thumbnailUrl = other.thumbnail_url ?: thumbnailUrl
    return this.copy(
        author = author,
        artist = artist,
        description = description,
        genre = genres,
        thumbnailUrl = thumbnailUrl,
        status = other.status.toLong(),
        updateStrategy = other.update_strategy,
        initialized = other.initialized && initialized,
        memo = other.memo,
    )
}

fun Manga.hasCustomCover(coverCache: CoverCache = Injekt.get<Context>().appGraph.coverCache): Boolean {
    return coverCache.hasCustomCover(id)
}

