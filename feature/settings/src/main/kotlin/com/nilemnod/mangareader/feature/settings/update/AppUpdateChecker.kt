package com.nilemnod.mangareader.feature.settings.update

import com.nilemnod.mangareader.core.common.AppBuild
import com.nilemnod.mangareader.domain.release.interactor.GetApplicationRelease
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Inject
class AppUpdateChecker(
    private val getApplicationRelease: GetApplicationRelease,
) {

    suspend fun checkForUpdate(forceCheck: Boolean = false): GetApplicationRelease.Result {
        return withContext(Dispatchers.IO) {
            getApplicationRelease.await(
                GetApplicationRelease.Arguments(
                    AppBuild.isFoss,
                    AppBuild.isNightly,
                    AppBuild.info.commitCount.toInt(),
                    AppBuild.versionName,
                    AppBuild.info.updateRepository,
                    forceCheck,
                ),
            )
        }
    }
}
