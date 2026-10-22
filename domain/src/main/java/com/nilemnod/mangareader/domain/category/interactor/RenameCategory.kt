package com.nilemnod.mangareader.domain.category.interactor

import com.nilemnod.mangareader.core.common.util.system.logcat
import com.nilemnod.mangareader.domain.category.model.Category
import com.nilemnod.mangareader.domain.category.repository.CategoryRepository
import dev.zacsweers.metro.Inject
import logcat.LogPriority

@Inject
class RenameCategory(
    private val categoryRepository: CategoryRepository,
) {

    suspend fun await(categoryId: Long, name: String): Result {
        return try {
            categoryRepository.updateName(categoryId = categoryId, name = name)
            Result.Success
        } catch (e: Exception) {
            logcat(LogPriority.ERROR, e)
            Result.InternalError(e)
        }
    }

    suspend fun await(category: Category, name: String) = await(category.id, name)

    sealed interface Result {
        data object Success : Result
        data class InternalError(val error: Throwable) : Result
    }
}
