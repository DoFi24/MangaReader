package com.nilemnod.mangareader.app.data.download

import android.content.Context
import com.nilemnod.mangareader.core.download.DownloadScheduler
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow

@Inject
@ContributesBinding(AppScope::class)
class WorkDownloadScheduler(
    private val context: Context,
) : DownloadScheduler {

    override val isRunning: Flow<Boolean>
        get() = DownloadWorker.isRunningFlow(context)

    override fun start() {
        DownloadWorker.start(context)
    }
}
