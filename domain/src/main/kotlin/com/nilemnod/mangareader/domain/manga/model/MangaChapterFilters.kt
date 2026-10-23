package com.nilemnod.mangareader.domain.manga.model

import com.nilemnod.mangareader.core.common.preference.TriState

fun Manga.downloadedFilter(downloadedOnly: Boolean): TriState {
    if (downloadedOnly) return TriState.ENABLED_IS
    return when (downloadedFilterRaw) {
        Manga.CHAPTER_SHOW_DOWNLOADED -> TriState.ENABLED_IS
        Manga.CHAPTER_SHOW_NOT_DOWNLOADED -> TriState.ENABLED_NOT
        else -> TriState.DISABLED
    }
}

fun Manga.chaptersFiltered(downloadedOnly: Boolean): Boolean {
    return unreadFilter != TriState.DISABLED ||
        downloadedFilter(downloadedOnly) != TriState.DISABLED ||
        bookmarkedFilter != TriState.DISABLED
}
