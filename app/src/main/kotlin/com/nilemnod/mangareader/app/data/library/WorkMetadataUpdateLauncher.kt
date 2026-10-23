package com.nilemnod.mangareader.app.data.library

import android.content.Context
import com.nilemnod.mangareader.core.android.system.workManager
import com.nilemnod.mangareader.feature.settings.screen.advanced.MetadataUpdateLauncher
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

@Inject
@ContributesBinding(AppScope::class)
class WorkMetadataUpdateLauncher(
    private val context: Context,
) : MetadataUpdateLauncher {

    override suspend fun startNow(): Boolean {
        return MetadataUpdateWorker.startNow(context.workManager)
    }
}
