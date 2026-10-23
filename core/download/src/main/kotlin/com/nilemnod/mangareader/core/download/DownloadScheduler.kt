package com.nilemnod.mangareader.core.download

import kotlinx.coroutines.flow.Flow

interface DownloadScheduler {

    val isRunning: Flow<Boolean>

    fun start()
}
