package com.nilemnod.mangareader.source.common

import com.nilemnod.mangareader.core.network.GET
import com.nilemnod.mangareader.core.network.HttpException
import com.nilemnod.mangareader.core.network.NetworkHelper
import com.nilemnod.mangareader.core.network.ProgressListener
import com.nilemnod.mangareader.core.network.awaitSuccess
import com.nilemnod.mangareader.core.network.newCachelessCallWithProgress
import com.nilemnod.mangareader.domain.source.api.ContentSource
import com.nilemnod.mangareader.domain.source.api.ImageContent
import com.nilemnod.mangareader.domain.source.api.Page
import com.nilemnod.mangareader.domain.source.api.SourceChapter
import com.nilemnod.mangareader.domain.source.api.SourceHttpException
import com.nilemnod.mangareader.domain.source.api.SourceManga
import com.nilemnod.mangareader.domain.source.api.WebAccess
import okhttp3.Call
import okhttp3.Headers
import okhttp3.OkHttpClient
import java.security.MessageDigest

abstract class HttpContentSource(
    protected val network: NetworkHelper,
) : ContentSource {

    abstract val baseUrl: String

    protected open val client: OkHttpClient
        get() = network.client

    protected open fun headersBuilder(): Headers.Builder = Headers.Builder()
        .add("User-Agent", network.defaultUserAgentProvider())

    protected val headers: Headers by lazy { headersBuilder().build() }

    abstract fun mangaUrl(manga: SourceManga): String

    abstract fun chapterUrl(chapter: SourceChapter): String

    override val web: WebAccess by lazy {
        object : WebAccess {
            override val homeUrl: String
                get() = baseUrl

            override val headers: Map<String, String>
                get() = this@HttpContentSource.headers.toMap()

            override val client: Call.Factory
                get() = this@HttpContentSource.client

            override fun mangaUrl(manga: SourceManga): String = this@HttpContentSource.mangaUrl(manga)

            override fun chapterUrl(chapter: SourceChapter): String = this@HttpContentSource.chapterUrl(chapter)
        }
    }

    override suspend fun openImage(
        page: Page,
        resumeFrom: Long,
        onProgress: (bytesRead: Long, contentLength: Long) -> Unit,
    ): ImageContent {
        val url = page.imageUrl.takeUnless { it.isNullOrEmpty() } ?: imageUrl(page)
        val listener = object : ProgressListener {
            override fun update(bytesRead: Long, contentLength: Long, done: Boolean) {
                onProgress(bytesRead, contentLength)
            }
        }
        val response = try {
            client.newCachelessCallWithProgress(GET(url, headers), listener, resumeFrom).awaitSuccess()
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

    private companion object {
        const val PARTIAL_CONTENT = 206
    }
}

fun stableSourceId(name: String, lang: String, version: Int = 1): Long {
    val key = "${name.lowercase()}/$lang/$version"
    val bytes = MessageDigest.getInstance("MD5").digest(key.toByteArray())
    return (0..7).map { bytes[it].toLong() and 0xff shl 8 * (7 - it) }.reduce(Long::or) and Long.MAX_VALUE
}
