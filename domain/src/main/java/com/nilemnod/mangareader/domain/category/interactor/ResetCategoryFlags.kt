package com.nilemnod.mangareader.domain.category.interactor

import dev.zacsweers.metro.Inject
import com.nilemnod.mangareader.domain.category.repository.CategoryRepository
import com.nilemnod.mangareader.domain.library.model.plus
import com.nilemnod.mangareader.domain.library.service.LibraryPreferences

@Inject
class ResetCategoryFlags(
    private val preferences: LibraryPreferences,
    private val categoryRepository: CategoryRepository,
) {

    suspend fun await() {
        val sort = preferences.sortingMode.get()
        categoryRepository.updateAllFlags(sort.type + sort.direction)
    }
}
