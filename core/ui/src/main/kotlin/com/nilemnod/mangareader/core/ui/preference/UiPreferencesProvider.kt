package com.nilemnod.mangareader.core.ui.preference

import android.content.Context
import com.nilemnod.mangareader.core.metro.metroGraph

interface UiPreferencesProvider {
    val uiPreferences: UiPreferences
}

val Context.appUiPreferences: UiPreferences
    get() = metroGraph<UiPreferencesProvider>().uiPreferences
