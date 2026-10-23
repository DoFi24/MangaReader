package com.nilemnod.mangareader.app.ui.reader.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nilemnod.mangareader.app.ui.reader.ReaderViewModel
import com.nilemnod.mangareader.core.reader.ReaderOrientation
import com.nilemnod.mangareader.core.reader.ReaderPreferences
import com.nilemnod.mangareader.core.reader.ReadingMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class ReaderSettingsViewModel(
    readerState: StateFlow<ReaderViewModel.State>,
    val onChangeReadingMode: (ReadingMode) -> Unit,
    val onChangeOrientation: (ReaderOrientation) -> Unit,
    val preferences: ReaderPreferences,
) : ViewModel() {

    val viewerFlow = readerState
        .map { it.viewer }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val mangaFlow = readerState
        .map { it.manga }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.Lazily, null)
}
