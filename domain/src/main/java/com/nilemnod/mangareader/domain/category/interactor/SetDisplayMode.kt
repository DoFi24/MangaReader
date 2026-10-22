package com.nilemnod.mangareader.domain.category.interactor

import dev.zacsweers.metro.Inject
import com.nilemnod.mangareader.domain.library.model.LibraryDisplayMode
import com.nilemnod.mangareader.domain.library.service.LibraryPreferences

@Inject
class SetDisplayMode(
    private val preferences: LibraryPreferences,
) {

    fun await(display: LibraryDisplayMode) {
        preferences.displayMode.set(display)
    }
}
