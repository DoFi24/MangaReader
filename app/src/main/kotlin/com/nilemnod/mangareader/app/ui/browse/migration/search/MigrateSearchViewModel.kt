package com.nilemnod.mangareader.app.ui.browse.migration.search

import androidx.lifecycle.viewModelScope
import com.nilemnod.mangareader.app.domain.source.service.SourcePreferences
import com.nilemnod.mangareader.app.extension.ExtensionManager
import com.nilemnod.mangareader.app.ui.browse.source.globalsearch.SearchItemResult
import com.nilemnod.mangareader.app.ui.browse.source.globalsearch.SearchViewModel
import com.nilemnod.mangareader.domain.manga.interactor.GetManga
import com.nilemnod.mangareader.domain.manga.interactor.NetworkToLocalManga
import com.nilemnod.mangareader.domain.source.api.ContentSource
import com.nilemnod.mangareader.domain.source.service.SourceManager
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import kotlinx.coroutines.launch

@AssistedInject
class MigrateSearchViewModel(
    @Assisted val mangaId: Long,
    sourcePreferences: SourcePreferences,
    extensionManager: ExtensionManager,
    networkToLocalManga: NetworkToLocalManga,
    getManga: GetManga,
    preferences: SourcePreferences,
    private val sourceManager: SourceManager,
) : SearchViewModel(
    sourcePreferences = sourcePreferences,
    sourceManager = sourceManager,
    extensionManager = extensionManager,
    networkToLocalManga = networkToLocalManga,
    getManga = getManga,
    preferences = preferences,
) {
    @AssistedFactory
    @ManualViewModelAssistedFactoryKey
    @ContributesIntoMap(AppScope::class)
    interface Factory : ManualViewModelAssistedFactory {
        fun create(mangaId: Long): MigrateSearchViewModel
    }

    private val migrationSources by lazy { sourcePreferences.migrationSources.get() }

    override val sortComparator = { map: Map<ContentSource, SearchItemResult> ->
        compareBy<ContentSource>(
            { (map[it] as? SearchItemResult.Success)?.isEmpty ?: true },
            { migrationSources.indexOf(it.id) },
        )
    }

    init {
        viewModelScope.launch {
            val manga = getManga.await(mangaId)!!
            updateState {
                it.copy(
                    from = manga,
                    searchQuery = manga.title,
                )
            }
            search()
        }
    }

    override suspend fun getEnabledSources(): List<ContentSource> {
        return migrationSources.mapNotNull { sourceManager.content(it) }
    }
}
