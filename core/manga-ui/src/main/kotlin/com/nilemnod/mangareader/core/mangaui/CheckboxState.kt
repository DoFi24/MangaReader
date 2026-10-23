package com.nilemnod.mangareader.core.mangaui

import androidx.compose.ui.state.ToggleableState
import com.nilemnod.mangareader.core.common.preference.CheckboxState

fun <T> CheckboxState.TriState<T>.asToggleableState() = when (this) {
    is CheckboxState.TriState.Exclude -> ToggleableState.Indeterminate
    is CheckboxState.TriState.Include -> ToggleableState.On
    is CheckboxState.TriState.None -> ToggleableState.Off
}
