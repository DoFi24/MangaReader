package com.nilemnod.mangareader.domain.category.interactor

import com.nilemnod.mangareader.core.common.util.system.logcat
import com.nilemnod.mangareader.domain.category.model.NewCategory
import com.nilemnod.mangareader.domain.category.repository.CategoryRepository
import com.nilemnod.mangareader.domain.library.service.LibraryPreferences
import dev.zacsweers.metro.Inject
import logcat.LogPriority

@Inject
class CreateCategoryWithName(
    private val categoryRepository: CategoryRepository,
    private val preferences: LibraryPreferences,
) {

    private val initialFlags: Long
        get() {
            val sort = preferences.sortingMode.get()
            return sort.type.flag or sort.direction.flag
        }

    suspend fun await(name: String): Result {
        return try {
            categoryRepository.insert(NewCategory(name = name, flags = initialFlags))
            Result.Success
        } catch (e: Exception) {
            logcat(LogPriority.ERROR, e)
            Result.InternalError(e)
        }
    }

    sealed interface Result {
        data object Success : Result
        data class InternalError(val error: Throwable) : Result
    }
}
