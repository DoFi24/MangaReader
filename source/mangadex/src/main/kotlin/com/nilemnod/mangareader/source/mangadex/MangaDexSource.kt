package com.nilemnod.mangareader.source.mangadex

import com.nilemnod.mangareader.core.network.GET
import com.nilemnod.mangareader.core.network.NetworkHelper
import com.nilemnod.mangareader.core.network.awaitSuccess
import com.nilemnod.mangareader.domain.source.api.FilterList
import com.nilemnod.mangareader.domain.source.api.ImageContent
import com.nilemnod.mangareader.domain.source.api.MangaListing
import com.nilemnod.mangareader.domain.source.api.MangaRefresh
import com.nilemnod.mangareader.domain.source.api.MangaStatus
import com.nilemnod.mangareader.domain.source.api.Page
import com.nilemnod.mangareader.domain.source.api.SourceChapter
import com.nilemnod.mangareader.domain.source.api.SourceFilter
import com.nilemnod.mangareader.domain.source.api.SourceHttpException
import com.nilemnod.mangareader.domain.source.api.SourceManga
import com.nilemnod.mangareader.source.common.HttpContentSource
import com.nilemnod.mangareader.source.common.stableSourceId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import kotlin.time.Instant

class MangaDexSource(
    network: NetworkHelper,
    override val lang: String,
) : HttpContentSource(network) {

    override val name: String = NAME

    override val id: Long = stableSourceId(NAME, lang)

    override val supportsLatest: Boolean = true

    override val baseUrl: String = "https://mangadex.org"

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private val requestMutex = Mutex()

    private var lastRequestAt = 0L

    override fun mangaUrl(manga: SourceManga): String = "$baseUrl/title/${manga.url}"

    override fun chapterUrl(chapter: SourceChapter): String = "$baseUrl/chapter/${chapter.url}"

    override fun filters(): FilterList = FilterList(
        listOf(
            SourceFilter.Select(SORT, SORT_OPTIONS.map { it.first }, selected = 0),
            SourceFilter.Group(
                CONTENT_RATING,
                CONTENT_RATINGS.map { (label, value) ->
                    SourceFilter.CheckBox(label, checked = value in DEFAULT_RATINGS)
                },
            ),
        ),
    )

    override suspend fun popular(page: Int): MangaListing = listing(page, "", FOLLOWED, DEFAULT_RATINGS)

    override suspend fun latest(page: Int): MangaListing = listing(page, "", LATEST_UPLOAD, DEFAULT_RATINGS)

    override suspend fun search(page: Int, query: String, filters: FilterList): MangaListing {
        val sort = filters.filterIsInstance<SourceFilter.Select>().firstOrNull()
            ?.let { SORT_OPTIONS.getOrNull(it.selected)?.second }
        val ratings = filters.filterIsInstance<SourceFilter.Group>().firstOrNull()
            ?.filters
            ?.filterIsInstance<SourceFilter.CheckBox>()
            ?.filter { it.checked }
            ?.mapNotNull { box -> CONTENT_RATINGS.firstOrNull { it.first == box.name }?.second }
            ?.takeIf { it.isNotEmpty() }
            ?: DEFAULT_RATINGS
        return listing(page, query, sort ?: if (query.isBlank()) FOLLOWED else RELEVANCE, ratings)
    }

    private suspend fun listing(page: Int, query: String, order: String, ratings: List<String>): MangaListing {
        val offset = (page - 1) * PAGE_SIZE
        val url = API.endpoint("/manga").apply {
            addQueryParameter("limit", PAGE_SIZE.toString())
            addQueryParameter("offset", offset.toString())
            addQueryParameter("includes[]", "cover_art")
            addQueryParameter("availableTranslatedLanguage[]", lang)
            addQueryParameter("hasAvailableChapters", "true")
            ratings.forEach { addQueryParameter("contentRating[]", it) }
            if (query.isNotBlank()) addQueryParameter("title", query)
            if (order != RELEVANCE) addQueryParameter("order[$order]", "desc") else addQueryParameter("order[relevance]", "desc")
        }.build()
        val result = fetch<MangaListDto>(url)
        return MangaListing(
            mangas = result.data.map { it.toSourceManga() },
            hasNextPage = offset + result.data.size < result.total,
        )
    }

    override suspend fun refresh(
        manga: SourceManga,
        chapters: List<SourceChapter>,
        fetchDetails: Boolean,
        fetchChapters: Boolean,
    ): MangaRefresh {
        val details = if (fetchDetails) {
            val url = API.endpoint("/manga/${manga.url}").apply {
                addQueryParameter("includes[]", "cover_art")
                addQueryParameter("includes[]", "author")
                addQueryParameter("includes[]", "artist")
            }.build()
            fetch<MangaResponseDto>(url).data.toSourceManga(withPeople = true).copy(initialized = true)
        } else {
            manga
        }
        val chapterList = if (fetchChapters) loadChapters(manga.url) else chapters
        return MangaRefresh(details, chapterList)
    }

    private suspend fun loadChapters(mangaId: String): List<SourceChapter> {
        val chapters = mutableListOf<SourceChapter>()
        var offset = 0
        while (true) {
            val url = API.endpoint("/manga/$mangaId/feed").apply {
                addQueryParameter("limit", FEED_PAGE_SIZE.toString())
                addQueryParameter("offset", offset.toString())
                addQueryParameter("translatedLanguage[]", lang)
                addQueryParameter("order[volume]", "desc")
                addQueryParameter("order[chapter]", "desc")
                addQueryParameter("includes[]", "scanlation_group")
                addQueryParameter("includeExternalUrl", "0")
                addQueryParameter("includeEmptyPages", "0")
                CONTENT_RATINGS.forEach { addQueryParameter("contentRating[]", it.second) }
            }.build()
            val result = fetch<ChapterListDto>(url)
            chapters += result.data.map { it.toSourceChapter() }
            offset += result.data.size
            if (result.data.isEmpty() || offset >= result.total) break
        }
        return chapters
    }

    override suspend fun pages(chapter: SourceChapter): List<Page> {
        val server = fetch<AtHomeDto>(API.endpoint("/at-home/server/${chapter.url}").build())
        return server.chapter.data.mapIndexed { index, file ->
            val saver = server.chapter.dataSaver.getOrNull(index).orEmpty()
            Page(
                index,
                url = "${chapter.url}/$file/$saver",
                imageUrl = "${server.baseUrl}/data/${server.chapter.hash}/$file",
            )
        }
    }

    override suspend fun openImage(
        page: Page,
        resumeFrom: Long,
        onProgress: (bytesRead: Long, contentLength: Long) -> Unit,
    ): ImageContent {
        var attempt = 0
        while (true) {
            try {
                return super.openImage(page, resumeFrom, onProgress)
            } catch (e: SourceHttpException) {
                // Image nodes answer 404 or 5xx while they warm up their cache, so move to another node
                val retryable = e.code == HTTP_NOT_FOUND || e.code >= HTTP_SERVER_ERROR
                if (!retryable || ++attempt >= IMAGE_ATTEMPTS) throw e
                delay(IMAGE_RETRY_DELAY * attempt)
                if (attempt >= 2) refreshNode(page, useSaver = attempt % 2 == 0)
            }
        }
    }

    private suspend fun refreshNode(page: Page, useSaver: Boolean) {
        val parts = page.url.split('/', limit = 3).takeIf { it.size == 3 } ?: return
        val (chapterId, file, saver) = parts
        val server = fetch<AtHomeDto>(API.endpoint("/at-home/server/$chapterId").build())
        page.imageUrl = if (useSaver && saver.isNotEmpty()) {
            "${server.baseUrl}/data-saver/${server.chapter.hash}/$saver"
        } else {
            "${server.baseUrl}/data/${server.chapter.hash}/$file"
        }
    }

    private suspend inline fun <reified T> fetch(url: HttpUrl): T {
        throttle()
        return withContext(Dispatchers.IO) {
            client.newCall(GET(url, headers)).awaitSuccess().use { response ->
                json.decodeFromString<T>(response.body.string())
            }
        }
    }

    private suspend fun throttle() {
        requestMutex.withLock {
            val wait = lastRequestAt + MIN_REQUEST_INTERVAL - System.currentTimeMillis()
            if (wait > 0) delay(wait)
            lastRequestAt = System.currentTimeMillis()
        }
    }

    private fun HttpUrl.endpoint(path: String): HttpUrl.Builder = newBuilder().encodedPath(path)

    private fun MangaDto.toSourceManga(withPeople: Boolean = false): SourceManga {
        val attributes = attributes
        val title = attributes.title[lang]
            ?: attributes.altTitles.firstNotNullOfOrNull { it[lang] }
            ?: attributes.title["en"]
            ?: attributes.altTitles.firstNotNullOfOrNull { it["en"] }
            ?: attributes.title.values.firstOrNull()
            ?: id
        val cover = relationships.firstOrNull { it.type == "cover_art" }?.attributes?.fileName
        val people = { type: String ->
            relationships.filter { it.type == type }.mapNotNull { it.attributes?.name }.distinct().joinToString()
                .takeIf { it.isNotEmpty() }
        }
        return SourceManga(
            url = id,
            title = title,
            author = if (withPeople) people("author") else null,
            artist = if (withPeople) people("artist") else null,
            description = attributes.description[lang] ?: attributes.description["en"],
            genres = attributes.tags
                .filter { it.attributes.group == "genre" || it.attributes.group == "theme" }
                .mapNotNull { it.attributes.name["en"] },
            status = when (attributes.status) {
                "ongoing" -> MangaStatus.ONGOING
                "completed" -> MangaStatus.COMPLETED
                "hiatus" -> MangaStatus.ON_HIATUS
                "cancelled" -> MangaStatus.CANCELLED
                else -> MangaStatus.UNKNOWN
            },
            thumbnailUrl = cover?.let { "$COVERS/$id/$it.512.jpg" },
        )
    }

    private fun ChapterDto.toSourceChapter(): SourceChapter {
        val volume = attributes.volume?.takeIf { it.isNotBlank() }
        val chapter = attributes.chapter?.takeIf { it.isNotBlank() }
        val title = attributes.title?.takeIf { it.isNotBlank() }
        val name = buildString {
            if (volume != null) append("Vol.$volume ")
            if (chapter != null) append("Ch.$chapter") else if (title == null) append("Oneshot")
            if (title != null) {
                if (isNotEmpty()) append(" - ")
                append(title)
            }
        }
        val groups = relationships
            .filter { it.type == "scanlation_group" }
            .mapNotNull { it.attributes?.name }
            .joinToString()
            .takeIf { it.isNotEmpty() }
        return SourceChapter(
            url = id,
            name = name,
            chapterNumber = chapter?.toDoubleOrNull() ?: -1.0,
            scanlator = groups,
            dateUpload = attributes.publishAt?.let { runCatching { Instant.parse(it).toEpochMilliseconds() }.getOrNull() }
                ?: 0L,
        )
    }

    private companion object {
        const val NAME = "MangaDex"
        const val COVERS = "https://uploads.mangadex.org/covers"
        const val PAGE_SIZE = 20
        const val FEED_PAGE_SIZE = 500
        const val MIN_REQUEST_INTERVAL = 250L
        const val IMAGE_ATTEMPTS = 6
        const val IMAGE_RETRY_DELAY = 1000L
        const val HTTP_NOT_FOUND = 404
        const val HTTP_SERVER_ERROR = 500

        const val SORT = "Sort by"
        const val CONTENT_RATING = "Content rating"
        const val RELEVANCE = "relevance"
        const val FOLLOWED = "followedCount"
        const val LATEST_UPLOAD = "latestUploadedChapter"

        val API = "https://api.mangadex.org".toHttpUrl()

        val SORT_OPTIONS = listOf(
            "Popular" to FOLLOWED,
            "Latest upload" to LATEST_UPLOAD,
            "Rating" to "rating",
            "Newest" to "createdAt",
            "Relevance" to RELEVANCE,
        )

        val CONTENT_RATINGS = listOf(
            "Safe" to "safe",
            "Suggestive" to "suggestive",
            "Erotica" to "erotica",
            "Pornographic" to "pornographic",
        )

        val DEFAULT_RATINGS = listOf("safe", "suggestive")
    }
}
