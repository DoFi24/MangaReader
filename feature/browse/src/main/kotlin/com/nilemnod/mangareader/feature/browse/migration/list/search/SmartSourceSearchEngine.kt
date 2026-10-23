package com.nilemnod.mangareader.feature.browse.migration.list.search

import com.nilemnod.mangareader.domain.manga.model.Manga
import com.nilemnod.mangareader.domain.manga.model.toDomainManga
import com.nilemnod.mangareader.domain.source.api.ContentSource
import com.nilemnod.mangareader.domain.source.api.SourceManga
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SmartSourceSearchEngine(extraSearchParams: String?) : BaseSmartSearchEngine<SourceManga>(extraSearchParams) {

    override fun getTitle(result: SourceManga) = result.title

    suspend fun regularSearch(source: ContentSource, title: String): Manga? {
        return regularSearch(makeSearchAction(source), title).let {
            it?.toDomainManga(source.id)
        }
    }

    suspend fun deepSearch(source: ContentSource, title: String): Manga? {
        return deepSearch(makeSearchAction(source), title).let {
            it?.toDomainManga(source.id)
        }
    }

    // Sources aren't main-safe: their default implementations block on the network
    private fun makeSearchAction(source: ContentSource): SearchAction<SourceManga> = { query ->
        withContext(Dispatchers.IO) { source.search(1, query, source.filters()).mangas }
    }
}
