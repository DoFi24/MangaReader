package com.nilemnod.mangareader.domain.source.api

import kotlinx.serialization.json.JsonObject
import okio.BufferedSource

enum class MangaStatus(val code: Long) {
    UNKNOWN(0),
    ONGOING(1),
    COMPLETED(2),
    LICENSED(3),
    PUBLISHING_FINISHED(4),
    CANCELLED(5),
    ON_HIATUS(6),
    ;

    companion object {
        fun fromCode(code: Long): MangaStatus = entries.firstOrNull { it.code == code } ?: UNKNOWN
    }
}

enum class UpdateStrategy {
    ALWAYS_UPDATE,
    ONLY_FETCH_ONCE,
}

data class SourceManga(
    val url: String,
    val title: String,
    val author: String? = null,
    val artist: String? = null,
    val description: String? = null,
    val genres: List<String> = emptyList(),
    val status: MangaStatus = MangaStatus.UNKNOWN,
    val thumbnailUrl: String? = null,
    val updateStrategy: UpdateStrategy = UpdateStrategy.ALWAYS_UPDATE,
    val initialized: Boolean = false,
    val memo: JsonObject = JsonObject(emptyMap()),
)

data class SourceChapter(
    val url: String,
    val name: String,
    val chapterNumber: Double = -1.0,
    val scanlator: String? = null,
    val dateUpload: Long = 0L,
    val memo: JsonObject = JsonObject(emptyMap()),
)

data class MangaListing(
    val mangas: List<SourceManga>,
    val hasNextPage: Boolean,
)

data class MangaRefresh(
    val manga: SourceManga,
    val chapters: List<SourceChapter>,
)

class ImageContent(
    val source: BufferedSource,
    val contentType: String? = null,
    val contentLength: Long = -1L,
    val isPartial: Boolean = false,
) : AutoCloseable {

    override fun close() {
        source.close()
    }
}
