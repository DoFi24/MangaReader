package com.nilemnod.mangareader.feature.settings.screen.advanced

interface MetadataUpdateLauncher {

    suspend fun startNow(): Boolean
}
