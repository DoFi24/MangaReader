package com.nilemnod.mangareader.app.data.download

import com.nilemnod.mangareader.core.download.DownloadManager
import com.nilemnod.mangareader.domain.download.model.Download
import com.nilemnod.mangareader.feature.downloads.DownloadQueue
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class DownloadManagerQueue(
    private val downloadManager: DownloadManager,
) : DownloadQueue {

    override val queueState: Flow<List<Download>>
        get() = downloadManager.queueState

    override val isRunning: Flow<Boolean>
        get() = downloadManager.isDownloaderRunning

    override fun start() {
        downloadManager.startDownloads()
    }

    override suspend fun pause() {
        downloadManager.pauseDownloads()
    }

    override suspend fun clear() {
        downloadManager.clearQueue()
    }

    override suspend fun reorder(downloads: List<Download>) {
        downloadManager.reorderQueue(downloads)
    }

    override suspend fun cancel(downloads: List<Download>) {
        downloadManager.cancelQueuedDownloads(downloads)
    }
}
