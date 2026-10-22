package com.nilemnod.mangareader.app.domain.extension.interactor

import com.nilemnod.mangareader.app.domain.source.service.SourcePreferences
import com.nilemnod.mangareader.domain.extension.model.Extension
import dev.zacsweers.metro.Inject
import eu.kanade.tachiyomi.source.Source
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Inject
class GetExtensionSources(
    private val preferences: SourcePreferences,
) {

    fun subscribe(extension: Extension.Loaded): Flow<List<ExtensionSourceItem>> {
        val isMultiSource = extension.sources.size > 1
        val isMultiLangSingleSource =
            isMultiSource && extension.sources.map { it.name }.distinct().size == 1

        return preferences.disabledSources.changes().map { disabledSources ->
            fun Source.isEnabled() = id.toString() !in disabledSources

            extension.sources
                .map { source ->
                    ExtensionSourceItem(
                        source = source,
                        enabled = source.isEnabled(),
                        labelAsName = isMultiSource && !isMultiLangSingleSource,
                    )
                }
        }
    }
}

data class ExtensionSourceItem(
    val source: Source,
    val enabled: Boolean,
    val labelAsName: Boolean,
)
