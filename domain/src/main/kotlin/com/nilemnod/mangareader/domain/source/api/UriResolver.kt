package com.nilemnod.mangareader.domain.source.api

interface UriResolver {

    fun uriType(uri: String): UriType

    suspend fun manga(uri: String): SourceManga?

    suspend fun chapter(uri: String): SourceChapter?
}

sealed interface UriType {
    data object Manga : UriType
    data object Chapter : UriType
    data object Unknown : UriType
}
