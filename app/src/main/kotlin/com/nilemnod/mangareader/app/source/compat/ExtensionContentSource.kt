package com.nilemnod.mangareader.app.source.compat

import com.nilemnod.mangareader.domain.source.api.ContentSource
import com.nilemnod.mangareader.domain.source.api.FilterList
import com.nilemnod.mangareader.domain.source.api.ImageContent
import com.nilemnod.mangareader.domain.source.api.MangaListing
import com.nilemnod.mangareader.domain.source.api.MangaRefresh
import com.nilemnod.mangareader.domain.source.api.PageRef
import com.nilemnod.mangareader.domain.source.api.SourceChapter
import com.nilemnod.mangareader.domain.source.api.SourceManga
import com.nilemnod.mangareader.domain.source.api.WebAccess
import eu.kanade.tachiyomi.source.Source
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.online.HttpSource

class ExtensionContentSource(
    val native: Source,
) : ContentSource {

    override val id: Long
        get() = native.id

    override val name: String
        get() = native.name

    override val lang: String
        get() = native.lang

    override val supportsLatest: Boolean
        get() = native.supportsLatest

    override val web: WebAccess? = (native as? HttpSource)?.let { HttpWebAccess(it) }

    override fun filters(): FilterList = native.getFilterList().toSource()

    override suspend fun popular(page: Int): MangaListing = native.getPopularManga(page).toSource()

    override suspend fun latest(page: Int): MangaListing = native.getLatestUpdates(page).toSource()

    override suspend fun search(page: Int, query: String, filters: FilterList): MangaListing {
        val nativeFilters = native.getFilterList().also { it.applyFrom(filters) }
        return native.getSearchManga(page, query, nativeFilters).toSource()
    }

    override suspend fun refresh(
        manga: SourceManga,
        chapters: List<SourceChapter>,
        fetchDetails: Boolean,
        fetchChapters: Boolean,
    ): MangaRefresh {
        val update = native.getMangaUpdate(
            manga = manga.toNative(),
            chapters = chapters.map { it.toNative() },
            fetchDetails = fetchDetails,
            fetchChapters = fetchChapters,
        )
        return MangaRefresh(
            manga = update.manga.toSource(),
            chapters = update.chapters.map { it.toSource() },
        )
    }

    override suspend fun pages(chapter: SourceChapter): List<PageRef> {
        return native.getPageList(chapter.toNative()).map { it.toSource() }
    }

    override suspend fun openImage(
        page: PageRef,
        resumeFrom: Long,
        onProgress: (bytesRead: Long, contentLength: Long) -> Unit,
    ): ImageContent {
        val http = native as? HttpSource ?: throw UnsupportedOperationException("Source has no remote images")
        val nativePage = object : Page(page.index, page.url, page.imageUrl) {
            override fun update(bytesRead: Long, contentLength: Long, done: Boolean) {
                onProgress(bytesRead, contentLength)
            }
        }
        if (nativePage.imageUrl.isNullOrEmpty()) {
            nativePage.imageUrl = http.getImageUrl(nativePage)
        }
        val response = http.getImage(nativePage, resumeFrom)
        val body = response.body
        return ImageContent(
            source = body.source(),
            contentType = body.contentType()?.toString(),
            contentLength = body.contentLength(),
            isPartial = response.code == PARTIAL_CONTENT,
        )
    }

    override fun prepareNewChapter(chapter: SourceChapter, manga: SourceManga): SourceChapter {
        val http = native as? HttpSource ?: return chapter
        val nativeChapter = chapter.toNative()
        http.prepareNewChapter(nativeChapter, manga.toNative())
        return nativeChapter.toSource()
    }

    override fun toString(): String = native.toString()

    private companion object {
        const val PARTIAL_CONTENT = 206
    }
}

private class HttpWebAccess(private val source: HttpSource) : WebAccess {

    override val homeUrl: String
        get() = source.getHomeUrl()

    override val headers: Map<String, String>
        get() = source.headers.toMap()

    override val client
        get() = source.client

    override fun mangaUrl(manga: SourceManga): String = source.getMangaUrl(manga.toNative())

    override fun chapterUrl(chapter: SourceChapter): String = source.getChapterUrl(chapter.toNative())
}
