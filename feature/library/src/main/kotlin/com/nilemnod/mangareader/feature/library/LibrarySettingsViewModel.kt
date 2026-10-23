package com.nilemnod.mangareader.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nilemnod.mangareader.core.common.preference.Preference
import com.nilemnod.mangareader.core.common.preference.TriState
import com.nilemnod.mangareader.core.common.preference.getAndSet
import com.nilemnod.mangareader.domain.base.service.ModePreferences
import com.nilemnod.mangareader.domain.category.interactor.SetDisplayMode
import com.nilemnod.mangareader.domain.category.interactor.SetSortModeForCategory
import com.nilemnod.mangareader.domain.category.model.Category
import com.nilemnod.mangareader.domain.library.model.LibraryDisplayMode
import com.nilemnod.mangareader.domain.library.model.LibrarySort
import com.nilemnod.mangareader.domain.library.service.LibraryPreferences
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.launch

@Inject
@ViewModelKey
@ContributesIntoMap(AppScope::class)
class LibrarySettingsViewModel(
    val preferences: ModePreferences,
    val libraryPreferences: LibraryPreferences,
    private val setDisplayMode: SetDisplayMode,
    private val setSortModeForCategory: SetSortModeForCategory,
) : ViewModel() {

    fun toggleFilter(preference: (LibraryPreferences) -> Preference<TriState>) {
        preference(libraryPreferences).getAndSet {
            it.next()
        }
    }

    fun setDisplayMode(mode: LibraryDisplayMode) {
        setDisplayMode.await(mode)
    }

    fun setSort(category: Category?, mode: LibrarySort.Type, direction: LibrarySort.Direction) {
        viewModelScope.launch {
            setSortModeForCategory.await(category, mode, direction)
        }
    }
}
