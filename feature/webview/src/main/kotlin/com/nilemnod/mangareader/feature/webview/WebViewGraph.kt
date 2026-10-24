package com.nilemnod.mangareader.feature.webview

import android.content.Context
import com.nilemnod.mangareader.core.metro.metroGraph
import com.nilemnod.mangareader.core.network.NetworkHelper
import com.nilemnod.mangareader.domain.source.service.SourceManager
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo

@ContributesTo(AppScope::class)
interface WebViewGraph {
    val sourceManager: SourceManager
    val networkHelper: NetworkHelper
}

val Context.webViewGraph: WebViewGraph
    get() = metroGraph<WebViewGraph>()
