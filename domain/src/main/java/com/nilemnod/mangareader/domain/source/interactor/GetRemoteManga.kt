package com.nilemnod.mangareader.domain.source.interactor

import com.nilemnod.mangareader.domain.source.repository.SourcePagingSource
import com.nilemnod.mangareader.domain.source.repository.SourceRepository
import dev.zacsweers.metro.Inject
import eu.kanade.tachiyomi.source.model.FilterList

@Inject
class GetRemoteManga(
    private val repository: SourceRepository,
) {

    operator fun invoke(sourceId: Long, query: String, filterList: FilterList): SourcePagingSource {
        return when (query) {
            QUERY_POPULAR -> repository.getPopular(sourceId)
            QUERY_LATEST -> repository.getLatest(sourceId)
            else -> repository.search(sourceId, query, filterList)
        }
    }

    companion object {
        const val QUERY_POPULAR = "com.nilemnod.mangareader.app.domain.source.interactor.POPULAR"
        const val QUERY_LATEST = "com.nilemnod.mangareader.app.domain.source.interactor.LATEST"
    }
}
