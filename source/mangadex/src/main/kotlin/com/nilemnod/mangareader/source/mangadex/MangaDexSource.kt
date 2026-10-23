package com.nilemnod.mangareader.source.mangadex

import com.nilemnod.mangareader.core.network.GET
import com.nilemnod.mangareader.core.network.HttpException
import com.nilemnod.mangareader.core.network.NetworkHelper
import com.nilemnod.mangareader.core.network.await
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
import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Response
import java.io.IOException
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

    private val nodeMutex = Mutex()

    private val nodes = object : LinkedHashMap<String, AtHomeNode>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, AtHomeNode>): Boolean = size > MAX_NODES
    }

    private var lastRequestAt = 0L

    override fun headersBuilder(): Headers.Builder = Headers.Builder().add("User-Agent", USER_AGENT)

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
            if (order !=
                RELEVANCE
            ) {
                addQueryParameter("order[$order]", "desc")
            } else {
                addQueryParameter("order[relevance]", "desc")
            }
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
        val node = node(chapter.url)
        return node.chapter.data.mapIndexed { index, file ->
            val saver = node.chapter.dataSaver.getOrNull(index).orEmpty()
            Page(
                index,
                url = "${chapter.url}/$file/$saver",
                imageUrl = node.imageUrl(file, saver, useSaver = false),
            )
        }
    }

    override suspend fun openImage(
        page: Page,
        resumeFrom: Long,
        onProgress: (bytesRead: Long, contentLength: Long) -> Unit,
    ): ImageContent {
        val parts = page.url.split('/', limit = 3).takeIf { it.size == 3 }
        var attempt = 0
        while (true) {
            if (parts != null) adoptNode(page, parts)
            try {
                return super.openImage(page, resumeFrom, onProgress)
            } catch (e: SourceHttpException) {
                val retryable = e.code == HTTP_NOT_FOUND || e.code >= HTTP_SERVER_ERROR
                if (!retryable || ++attempt >= IMAGE_ATTEMPTS) throw e
                delay(IMAGE_RETRY_DELAY * attempt)
                if (parts != null) refreshNode(page, parts, useSaver = attempt % 2 == 0)
            }
        }
    }

    private suspend fun adoptNode(page: Page, parts: List<String>) {
        val (chapterId, file, saver) = parts
        val node = cachedNode(chapterId) ?: return
        val current = page.imageUrl.orEmpty()
        if (current.startsWith("${node.baseUrl}/")) return
        page.imageUrl = node.imageUrl(file, saver, useSaver = "/data-saver/" in current)
    }

    private suspend fun refreshNode(page: Page, parts: List<String>, useSaver: Boolean) {
        val (chapterId, file, saver) = parts
        val failedBase = page.imageUrl.orEmpty().substringBefore("/data")
        val node = try {
            node(chapterId, failedBase)
        } catch (e: HttpException) {
            return
        } catch (e: IOException) {
            return
        }
        page.imageUrl = node.imageUrl(file, saver, useSaver)
    }

    private suspend fun cachedNode(chapterId: String): AtHomeNode? = nodeMutex.withLock {
        nodes[chapterId]?.takeIf { it.isFresh() }
    }

    private suspend fun node(chapterId: String, failedBase: String? = null): AtHomeNode = nodeMutex.withLock {
        val cached = nodes[chapterId]
        val reusable = cached != null && cached.isFresh() &&
            (cached.baseUrl != failedBase || System.currentTimeMillis() - cached.fetchedAt < NODE_MIN_REFRESH)
        if (cached != null && reusable) return@withLock cached
        val loaded = AtHomeNode(fetch<AtHomeDto>(API.endpoint("/at-home/server/$chapterId").build()))
        nodes[chapterId] = loaded
        loaded
    }

    private suspend inline fun <reified T> fetch(url: HttpUrl): T {
        var attempt = 0
        while (true) {
            throttle()
            val response = withContext(Dispatchers.IO) { client.newCall(GET(url, headers)).await() }
            if (response.code == HTTP_TOO_MANY_REQUESTS && ++attempt < RATE_LIMIT_ATTEMPTS) {
                val wait = response.retryDelay()
                response.close()
                delay(wait)
                continue
            }
            return withContext(Dispatchers.IO) {
                response.use {
                    if (!it.isSuccessful) throw HttpException(it.code)
                    json.decodeFromString<T>(it.body.string())
                }
            }
        }
    }

    private fun Response.retryDelay(): Long {
        val resetAt = header("X-RateLimit-Retry-After")?.toLongOrNull()?.let { it * 1000 - System.currentTimeMillis() }
        val retryAfter = header("Retry-After")?.toLongOrNull()?.let { it * 1000 }
        return (resetAt ?: retryAfter ?: DEFAULT_RATE_LIMIT_DELAY).coerceIn(
            DEFAULT_RATE_LIMIT_DELAY,
            MAX_RATE_LIMIT_DELAY,
        )
    }

    private class AtHomeNode(val dto: AtHomeDto) {

        val fetchedAt: Long = System.currentTimeMillis()

        val baseUrl: String get() = dto.baseUrl

        val chapter: AtHomeChapterDto get() = dto.chapter

        fun isFresh(): Boolean = System.currentTimeMillis() - fetchedAt < NODE_TTL

        fun imageUrl(file: String, saver: String, useSaver: Boolean): String =
            if (useSaver && saver.isNotEmpty()) {
                "$baseUrl/data-saver/${chapter.hash}/$saver"
            } else {
                "$baseUrl/data/${chapter.hash}/$file"
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
            if (chapter != null) {
                append("Ch.$chapter")
            } else if (title == null) {
                append("Oneshot")
            }
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
            dateUpload =
            attributes.publishAt?.let { runCatching { Instant.parse(it).toEpochMilliseconds() }.getOrNull() }
                ?: 0L,
        )
    }

    private companion object {
        const val NAME = "MangaDex"
        const val USER_AGENT = "MangaReader/0.1 (personal reader)"
        const val COVERS = "https://uploads.mangadex.org/covers"
        const val PAGE_SIZE = 20
        const val FEED_PAGE_SIZE = 500
        const val MIN_REQUEST_INTERVAL = 250L
        const val IMAGE_ATTEMPTS = 4
        const val IMAGE_RETRY_DELAY = 1000L
        const val RATE_LIMIT_ATTEMPTS = 4
        const val DEFAULT_RATE_LIMIT_DELAY = 2000L
        const val MAX_RATE_LIMIT_DELAY = 30_000L
        const val NODE_TTL = 10 * 60 * 1000L
        const val NODE_MIN_REFRESH = 5000L
        const val MAX_NODES = 32
        const val HTTP_NOT_FOUND = 404
        const val HTTP_TOO_MANY_REQUESTS = 429
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
