package com.nilemnod.mangareader.core.backup.create.creators

import com.nilemnod.mangareader.core.backup.models.BackupCategory
import com.nilemnod.mangareader.core.backup.models.backupCategoryMapper
import com.nilemnod.mangareader.domain.category.interactor.GetCategories
import com.nilemnod.mangareader.domain.category.model.Category
import dev.zacsweers.metro.Inject

@Inject
class CategoriesBackupCreator(
    private val getCategories: GetCategories,
) {

    suspend operator fun invoke(): List<BackupCategory> {
        return getCategories.await()
            .filterNot(Category::isSystemCategory)
            .map(backupCategoryMapper)
    }
}
