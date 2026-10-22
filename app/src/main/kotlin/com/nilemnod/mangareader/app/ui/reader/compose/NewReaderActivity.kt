package com.nilemnod.mangareader.app.ui.reader.compose

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.nilemnod.mangareader.app.di.AppGraph
import com.nilemnod.mangareader.app.ui.base.activity.BaseActivity
import com.nilemnod.mangareader.core.android.system.toShareIntent
import com.nilemnod.mangareader.core.common.util.system.toast
import com.nilemnod.mangareader.core.metro.metroGraph
import com.nilemnod.mangareader.core.ui.i18n.stringResource
import com.nilemnod.mangareader.core.ui.theme.MangaReaderTheme
import com.nilemnod.mangareader.feature.reader.ReaderScreen
import com.nilemnod.mangareader.feature.reader.image.createReaderImageLoader
import com.nilemnod.mangareader.feature.reader.model.ReaderHost
import com.nilemnod.mangareader.feature.reader.model.ReaderRemote
import com.nilemnod.mangareader.feature.reader.settings.ReadingMode
import com.nilemnod.mangareader.feature.reader.settings.SharedPreferencesReaderSettingsStore
import com.nilemnod.mangareader.i18n.MR
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class NewReaderActivity : BaseActivity() {

    private val graph: AppGraph by lazy { metroGraph() }

    private val viewModel by viewModels<NewReaderViewModel> { graph.viewModelFactory }

    private val settingsStore by lazy { SharedPreferencesReaderSettingsStore(this) }

    private val imageLoader by lazy { createReaderImageLoader(this) }

    private val remote = ReaderRemote()

    private val host = object : ReaderHost {
        override fun onPageChanged(chapterId: Long, pageIndex: Int) = viewModel.onPageChanged(chapterId, pageIndex)

        override fun onChapterFinished(chapterId: Long) = Unit

        override fun onRequestPreviousChapter() = viewModel.loadPrevious()

        override fun onRequestNextChapter() = viewModel.loadNext()

        override fun onAppendNextChapter() = viewModel.appendNextChapter()

        override fun onPrependPreviousChapter() = viewModel.prependPreviousChapter()

        override fun onReadingModeChanged(mode: ReadingMode) = viewModel.setReadingMode(mode)

        override fun onSavePage(chapterId: Long, pageIndex: Int) = viewModel.savePage(chapterId, pageIndex)

        override fun onSharePage(chapterId: Long, pageIndex: Int) = viewModel.sharePage(chapterId, pageIndex)

        override fun onExit() = finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowCompat.getInsetsController(window, window.decorView).apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.systemBars())
        }

        viewModel.events
            .onEach { event ->
                when (event) {
                    NewReaderViewModel.Event.PictureSaved -> toast(MR.strings.picture_saved)
                    is NewReaderViewModel.Event.SharePage -> {
                        startActivity(event.uri.toShareIntent(applicationContext, message = event.message))
                    }
                }
            }
            .launchIn(lifecycleScope)

        setContent {
            MangaReaderTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ReaderContent()
                }
            }
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val settings = settingsStore.settings.value
        val isVolumeKey = event.keyCode == KeyEvent.KEYCODE_VOLUME_UP || event.keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
        if (settings.volumeKeys && isVolumeKey) {
            if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
                val forward = (event.keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) != settings.invertVolumeKeys
                remote.step(if (forward) 1 else -1)
            }
            return true
        }
        return super.dispatchKeyEvent(event)
    }

    override fun finish() {
        viewModel.onActivityFinish()
        super.finish()
    }

    override fun onResume() {
        super.onResume()
        viewModel.restartReadTimer()
    }

    override fun onPause() {
        viewModel.saveHistory()
        super.onPause()
    }

    @Composable
    private fun ReaderContent() {
        val state by viewModel.state.collectAsStateWithLifecycle()
        when (val current = state) {
            NewReaderViewModel.UiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is NewReaderViewModel.UiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = current.error.message ?: current.error.toString(),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Button(onClick = ::finish) {
                        Text(stringResource(MR.strings.action_ok))
                    }
                }
            }
            is NewReaderViewModel.UiState.Content -> {
                ReaderScreen(
                    chapter = current.chapter,
                    chapters = current.chapters,
                    settingsStore = settingsStore,
                    host = host,
                    imageLoader = imageLoader,
                    remote = remote,
                )
            }
        }
    }
}
