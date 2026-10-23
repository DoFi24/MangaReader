package com.nilemnod.mangareader.feature.downloads

import com.nilemnod.mangareader.domain.download.model.Download
import kotlinx.coroutines.flow.Flow

interface DownloadQueue {

    val queueState: Flow<List<Download>>

    val isRunning: Flow<Boolean>

    fun start()

    suspend fun pause()

    suspend fun clear()

    suspend fun reorder(downloads: List<Download>)

    suspend fun cancel(downloads: List<Download>)
}
