package com.nilemnod.mangareader.domain.source.interactor

import com.nilemnod.mangareader.core.common.preference.getAndSet
import com.nilemnod.mangareader.domain.source.model.Source
import com.nilemnod.mangareader.domain.source.service.SourcePreferences
import dev.zacsweers.metro.Inject

@Inject
class ToggleSourcePin(
    private val preferences: SourcePreferences,
) {

    fun await(source: Source) {
        val isPinned = source.id.toString() in preferences.pinnedSources.get()
        preferences.pinnedSources.getAndSet { pinned ->
            if (isPinned) pinned.minus("${source.id}") else pinned.plus("${source.id}")
        }
    }
}
