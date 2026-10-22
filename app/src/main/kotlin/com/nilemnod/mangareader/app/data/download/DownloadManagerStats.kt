package com.nilemnod.mangareader.app.data.download

import com.nilemnod.mangareader.feature.stats.DownloadStats
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class DownloadManagerStats(
    private val downloadManager: DownloadManager,
) : DownloadStats {

    override fun downloadCount(): Int = downloadManager.getDownloadCount()
}
