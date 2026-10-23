package com.nilemnod.mangareader.domain.source.api

import androidx.preference.PreferenceScreen

interface SourceSettings {

    val preferencesName: String

    fun setupScreen(screen: PreferenceScreen)
}
