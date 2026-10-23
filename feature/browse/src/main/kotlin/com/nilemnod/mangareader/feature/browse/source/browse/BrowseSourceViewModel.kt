package com.nilemnod.mangareader.feature.browse.source.browse

import android.content.res.Configuration
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import androidx.paging.filter
import androidx.paging.map
import com.nilemnod.mangareader.core.common.preference.CheckboxState
import com.nilemnod.mangareader.core.common.preference.mapAsCheckboxState
import com.nilemnod.mangareader.core.metro.AppCoroutineScope
import com.nilemnod.mangareader.core.storage.CoverCache
import com.nilemnod.mangareader.core.ui.preference.asState
import com.nilemnod.mangareader.domain.category.interactor.GetCategories
import com.nilemnod.mangareader.domain.category.interactor.SetMangaCategories
import com.nilemnod.mangareader.domain.category.model.Category
import com.nilemnod.mangareader.domain.chapter.interactor.SetMangaDefaultChapterFlags
import com.nilemnod.mangareader.domain.library.service.LibraryPreferences
import com.nilemnod.mangareader.domain.manga.interactor.GetDuplicateLibraryManga
import com.nilemnod.mangareader.domain.manga.interactor.GetManga
import com.nilemnod.mangareader.domain.manga.interactor.UpdateManga
import com.nilemnod.mangareader.domain.manga.model.Manga
import com.nilemnod.mangareader.domain.manga.model.MangaUpdate
import com.nilemnod.mangareader.domain.manga.model.MangaWithChapterCount
import com.nilemnod.mangareader.domain.source.api.ContentSource
import com.nilemnod.mangareader.domain.source.api.FilterList
import com.nilemnod.mangareader.domain.source.api.SourceFilter
import com.nilemnod.mangareader.domain.source.interactor.GetIncognitoState
import com.nilemnod.mangareader.domain.source.interactor.GetRemoteManga
import com.nilemnod.mangareader.domain.source.service.SourceManager
import com.nilemnod.mangareader.domain.source.service.SourcePreferences
import com.nilemnod.mangareader.source.local.isLocal
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds

