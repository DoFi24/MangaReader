package com.nilemnod.mangareader.compat.tachiyomi

import androidx.preference.PreferenceScreen
import com.nilemnod.mangareader.domain.source.api.ContentSource
import com.nilemnod.mangareader.domain.source.api.FilterList
import com.nilemnod.mangareader.domain.source.api.ImageContent
import com.nilemnod.mangareader.domain.source.api.MangaListing
import com.nilemnod.mangareader.domain.source.api.MangaRefresh
import com.nilemnod.mangareader.domain.source.api.Page
import com.nilemnod.mangareader.domain.source.api.SourceChapter
import com.nilemnod.mangareader.domain.source.api.SourceHttpException
import com.nilemnod.mangareader.domain.source.api.SourceManga
import com.nilemnod.mangareader.domain.source.api.SourceSettings
import com.nilemnod.mangareader.domain.source.api.UriResolver
import com.nilemnod.mangareader.domain.source.api.UriType
import com.nilemnod.mangareader.domain.source.api.WebAccess
import eu.kanade.tachiyomi.network.HttpException
import eu.kanade.tachiyomi.source.ConfigurableSource
import eu.kanade.tachiyomi.source.Source
import eu.kanade.tachiyomi.source.UnmeteredSource
import eu.kanade.tachiyomi.source.online.HttpSource
import eu.kanade.tachiyomi.source.online.ResolvableSource
import eu.kanade.tachiyomi.source.preferenceKey
import eu.kanade.tachiyomi.source.model.Page as NativePage
import eu.kanade.tachiyomi.source.online.UriType as NativeUriType

internal class ExtensionContentSource(
    val native: Source,
    override val origin: String?,
) : ContentSource {

    override val id: Long
        get() = native.id

    override val name: String
        get() = native.name

    override val lang: String
        get() = native.lang

    override val supportsLatest: Boolean
        get() = native.supportsLatest

    override val settings: SourceSettings? = (native as? ConfigurableSource)?.let { ExtensionSourceSettings(it) }

    override val resolver: UriResolver? = (native as? ResolvableSource)?.let { ExtensionUriResolver(it) }

    override val isUnmetered: Boolean
        get() = native is UnmeteredSource

    override val displayName: String
        get() = native.toString()

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

    override suspend fun pages(chapter: SourceChapter): List<Page> {
        return native.getPageList(chapter.toNative()).map { it.toSource() }
    }

    override suspend fun openImage(
        page: Page,
        resumeFrom: Long,
        onProgress: (bytesRead: Long, contentLength: Long) -> Unit,
    ): ImageContent {
        val http = native as? HttpSource ?: throw UnsupportedOperationException("Source has no remote images")
        val nativePage = object : NativePage(page.index, page.url, page.imageUrl) {
            override fun update(bytesRead: Long, contentLength: Long, done: Boolean) {
                onProgress(bytesRead, contentLength)
            }
        }
        if (nativePage.imageUrl.isNullOrEmpty()) {
            nativePage.imageUrl = http.getImageUrl(nativePage)
            page.imageUrl = nativePage.imageUrl
        }
        val response = try {
            http.getImage(nativePage, resumeFrom)
        } catch (e: HttpException) {
            throw SourceHttpException(e.code)
        }
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

    override suspend fun imageUrl(page: Page): String {
        val http = native as? HttpSource ?: return page.imageUrl.orEmpty()
        return http.getImageUrl(NativePage(page.index, page.url, page.imageUrl))
    }

    override fun equals(other: Any?): Boolean = other is ExtensionContentSource && other.native === native

    override fun hashCode(): Int = System.identityHashCode(native)

    override fun toString(): String = native.toString()

    private companion object {
        const val PARTIAL_CONTENT = 206
    }
}

private class ExtensionSourceSettings(private val source: ConfigurableSource) : SourceSettings {

    override val preferencesName: String
        get() = source.preferenceKey()

    override fun setupScreen(screen: PreferenceScreen) = source.setupPreferenceScreen(screen)
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

private class ExtensionUriResolver(private val source: ResolvableSource) : UriResolver {

    override fun uriType(uri: String): UriType = when (source.getUriType(uri)) {
        NativeUriType.Manga -> UriType.Manga
        NativeUriType.Chapter -> UriType.Chapter
        NativeUriType.Unknown -> UriType.Unknown
    }

    override suspend fun manga(uri: String): SourceManga? = source.getManga(uri)?.toSource()

    override suspend fun chapter(uri: String): SourceChapter? = source.getChapter(uri)?.toSource()
}
