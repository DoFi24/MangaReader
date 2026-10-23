package com.nilemnod.mangareader.feature.settings.about

interface DebugInfoProvider {

    fun getDebugInfo(): String

    suspend fun dumpLogs(exception: Throwable? = null)
}
