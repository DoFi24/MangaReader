package com.nilemnod.mangareader.feature.deeplink

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nilemnod.mangareader.domain.chapter.interactor.GetChapterByUrlAndMangaId
import com.nilemnod.mangareader.domain.chapter.model.Chapter
import com.nilemnod.mangareader.domain.manga.interactor.NetworkToLocalManga
import com.nilemnod.mangareader.domain.manga.model.Manga
import com.nilemnod.mangareader.domain.manga.model.toDomainManga
import com.nilemnod.mangareader.domain.source.api.SourceChapter
import com.nilemnod.mangareader.domain.source.api.UriType
import com.nilemnod.mangareader.domain.source.service.RemoteMangaUpdater
import com.nilemnod.mangareader.domain.source.service.SourceManager
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@AssistedInject
class DeepLinkViewModel(
    @Assisted query: String,
    private val sourceManager: SourceManager,
    private val networkToLocalManga: NetworkToLocalManga,
    private val getChapterByUrlAndMangaId: GetChapterByUrlAndMangaId,
    private val updateMangaFromRemote: RemoteMangaUpdater,
) : ViewModel() {

    val state: StateFlow<DeepLinkViewModel.State>
        field = MutableStateFlow<DeepLinkViewModel.State>(State.Loading)

    @AssistedFactory
    @ManualViewModelAssistedFactoryKey
    @ContributesIntoMap(AppScope::class)
    interface Factory : ManualViewModelAssistedFactory {
        fun create(query: String): DeepLinkViewModel
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val source = sourceManager.getAll()
                .firstOrNull { it.resolver?.uriType(query).let { type -> type != null && type != UriType.Unknown } }
            val resolver = source?.resolver

            val manga = resolver?.manga(query)?.let {
                networkToLocalManga(it.toDomainManga(source.id))
            }

            val chapter = if (resolver?.uriType(query) == UriType.Chapter && manga != null) {
                resolver.chapter(query)?.let { getChapterFromSourceChapter(it, manga) }
            } else {
                null
            }

            state.update {
                if (manga == null) {
                    State.NoResults
                } else {
                    if (chapter == null) {
                        State.Result(manga)
                    } else {
                        State.Result(manga, chapter.id)
                    }
                }
            }
        }
    }

    private suspend fun getChapterFromSourceChapter(sourceChapter: SourceChapter, manga: Manga): Chapter? {
        val localChapter = getChapterByUrlAndMangaId.await(sourceChapter.url, manga.id)

        if (localChapter != null) return localChapter
        updateMangaFromRemote(manga, fetchChapters = true).getOrElse { return null }
        return getChapterByUrlAndMangaId.await(sourceChapter.url, manga.id)
    }

    sealed interface State {
        @Immutable
        data object Loading : State

        @Immutable
        data object NoResults : State

        @Immutable
        data class Result(val manga: Manga, val chapterId: Long? = null) : State
    }
}
