package com.nilemnod.mangareader.feature.settings.screen.advanced

import androidx.compose.runtime.Composable
import com.nilemnod.mangareader.feature.settings.Preference

interface AdvancedSettingsGroups {

    @Composable
    fun groups(): List<Preference.PreferenceGroup>
}
