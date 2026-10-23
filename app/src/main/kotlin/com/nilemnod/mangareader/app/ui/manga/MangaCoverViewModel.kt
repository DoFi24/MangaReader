package com.nilemnod.mangareader.app.ui.manga

import android.content.Context
import android.net.Uri
import androidx.compose.material3.SnackbarHostState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil3.asDrawable
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.size.Size
import com.nilemnod.mangareader.core.android.system.getBitmapOrNull
import com.nilemnod.mangareader.core.android.system.toShareIntent
import com.nilemnod.mangareader.core.common.i18n.stringResource
import com.nilemnod.mangareader.core.common.util.system.logcat
import com.nilemnod.mangareader.core.storage.CoverCache
import com.nilemnod.mangareader.core.storage.editCover
import com.nilemnod.mangareader.core.storage.saver.Image
import com.nilemnod.mangareader.core.storage.saver.ImageSaver
import com.nilemnod.mangareader.core.storage.saver.Location
import com.nilemnod.mangareader.domain.manga.interactor.GetManga
import com.nilemnod.mangareader.domain.manga.interactor.UpdateManga
import com.nilemnod.mangareader.domain.manga.model.Manga
import com.nilemnod.mangareader.i18n.MR
import com.nilemnod.mangareader.source.local.image.LocalCoverManager
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import logcat.LogPriority
import kotlin.time.Duration.Companion.seconds

@AssistedInject
class MangaCoverViewModel(
    @Assisted private val mangaId: Long,
    private val getManga: GetManga,
    private val imageSaver: ImageSaver,
    private val coverCache: CoverCache,
    private val updateManga: UpdateManga,
    private val coverManager: LocalCoverManager,
    val snackbarHostState: SnackbarHostState = SnackbarHostState(),
) : ViewModel() {

    @AssistedFactory
    @ManualViewModelAssistedFactoryKey
    @ContributesIntoMap(AppScope::class)
    interface Factory : ManualViewModelAssistedFactory {
        fun create(mangaId: Long): MangaCoverViewModel
    }

    val state: StateFlow<Manga?> = getManga.subscribe(mangaId)
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5.seconds), null)

    fun saveCover(context: Context) {
        viewModelScope.launch {
            try {
                saveCoverInternal(context, temp = false)
                snackbarHostState.showSnackbar(
                    context.stringResource(MR.strings.cover_saved),
                    withDismissAction = true,
                )
            } catch (e: Throwable) {
                logcat(LogPriority.ERROR, e)
                snackbarHostState.showSnackbar(
                    context.stringResource(MR.strings.error_saving_cover),
                    withDismissAction = true,
                )
            }
        }
    }

    fun shareCover(context: Context) {
        viewModelScope.launch {
            try {
                val uri = saveCoverInternal(context, temp = true) ?: return@launch
                withContext(Dispatchers.Main) {
                    context.startActivity(uri.toShareIntent(context))
                }
            } catch (e: Throwable) {
                logcat(LogPriority.ERROR, e)
                snackbarHostState.showSnackbar(
                    context.stringResource(MR.strings.error_sharing_cover),
                    withDismissAction = true,
                )
            }
        }
    }

    /**
     * Save manga cover Bitmap to picture or temporary share directory.
     *
     * @param context The context for building and executing the ImageRequest
     * @return the uri to saved file
     */
    private suspend fun saveCoverInternal(context: Context, temp: Boolean): Uri? {
        val manga = state.value ?: return null
        val req = ImageRequest.Builder(context)
            .data(manga)
            .size(Size.ORIGINAL)
            .build()

        return withContext(Dispatchers.IO) {
            val result = context.imageLoader.execute(req).image?.asDrawable(context.resources)

            // TODO: Handle animated cover
            val bitmap = result?.getBitmapOrNull() ?: return@withContext null
            imageSaver.save(
                Image.Cover(
                    bitmap = bitmap,
                    name = manga.title,
                    location = if (temp) Location.Cache else Location.Pictures.create(),
                ),
            )
        }
    }

    /**
     * Update cover with local file.
     *
     * @param context Context.
     * @param data uri of the cover resource.
     */
    fun hasCustomCover(manga: Manga): Boolean = coverCache.hasCustomCover(manga.id)

    fun editCover(context: Context, data: Uri) {
        val manga = state.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            context.contentResolver.openInputStream(data)?.use {
                try {
                    manga.editCover(coverManager, it, updateManga, coverCache)
                    notifyCoverUpdated(context)
                } catch (e: Exception) {
                    notifyFailedCoverUpdate(context, e)
                }
            }
        }
    }

    fun deleteCustomCover(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                coverCache.deleteCustomCover(mangaId)
                updateManga.awaitUpdateCoverLastModified(mangaId)
                notifyCoverUpdated(context)
            } catch (e: Exception) {
                notifyFailedCoverUpdate(context, e)
            }
        }
    }

    fun addToLibraryWarning(context: Context) {
        viewModelScope.launch {
            snackbarHostState.showSnackbar(
                context.stringResource(MR.strings.notification_first_add_to_library),
                withDismissAction = true,
            )
        }
    }

    private fun notifyCoverUpdated(context: Context) {
        viewModelScope.launch {
            snackbarHostState.showSnackbar(
                context.stringResource(MR.strings.cover_updated),
                withDismissAction = true,
            )
        }
    }

    private fun notifyFailedCoverUpdate(context: Context, e: Throwable) {
        viewModelScope.launch {
            snackbarHostState.showSnackbar(
                context.stringResource(MR.strings.notification_cover_update_failed),
                withDismissAction = true,
            )
            logcat(LogPriority.ERROR, e)
        }
    }
}
