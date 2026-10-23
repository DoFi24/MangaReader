package com.nilemnod.mangareader.domain.manga.model

import com.nilemnod.mangareader.domain.source.api.UpdateStrategy
import kotlinx.serialization.json.JsonObject
import kotlin.time.Instant

data class MangaRemoteUpdate(
    val id: Long,
    val title: String?,
    val author: String?,
    val artist: String?,
    val description: String?,
    val genre: List<String>?,
    val status: Long,
    val thumbnailUrl: String?,
    val updateStrategy: UpdateStrategy,
    val memo: JsonObject,
    val initialized: Boolean,
    val coverLastModified: Instant?,
)
