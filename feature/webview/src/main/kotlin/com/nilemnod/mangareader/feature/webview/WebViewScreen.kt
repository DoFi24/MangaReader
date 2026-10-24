package com.nilemnod.mangareader.feature.webview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.nilemnod.mangareader.core.ui.screens.LoadingScreen
import com.nilemnod.mangareader.core.ui.util.AssistContentScreen
import com.nilemnod.mangareader.core.ui.util.Screen
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel

class WebViewScreen(
    private val url: String,
    private val initialTitle: String? = null,
    private val sourceId: Long? = null,
) : Screen(), AssistContentScreen {

    private var assistUrl: String? = null

    override fun onProvideAssistUrl() = assistUrl

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val context = LocalContext.current
        val viewModel =
            assistedMetroViewModel<WebViewViewModel, WebViewViewModel.Factory> { create(sourceId = sourceId) }

        val headers by viewModel.headers.collectAsState()
        if (headers == null) {
            LoadingScreen()
            return
        }

        WebViewScreenContent(
            onNavigateUp = { navigator.pop() },
            initialTitle = initialTitle,
            url = url,
            headers = headers.orEmpty(),
            defaultUserAgentProvider = viewModel::defaultUserAgentProvider,
            onUrlChange = { assistUrl = it },
            onShare = { viewModel.shareWebpage(context, it) },
            onOpenInBrowser = { viewModel.openInBrowser(context, it) },
            onClearCookies = viewModel::clearCookies,
        )
    }
}
