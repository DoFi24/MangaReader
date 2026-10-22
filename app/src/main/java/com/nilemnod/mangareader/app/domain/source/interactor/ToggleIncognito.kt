package com.nilemnod.mangareader.app.domain.source.interactor

import com.nilemnod.mangareader.app.domain.source.service.SourcePreferences
import com.nilemnod.mangareader.core.common.preference.getAndSet
import dev.zacsweers.metro.Inject

@Inject
class ToggleIncognito(
    private val preferences: SourcePreferences,
) {
    fun await(extensions: String, enable: Boolean) {
        preferences.incognitoExtensions.getAndSet {
            if (enable) it.plus(extensions) else it.minus(extensions)
        }
    }
}
