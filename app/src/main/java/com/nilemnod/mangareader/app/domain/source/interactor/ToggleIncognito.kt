package com.nilemnod.mangareader.app.domain.source.interactor

import dev.zacsweers.metro.Inject
import com.nilemnod.mangareader.app.domain.source.service.SourcePreferences
import com.nilemnod.mangareader.core.common.preference.getAndSet

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
