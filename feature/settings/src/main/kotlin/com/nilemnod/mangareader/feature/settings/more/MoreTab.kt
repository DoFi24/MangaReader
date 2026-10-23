package com.nilemnod.mangareader.feature.settings.more

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.nilemnod.mangareader.core.download.DownloadManager
import com.nilemnod.mangareader.core.navigation.SharedScreen
import com.nilemnod.mangareader.core.navigation.screen
import com.nilemnod.mangareader.core.ui.i18n.stringResource
import com.nilemnod.mangareader.core.ui.preference.asState
import com.nilemnod.mangareader.core.ui.util.Tab
import com.nilemnod.mangareader.domain.base.service.ModePreferences
import com.nilemnod.mangareader.feature.settings.SettingsScreen
import com.nilemnod.mangareader.feature.settings.more.MoreScreen
import com.nilemnod.mangareader.i18n.MR
import com.nilemnod.mangareader.icons.custommaterialsymbols.CustomMaterialSymbols
import com.nilemnod.mangareader.icons.custommaterialsymbols.edge.More
import com.nilemnod.mangareader.icons.custommaterialsymbols.edgefilled.More
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data object MoreTab : Tab {

    override val options: TabOptions
        @Composable
        get() {
            val isSelected = LocalTabNavigator.current.current.key == key
            return TabOptions(
                index = 4u,
                title = stringResource(MR.strings.label_more),
                icon = rememberVectorPainter(
                    if (isSelected) CustomMaterialSymbols.EdgeFilled.More else CustomMaterialSymbols.Edge.More,
                ),
            )
        }

    override suspend fun onReselect(navigator: Navigator) {
        navigator.push(SettingsScreen())
    }

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = metroViewModel<MoreViewModel>()
        val downloadQueueState by viewModel.downloadQueueState.collectAsState()
        MoreScreen(
            downloadQueueStateProvider = { downloadQueueState },
            downloadedOnly = viewModel.downloadedOnly,
            onDownloadedOnlyChange = { viewModel.downloadedOnly = it },
            incognitoMode = viewModel.incognitoMode,
            onIncognitoModeChange = { viewModel.incognitoMode = it },
            onClickDownloadQueue = { navigator.push(SharedScreen.DownloadQueue.screen()) },
            onClickCategories = { navigator.push(SharedScreen.Category.screen()) },
            onClickStats = { navigator.push(SharedScreen.Stats.screen()) },
            onClickDataAndStorage = { navigator.push(SettingsScreen(SettingsScreen.Destination.DataAndStorage)) },
            onClickSettings = { navigator.push(SettingsScreen()) },
            onClickAbout = { navigator.push(SettingsScreen(SettingsScreen.Destination.About)) },
        )
    }
}

@Inject
@ViewModelKey
@ContributesIntoMap(AppScope::class)
class MoreViewModel(
    private val downloadManager: DownloadManager,
    preferences: ModePreferences,
) : ViewModel() {

    var downloadedOnly by preferences.downloadedOnly.asState(viewModelScope)
    var incognitoMode by preferences.incognitoMode.asState(viewModelScope)

    private var _downloadQueueState: MutableStateFlow<DownloadQueueState> = MutableStateFlow(DownloadQueueState.Stopped)
    val downloadQueueState: StateFlow<DownloadQueueState> = _downloadQueueState.asStateFlow()

    init {
        // Handle running/paused status change and queue progress updating
        viewModelScope.launch {
            combine(
                downloadManager.isDownloaderRunning,
                downloadManager.queueState,
            ) { isRunning, downloadQueue -> Pair(isRunning, downloadQueue.size) }
                .collectLatest { (isDownloading, downloadQueueSize) ->
                    val pendingDownloadExists = downloadQueueSize != 0
                    _downloadQueueState.value = when {
                        !pendingDownloadExists -> DownloadQueueState.Stopped
                        !isDownloading -> DownloadQueueState.Paused(downloadQueueSize)
                        else -> DownloadQueueState.Downloading(downloadQueueSize)
                    }
                }
        }
    }
}

sealed interface DownloadQueueState {
    data object Stopped : DownloadQueueState
    data class Paused(val pending: Int) : DownloadQueueState
    data class Downloading(val pending: Int) : DownloadQueueState
}
