package com.nilemnod.mangareader.source.pepper

import com.nilemnod.mangareader.core.network.GET
import com.nilemnod.mangareader.core.network.NetworkHelper
import com.nilemnod.mangareader.core.network.awaitSuccess
import com.nilemnod.mangareader.domain.source.api.FilterList
import com.nilemnod.mangareader.domain.source.api.MangaListing
import com.nilemnod.mangareader.domain.source.api.MangaRefresh
import com.nilemnod.mangareader.domain.source.api.MangaStatus
import com.nilemnod.mangareader.domain.source.api.Page
import com.nilemnod.mangareader.domain.source.api.SourceChapter
import com.nilemnod.mangareader.domain.source.api.SourceManga
import com.nilemnod.mangareader.source.common.HttpContentSource
import com.nilemnod.mangareader.source.common.stableSourceId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class PepperCarrotSource(
    network: NetworkHelper,
    private val json: Json,
    override val lang: String,
) : HttpContentSource(network) {

    override val name: String = NAME

    override val id: Long = stableSourceId(NAME, lang)

    override val supportsLatest: Boolean = false

    override val baseUrl: String = BASE_URL

    private val episodesMutex = Mutex()

    private var episodes: List<Episode>? = null

    override fun mangaUrl(manga: SourceManga): String = "$baseUrl/$lang/webcomics/index.html"

    override fun chapterUrl(chapter: SourceChapter): String = "$baseUrl/$lang/webcomic/${chapter.url}.html"

    override suspend fun popular(page: Int): MangaListing = search(page, "", FilterList())

    override suspend fun latest(page: Int): MangaListing = popular(page)

    override suspend fun search(page: Int, query: String, filters: FilterList): MangaListing {
        val matches = query.isBlank() || SERIES.title.contains(query, ignoreCase = true)
        return MangaListing(if (matches && page == 1) listOf(SERIES) else emptyList(), hasNextPage = false)
    }

    override suspend fun refresh(
        manga: SourceManga,
        chapters: List<SourceChapter>,
        fetchDetails: Boolean,
        fetchChapters: Boolean,
    ): MangaRefresh {
        val details = if (fetchDetails) SERIES.copy(initialized = true) else manga
        val chapterList = if (fetchChapters) {
            loadEpisodes()
                .filter { lang in it.languages }
                .sortedByDescending { it.number }
                .map { it.toChapter() }
        } else {
            chapters
        }
        return MangaRefresh(details, chapterList)
    }

    override suspend fun pages(chapter: SourceChapter): List<Page> {
        val episode = loadEpisodes().firstOrNull { it.name == chapter.url }
            ?: error("Episode ${chapter.url} not found")
        val files = buildList {
            episode.pages["title"]?.let(::add)
            episode.pages
                .filterKeys { it.toIntOrNull() != null }
                .toSortedMap(compareBy { it.toInt() })
                .values
                .forEach(::add)
            episode.pages["credits"]?.let(::add)
        }
        return files.mapIndexed { index, file ->
            Page(index, imageUrl = "$baseUrl/0_sources/${episode.name}/hi-res/${lang}_$file")
        }
    }

    private suspend fun loadEpisodes(): List<Episode> = episodesMutex.withLock {
        episodes ?: withContext(Dispatchers.IO) {
            client.newCall(GET(EPISODES_URL, headers)).awaitSuccess().use { response ->
                json.decodeFromString<List<Episode>>(response.body.string())
            }
        }.also { episodes = it }
    }

    @Serializable
    private class Episode(
        val name: String,
        val pages: Map<String, String> = emptyMap(),
        @SerialName("translated_languages") val languages: List<String> = emptyList(),
    ) {

        val number: Int
            get() = NUMBER.find(name)?.groupValues?.get(1)?.toIntOrNull() ?: 0

        fun toChapter(): SourceChapter {
            val title = name.substringAfter('_', "").replace('-', ' ')
            return SourceChapter(
                url = name,
                name = "Episode $number: $title",
                chapterNumber = number.toDouble(),
            )
        }
    }

    private companion object {
        const val NAME = "Pepper&Carrot"
        const val BASE_URL = "https://www.peppercarrot.com"
        const val EPISODES_URL = "$BASE_URL/0_sources/episodes-v1.json"

        val NUMBER = Regex("^ep(\\d+)_")

        val SERIES = SourceManga(
            url = "pepper-and-carrot",
            title = "Pepper&Carrot",
            author = "David Revoy",
            artist = "David Revoy",
            description = "Open source webcomic about the young witch Pepper and her cat Carrot. " +
                "Licensed under Creative Commons Attribution 4.0 (CC BY 4.0).",
            genres = listOf("Fantasy", "Comedy", "Open source"),
            status = MangaStatus.ONGOING,
            thumbnailUrl = "$BASE_URL/0_sources/ep01_Potion-of-Flight/low-res/Pepper-and-Carrot_by-David-Revoy_E01.jpg",
        )
    }
}
