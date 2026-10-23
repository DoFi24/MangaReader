package com.nilemnod.mangareader.app.data.library

import android.content.Context
import com.nilemnod.mangareader.core.android.system.workManager
import com.nilemnod.mangareader.domain.category.model.Category
import com.nilemnod.mangareader.domain.library.service.LibraryUpdateLauncher
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

@Inject
@ContributesBinding(AppScope::class)
class WorkLibraryUpdateLauncher(
    private val context: Context,
) : LibraryUpdateLauncher {

    override suspend fun startNow(category: Category?): Boolean {
        return LibraryUpdateWorker.startNow(context.workManager, category)
    }

    override fun schedule(interval: Int?) {
        LibraryUpdateWorker.setupTask(context, interval)
    }
}
