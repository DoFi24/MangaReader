package com.nilemnod.mangareader.domain.source.api

import okhttp3.Call

interface WebAccess {

    val homeUrl: String

    val headers: Map<String, String>

    val client: Call.Factory

    fun mangaUrl(manga: SourceManga): String

    fun chapterUrl(chapter: SourceChapter): String
}
