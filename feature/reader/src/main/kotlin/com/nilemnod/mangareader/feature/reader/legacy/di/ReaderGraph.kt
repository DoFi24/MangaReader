package com.nilemnod.mangareader.feature.reader.legacy.di

import com.nilemnod.mangareader.core.download.DownloadManager
import com.nilemnod.mangareader.core.reader.ReaderPreferences
import com.nilemnod.mangareader.core.ui.preference.UiPreferences
import com.nilemnod.mangareader.feature.reader.legacy.ReaderActivity
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory

@ContributesTo(AppScope::class)
interface ReaderGraph {
    fun inject(readerActivity: ReaderActivity)

    val viewModelFactory: MetroViewModelFactory
    val readerPreferences: ReaderPreferences
    val uiPreferences: UiPreferences
    val downloadManager: DownloadManager
}
