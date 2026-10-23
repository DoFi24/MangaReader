package com.nilemnod.mangareader.data.source

import androidx.paging.PagingState
import com.nilemnod.mangareader.domain.manga.interactor.NetworkToLocalManga
import com.nilemnod.mangareader.domain.manga.model.Manga
import com.nilemnod.mangareader.domain.manga.model.toDomainManga
import com.nilemnod.mangareader.domain.source.api.ContentSource
import com.nilemnod.mangareader.domain.source.api.FilterList
import com.nilemnod.mangareader.domain.source.api.MangaListing
import com.nilemnod.mangareader.domain.source.repository.SourcePagingSource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SourceSearchPagingSource(
    source: suspend () -> ContentSource,
    private val query: String,
    private val filters: FilterList,
    networkToLocalManga: NetworkToLocalManga,
) : BaseSourcePagingSource(source, networkToLocalManga) {
    override suspend fun requestNextPage(source: ContentSource, currentPage: Int): MangaListing {
        return source.search(currentPage, query, filters)
    }
}

class SourcePopularPagingSource(
    source: suspend () -> ContentSource,
    networkToLocalManga: NetworkToLocalManga,
) : BaseSourcePagingSource(source, networkToLocalManga) {
    override suspend fun requestNextPage(source: ContentSource, currentPage: Int): MangaListing {
        return source.popular(currentPage)
    }
}

class SourceLatestPagingSource(
    source: suspend () -> ContentSource,
    networkToLocalManga: NetworkToLocalManga,
) : BaseSourcePagingSource(source, networkToLocalManga) {
    override suspend fun requestNextPage(source: ContentSource, currentPage: Int): MangaListing {
        return source.latest(currentPage)
    }
}

abstract class BaseSourcePagingSource(
    private val source: suspend () -> ContentSource,
    private val networkToLocalManga: NetworkToLocalManga,
) : SourcePagingSource() {

    private val seenManga = hashSetOf<String>()

    abstract suspend fun requestNextPage(source: ContentSource, currentPage: Int): MangaListing

    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, Manga> {
        val page = params.key ?: 1

        return try {
            val source = source()
            val mangasPage = withContext(Dispatchers.IO) {
                requestNextPage(source, page.toInt())
                    .takeIf { it.mangas.isNotEmpty() }
                    ?: throw NoResultsException()
            }

            val manga = mangasPage.mangas
                .map { it.toDomainManga(source.id) }
                .filter { seenManga.add(it.url) }
                .let { networkToLocalManga(it) }

            LoadResult.Page(
                data = manga,
                prevKey = null,
                nextKey = if (mangasPage.hasNextPage) page + 1 else null,
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Long, Manga>): Long? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey ?: anchorPage?.nextKey
        }
    }
}

class NoResultsException : Exception()
