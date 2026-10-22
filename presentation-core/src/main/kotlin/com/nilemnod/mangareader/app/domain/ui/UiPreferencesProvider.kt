package com.nilemnod.mangareader.app.domain.ui

import android.content.Context
import com.nilemnod.mangareader.core.metro.metroGraph

interface UiPreferencesProvider {
    val uiPreferences: UiPreferences
}

val Context.appUiPreferences: UiPreferences
    get() = metroGraph<UiPreferencesProvider>().uiPreferences
