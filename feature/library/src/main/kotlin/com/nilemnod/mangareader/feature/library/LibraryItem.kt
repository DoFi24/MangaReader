package com.nilemnod.mangareader.feature.library

import com.nilemnod.mangareader.domain.library.model.LibraryManga

data class LibraryItem(
    val libraryManga: LibraryManga,
    val downloadCount: Int,
    val unreadCount: Long,
    val isLocal: Boolean,
    val sourceName: String,
    val sourceLanguage: String,
    val badges: Badges,
) {
    val id: Long = libraryManga.id

    data class Badges(
        val downloadCount: Int,
        val unreadCount: Long,
        val isLocal: Boolean,
        val sourceLanguage: String,
    )
}
