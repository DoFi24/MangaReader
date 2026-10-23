package com.nilemnod.mangareader.source.mangadex

import kotlinx.serialization.Serializable

@Serializable
internal class MangaListDto(
    val data: List<MangaDto> = emptyList(),
    val total: Int = 0,
)

@Serializable
internal class MangaResponseDto(
    val data: MangaDto,
)

@Serializable
internal class MangaDto(
    val id: String,
    val attributes: MangaAttributesDto,
    val relationships: List<RelationshipDto> = emptyList(),
)

@Serializable
internal class MangaAttributesDto(
    val title: Map<String, String> = emptyMap(),
    val altTitles: List<Map<String, String>> = emptyList(),
    val description: Map<String, String> = emptyMap(),
    val status: String? = null,
    val tags: List<TagDto> = emptyList(),
)

@Serializable
internal class TagDto(
    val attributes: TagAttributesDto,
)

@Serializable
internal class TagAttributesDto(
    val name: Map<String, String> = emptyMap(),
    val group: String = "",
)

@Serializable
internal class RelationshipDto(
    val id: String,
    val type: String,
    val attributes: RelationshipAttributesDto? = null,
)

@Serializable
internal class RelationshipAttributesDto(
    val fileName: String? = null,
    val name: String? = null,
)

@Serializable
internal class ChapterListDto(
    val data: List<ChapterDto> = emptyList(),
    val total: Int = 0,
)

@Serializable
internal class ChapterDto(
    val id: String,
    val attributes: ChapterAttributesDto,
    val relationships: List<RelationshipDto> = emptyList(),
)

@Serializable
internal class ChapterAttributesDto(
    val volume: String? = null,
    val chapter: String? = null,
    val title: String? = null,
    val publishAt: String? = null,
)

@Serializable
internal class AtHomeDto(
    val baseUrl: String,
    val chapter: AtHomeChapterDto,
)

@Serializable
internal class AtHomeChapterDto(
    val hash: String,
    val data: List<String> = emptyList(),
    val dataSaver: List<String> = emptyList(),
)
