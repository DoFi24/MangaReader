package com.nilemnod.mangareader.app.ui.webview

import android.app.assist.AssistContent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.core.net.toUri
import com.nilemnod.mangareader.app.di.appGraph
import com.nilemnod.mangareader.app.presentation.webview.WebViewScreenContent
import com.nilemnod.mangareader.app.ui.base.activity.BaseActivity
import com.nilemnod.mangareader.app.util.view.setComposeContent
import com.nilemnod.mangareader.core.android.system.openInBrowser
import com.nilemnod.mangareader.core.android.system.toShareIntent
import com.nilemnod.mangareader.core.common.util.system.WebViewUtil
import com.nilemnod.mangareader.core.common.util.system.logcat
import com.nilemnod.mangareader.core.common.util.system.toWebViewHeaders
import com.nilemnod.mangareader.core.common.util.system.toast
import com.nilemnod.mangareader.core.network.NetworkHelper
import com.nilemnod.mangareader.core.ui.screens.LoadingScreen
import com.nilemnod.mangareader.domain.source.service.SourceManager
import com.nilemnod.mangareader.i18n.MR
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import logcat.LogPriority
import okhttp3.Headers.Companion.toHeaders
import okhttp3.HttpUrl.Companion.toHttpUrl
import com.nilemnod.mangareader.core.ui.R as UiR

class WebViewActivity : BaseActivity() {

    @Inject private lateinit var sourceManager: SourceManager

    @Inject private lateinit var network: NetworkHelper

    private var assistUrl: String? = null

    init {
        registerSecureActivity(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(
                OVERRIDE_TRANSITION_OPEN,
                UiR.anim.shared_axis_x_push_enter,
                UiR.anim.shared_axis_x_push_exit,
            )
        } else {
            @Suppress("DEPRECATION")
            overridePendingTransition(UiR.anim.shared_axis_x_push_enter, UiR.anim.shared_axis_x_push_exit)
        }
        super.onCreate(savedInstanceState)
        appGraph.inject(this)

        if (!WebViewUtil.supportsWebView(this)) {
            toast(MR.strings.information_webview_required, Toast.LENGTH_LONG)
            finish()
            return
        }

        val url = intent.extras?.getString(URL_KEY) ?: return
        assistUrl = url

        setComposeContent {
            // Null until the source it belongs to has been resolved
            val headers by produceState<Map<String, String>?>(initialValue = null) {
                val source = sourceManager.get(intent.extras!!.getLong(SOURCE_KEY))?.takeIf { it.web != null }
                value = withContext(Dispatchers.IO) {
                    try {
                        source?.web?.headers?.toHeaders()?.toWebViewHeaders().orEmpty()
                    } catch (e: Exception) {
                        logcat(LogPriority.ERROR, e) { "Failed to build headers" }
                        emptyMap()
                    }
                }
            }

            if (headers == null) {
                LoadingScreen()
                return@setComposeContent
            }

            WebViewScreenContent(
                onNavigateUp = { finish() },
                initialTitle = intent.extras?.getString(TITLE_KEY),
                url = url,
                headers = headers.orEmpty(),
                defaultUserAgentProvider = network::defaultUserAgentProvider,
                onUrlChange = { assistUrl = it },
                onShare = this::shareWebpage,
                onOpenInBrowser = this::openInBrowser,
                onClearCookies = this::clearCookies,
            )
        }
    }

    override fun onProvideAssistContent(outContent: AssistContent) {
        super.onProvideAssistContent(outContent)
        assistUrl?.let { outContent.webUri = it.toUri() }
    }

    override fun finish() {
        super.finish()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(
                OVERRIDE_TRANSITION_CLOSE,
                UiR.anim.shared_axis_x_pop_enter,
                UiR.anim.shared_axis_x_pop_exit,
            )
        } else {
            @Suppress("DEPRECATION")
            overridePendingTransition(UiR.anim.shared_axis_x_pop_enter, UiR.anim.shared_axis_x_pop_exit)
        }
    }

    private fun shareWebpage(url: String) {
        try {
            startActivity(url.toUri().toShareIntent(this, type = "text/plain"))
        } catch (e: Exception) {
            toast(e.message)
        }
    }

    private fun openInBrowser(url: String) {
        openInBrowser(url, forceDefaultBrowser = true)
    }

    private fun clearCookies(url: String) {
        val cleared = network.cookieJar.remove(url.toHttpUrl())
        logcat { "Cleared $cleared cookies for: $url" }
    }

    companion object {
        private const val URL_KEY = "url_key"
        private const val SOURCE_KEY = "source_key"
        private const val TITLE_KEY = "title_key"

        fun newIntent(context: Context, url: String, sourceId: Long? = null, title: String? = null): Intent {
            return Intent(context, WebViewActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtra(URL_KEY, url)
                putExtra(SOURCE_KEY, sourceId)
                putExtra(TITLE_KEY, title)
            }
        }
    }
}
