package com.nilemnod.mangareader.domain.source.model

import com.nilemnod.mangareader.domain.source.api.ContentSource
import com.nilemnod.mangareader.domain.source.api.FilterList
import com.nilemnod.mangareader.domain.source.api.ImageContent
import com.nilemnod.mangareader.domain.source.api.MangaListing
import com.nilemnod.mangareader.domain.source.api.MangaRefresh
import com.nilemnod.mangareader.domain.source.api.Page
import com.nilemnod.mangareader.domain.source.api.SourceChapter
import com.nilemnod.mangareader.domain.source.api.SourceManga

data class StubSource(
    override val id: Long,
    override val lang: String,
    override val name: String,
) : ContentSource {

    private val isInvalid: Boolean = name.isBlank() || lang.isBlank()

    override val supportsLatest: Boolean = false

    override val isStub: Boolean = true

    override val displayName: String
        get() = if (!isInvalid) "$name (${lang.uppercase()})" else id.toString()

    override suspend fun popular(page: Int): MangaListing = throw SourceNotInstalledException()

    override suspend fun latest(page: Int): MangaListing = throw SourceNotInstalledException()

    override suspend fun search(page: Int, query: String, filters: FilterList): MangaListing =
        throw SourceNotInstalledException()

    override suspend fun refresh(
        manga: SourceManga,
        chapters: List<SourceChapter>,
        fetchDetails: Boolean,
        fetchChapters: Boolean,
    ): MangaRefresh = throw SourceNotInstalledException()

    override suspend fun pages(chapter: SourceChapter): List<Page> = throw SourceNotInstalledException()

    override suspend fun openImage(
        page: Page,
        resumeFrom: Long,
        onProgress: (bytesRead: Long, contentLength: Long) -> Unit,
    ): ImageContent = throw SourceNotInstalledException()

    override fun toString(): String = displayName

    companion object {
        fun from(source: ContentSource): StubSource {
            return StubSource(id = source.id, lang = source.lang, name = source.name)
        }
    }
}

class SourceNotInstalledException : Exception()
