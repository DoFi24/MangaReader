package com.nilemnod.mangareader.domain.source.api

interface ContentSource {

    val id: Long

    val name: String

    val lang: String

    val supportsLatest: Boolean

    val web: WebAccess?
        get() = null

    fun filters(): FilterList = FilterList()

    suspend fun popular(page: Int): MangaListing

    suspend fun latest(page: Int): MangaListing

    suspend fun search(page: Int, query: String, filters: FilterList): MangaListing

    suspend fun refresh(
        manga: SourceManga,
        chapters: List<SourceChapter>,
        fetchDetails: Boolean,
        fetchChapters: Boolean,
    ): MangaRefresh

    suspend fun pages(chapter: SourceChapter): List<PageRef>

    suspend fun openImage(
        page: PageRef,
        resumeFrom: Long = 0L,
        onProgress: (bytesRead: Long, contentLength: Long) -> Unit = { _, _ -> },
    ): ImageContent

    fun prepareNewChapter(chapter: SourceChapter, manga: SourceManga): SourceChapter = chapter
}
