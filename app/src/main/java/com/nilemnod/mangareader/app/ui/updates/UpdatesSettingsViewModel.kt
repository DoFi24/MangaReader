package com.nilemnod.mangareader.app.ui.updates

import androidx.lifecycle.ViewModel
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import com.nilemnod.mangareader.core.common.preference.Preference
import com.nilemnod.mangareader.core.common.preference.TriState
import com.nilemnod.mangareader.core.common.preference.getAndSet
import com.nilemnod.mangareader.domain.category.interactor.GetCategories
import com.nilemnod.mangareader.domain.category.model.Category
import com.nilemnod.mangareader.domain.updates.service.UpdatesPreferences

@Inject
@ViewModelKey
@ContributesIntoMap(AppScope::class)
class UpdatesSettingsViewModel(
    val updatesPreferences: UpdatesPreferences,
    val getCategories: GetCategories,
) : ViewModel() {

    val includedCategories = updatesPreferences.filterIncludedCategories
    val excludedCategories = updatesPreferences.filterExcludedCategories

    fun cycleCategory(category: Category) {
        when (category.id) {
            in includedCategories.get() -> {
                includedCategories.getAndSet { it - category.id }
                excludedCategories.getAndSet { it + category.id }
            }

            in excludedCategories.get() -> excludedCategories.getAndSet { it - category.id }
            else -> includedCategories.getAndSet { it + category.id }
        }
    }

    fun toggleFilter(preference: (UpdatesPreferences) -> Preference<TriState>) {
        preference(updatesPreferences).getAndSet {
            it.next()
        }
    }
}
