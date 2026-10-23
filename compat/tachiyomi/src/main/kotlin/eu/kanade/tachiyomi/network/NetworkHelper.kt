package eu.kanade.tachiyomi.network

import okhttp3.OkHttpClient
import com.nilemnod.mangareader.core.network.NetworkHelper as CoreNetworkHelper

class NetworkHelper(private val core: CoreNetworkHelper) {

    val cookieJar = AndroidCookieJar()

    val client: OkHttpClient
        get() = core.client

    /**
     * @deprecated Since extension-lib 1.5
     */
    @Deprecated("The regular client handles Cloudflare by default")
    @Suppress("UNUSED")
    val cloudflareClient: OkHttpClient get() = client

    fun defaultUserAgentProvider() = core.defaultUserAgentProvider()
}
