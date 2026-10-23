package com.nilemnod.mangareader.feature.settings.update

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nilemnod.mangareader.core.android.storage.getUriCompat
import com.nilemnod.mangareader.core.android.storage.saveTo
import com.nilemnod.mangareader.core.common.util.system.logcat
import com.nilemnod.mangareader.core.network.GET
import com.nilemnod.mangareader.core.network.NetworkHelper
import com.nilemnod.mangareader.core.network.ProgressListener
import com.nilemnod.mangareader.core.network.awaitSuccess
import com.nilemnod.mangareader.core.network.newCachelessCallWithProgress
import com.nilemnod.mangareader.domain.base.service.AppStatePreferences
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import logcat.LogPriority
import java.io.File

@AssistedInject
class NewUpdateScreenModel(
    @Assisted changelogInfo: String,
    @Assisted private val downloadLink: String,
    private val context: Context,
    private val network: NetworkHelper,
    private val appStatePreferences: AppStatePreferences,
) : ViewModel() {

    val state: StateFlow<NewUpdateScreenModel.State>
        field = MutableStateFlow<NewUpdateScreenModel.State>(State(changelogInfo = changelogInfo))

    @AssistedFactory
    @ManualViewModelAssistedFactoryKey
    @ContributesIntoMap(AppScope::class)
    interface Factory : ManualViewModelAssistedFactory {
        fun create(changelogInfo: String, downloadLink: String): NewUpdateScreenModel
    }

    private val apkFile: File
        get() = File(context.externalCacheDir, "update.apk")

    private var downloadJob: Job? = null

    fun startDownload() {
        if (downloadJob?.isActive == true) return

        downloadJob = viewModelScope.launch {
            state.update { it.copy(downloadProgress = 0, stage = Stage.Downloading) }
            try {
                withContext(Dispatchers.IO) { downloadApk() }
                state.update { it.copy(downloadProgress = 100, stage = Stage.Downloaded) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logcat(LogPriority.ERROR, e)
                apkFile.delete()
                state.update { it.copy(stage = Stage.Failed) }
            }
        }
    }

    private suspend fun downloadApk() {
        val progressListener = object : ProgressListener {
            // Progress of the download
            var savedProgress = 0

            // Keep track of the last update sent to avoid updating the state too often.
            var lastTick = 0L

            override fun update(bytesRead: Long, contentLength: Long, done: Boolean) {
                val progress = (100 * (bytesRead.toFloat() / contentLength)).toInt()
                val currentTime = System.currentTimeMillis()
                if (progress > savedProgress && currentTime - 200 > lastTick) {
                    savedProgress = progress
                    lastTick = currentTime
                    state.update { it.copy(downloadProgress = progress) }
                }
            }
        }

        val response = network.client.newCachelessCallWithProgress(GET(downloadLink), progressListener).awaitSuccess()
        response.body.source().saveTo(apkFile)
    }

    fun skipUpdate(versionName: String) {
        downloadJob?.cancel()
        appStatePreferences.skippedAppUpdate.set(versionName)
    }

    fun installUpdate() {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkFile.getUriCompat(context), APK_MIME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        context.startActivity(intent)
    }

    @Immutable
    data class State(
        val changelogInfo: String,
        val downloadProgress: Int = 0,
        val stage: Stage = Stage.Available,
    )

    enum class Stage {
        Available,
        Downloading,
        Downloaded,
        Failed,
    }
}

private const val APK_MIME = "application/vnd.android.package-archive"