@AssistedInject
class BrowseSourceViewModel(
    @Assisted private val sourceId: Long,
    @Assisted listingQuery: String?,
    @AppCoroutineScope private val appScope: CoroutineScope,
    val sourceManager: SourceManager,
    sourcePreferences: SourcePreferences,
    private val libraryPreferences: LibraryPreferences,
    private val coverCache: CoverCache,
    private val getRemoteManga: GetRemoteManga,
    private val getDuplicateLibraryManga: GetDuplicateLibraryManga,
    private val getCategories: GetCategories,
    private val setMangaCategories: SetMangaCategories,
    private val setMangaDefaultChapterFlags: SetMangaDefaultChapterFlags,
    private val getManga: GetManga,
    private val updateManga: UpdateManga,
    getIncognitoState: GetIncognitoState,
) : ViewModel() {

    val state: StateFlow<BrowseSourceViewModel.State>
        field = MutableStateFlow<BrowseSourceViewModel.State>(State(Listing.valueOf(listingQuery)))

    @AssistedFactory
    @ManualViewModelAssistedFactoryKey
    @ContributesIntoMap(AppScope::class)
    interface Factory : ManualViewModelAssistedFactory {
        fun create(sourceId: Long, listingQuery: String?): BrowseSourceViewModel
    }

    var displayMode by sourcePreferences.sourceDisplayMode.asState(viewModelScope)

    private val source: ContentSource? get() = state.value.source

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val source = sourceManager.getOrStub(sourceId)

            state.update {
                var query: String? = null
                var listing = it.listing

                if (listing is Listing.Search) {
                    query = listing.query
                    listing = Listing.Search(query, source.filters())
                }

                it.copy(
                    source = source,
                    listing = listing,
                    filters = source.filters(),
                    toolbarQuery = query,
                )
            }

            if (!getIncognitoState.await(source.id)) {
                sourcePreferences.lastUsedSource.set(source.id)
            }
        }
    }

    /**
     * Flow of Pager flow tied to [State.listing]
     */
    private val hideInLibraryItems = sourcePreferences.hideInLibraryItems.get()
    val mangaPagerFlowFlow = state.map { it.source to it.listing }
        .filter { (source, _) -> source != null }
        .map { (_, listing) -> listing }
        .distinctUntilChanged()
        .map { listing ->
            Pager(PagingConfig(pageSize = 25)) {
                getRemoteManga(sourceId, listing.query ?: "", listing.filters)
            }.flow.map { pagingData ->
                pagingData.map { manga ->
                    getManga.subscribe(manga.url, manga.source)
                        .map { it ?: manga }
                        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5.seconds), manga)
                }
                    .filter { !hideInLibraryItems || !it.value.favorite }
            }
                .cachedIn(viewModelScope)
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyFlow())

    fun getColumnsPreference(orientation: Int): GridCells {
        val isLandscape = orientation == Configuration.ORIENTATION_LANDSCAPE
        val columns = if (isLandscape) {
            libraryPreferences.landscapeColumns
        } else {
            libraryPreferences.portraitColumns
        }.get()
        return if (columns == 0) GridCells.Adaptive(128.dp) else GridCells.Fixed(columns)
    }

    fun resetFilters() {
        val source = source ?: return
        state.update { it.copy(filters = source.filters()) }
    }

    fun setListing(listing: Listing) {
        state.update { it.copy(listing = listing, toolbarQuery = null) }
    }

    fun setFilters(filters: FilterList) {
        state.update {
            it.copy(
                filters = filters,
            )
        }
    }

    fun search(query: String? = null, filters: FilterList? = null) {
        val input = state.value.listing as? Listing.Search
            ?: Listing.Search(query = null, filters = source?.filters() ?: FilterList())

        state.update {
            it.copy(
                listing = input.copy(
                    query = query ?: input.query,
                    filters = filters ?: input.filters,
                ),
                toolbarQuery = query ?: input.query,
            )
        }
    }

    fun searchGenre(genreName: String) {
        val defaultFilters = source?.filters() ?: return
        var genreExists = false

        val updated = defaultFilters.filters.map { sourceFilter ->
            if (genreExists) return@map sourceFilter
            when (sourceFilter) {
                is SourceFilter.Group -> {
                    val index = sourceFilter.filters.indexOfFirst {
                        it.name.equals(genreName, true) && (it is SourceFilter.TriState || it is SourceFilter.CheckBox)
                    }
                    if (index == -1) {
                        sourceFilter
                    } else {
                        genreExists = true
                        sourceFilter.copy(
                            filters = sourceFilter.filters.toMutableList().also {
                                it[index] = when (val child = it[index]) {
                                    is SourceFilter.TriState -> child.copy(state = SourceFilter.TriState.State.INCLUDE)
                                    is SourceFilter.CheckBox -> child.copy(checked = true)
                                    else -> child
                                }
                            },
                        )
                    }
                }
                is SourceFilter.Select -> {
                    val index = sourceFilter.values.indexOfFirst { it.equals(genreName, true) }
                    if (index == -1) {
                        sourceFilter
                    } else {
                        genreExists = true
                        sourceFilter.copy(selected = index)
                    }
                }
                else -> sourceFilter
            }
        }
        val filters = FilterList(updated)

        state.update {
            val listing = if (genreExists) {
                Listing.Search(query = null, filters = filters)
            } else {
                Listing.Search(query = genreName, filters = filters)
            }
            it.copy(
                filters = filters,
                listing = listing,
                toolbarQuery = listing.query,
            )
        }
    }

    /**
     * Adds or removes a manga from the library.
     *
     * @param manga the manga to update.
     */
    fun changeMangaFavorite(manga: Manga) {
        appScope.launch {
            val update = if (manga.favorite) {
                if (!manga.isLocal()) coverCache.deleteFromCache(manga, true)
                MangaUpdate(manga.id) { favoriteAt = null }
            } else {
                setMangaDefaultChapterFlags.await(manga)
                MangaUpdate(manga.id) { favoriteAt = Clock.System.now() }
            }

            updateManga.await(update)
        }
    }

    fun addFavorite(manga: Manga) {
        viewModelScope.launch {
            val categories = getCategories()
            val defaultCategoryId = libraryPreferences.defaultCategory.get()
            val defaultCategory = categories.find { it.id == defaultCategoryId.toLong() }

            when {
                // Default category set
                defaultCategory != null -> {
                    moveMangaToCategories(manga, defaultCategory)

                    changeMangaFavorite(manga)
                }

                // Automatic 'Default' or no categories
                defaultCategoryId == 0 || categories.isEmpty() -> {
                    moveMangaToCategories(manga)

                    changeMangaFavorite(manga)
                }

                // Choose a category
                else -> {
                    val preselectedIds = getCategories.await(manga.id).map { it.id }
                    setDialog(
                        Dialog.ChangeMangaCategory(
                            manga,
                            categories.mapAsCheckboxState { it.id in preselectedIds },
                        ),
                    )
                }
            }
        }
    }

    /**
     * Get user categories.
     *
     * @return List of categories, not including the default category
     */
    suspend fun getCategories(): List<Category> {
        return getCategories.subscribe()
            .firstOrNull()
            ?.filterNot { it.isSystemCategory }
            .orEmpty()
    }

    suspend fun getDuplicateLibraryManga(manga: Manga): List<MangaWithChapterCount> {
        return getDuplicateLibraryManga.invoke(manga)
    }

    private fun moveMangaToCategories(manga: Manga, vararg categories: Category) {
        moveMangaToCategories(manga, categories.filter { it.id != 0L }.map { it.id })
    }

    fun moveMangaToCategories(manga: Manga, categoryIds: List<Long>) {
        viewModelScope.launch(Dispatchers.IO) {
            setMangaCategories.await(
                mangaId = manga.id,
                categoryIds = categoryIds.toList(),
            )
        }
    }

    fun openFilterSheet() {
        setDialog(Dialog.Filter)
    }

    fun setDialog(dialog: Dialog?) {
        state.update { it.copy(dialog = dialog) }
    }

    fun setToolbarQuery(query: String?) {
        state.update { it.copy(toolbarQuery = query) }
    }

    sealed class Listing(open val query: String?, open val filters: FilterList) {
        data object Popular : Listing(query = GetRemoteManga.QUERY_POPULAR, filters = FilterList())
        data object Latest : Listing(query = GetRemoteManga.QUERY_LATEST, filters = FilterList())
        data class Search(
            override val query: String?,
            override val filters: FilterList,
        ) : Listing(query = query, filters = filters)

        companion object {
            fun valueOf(query: String?): Listing {
                return when (query) {
                    GetRemoteManga.QUERY_POPULAR -> Popular
                    GetRemoteManga.QUERY_LATEST -> Latest
                    else -> Search(query = query, filters = FilterList()) // filters are filled in later
                }
            }
        }
    }

    sealed interface Dialog {
        data object Filter : Dialog
        data class RemoveManga(val manga: Manga) : Dialog
        data class AddDuplicateManga(val manga: Manga, val duplicates: List<MangaWithChapterCount>) : Dialog
        data class ChangeMangaCategory(
            val manga: Manga,
            val initialSelection: List<CheckboxState.State<Category>>,
        ) : Dialog
        data class Migrate(val target: Manga, val current: Manga) : Dialog
    }

    @Immutable
    data class State(
        val listing: Listing,
        val source: ContentSource? = null,
        val filters: FilterList = FilterList(),
        val toolbarQuery: String? = null,
        val dialog: Dialog? = null,
    ) {
        val isUserQuery get() = listing is Listing.Search && !listing.query.isNullOrEmpty()
    }
}
