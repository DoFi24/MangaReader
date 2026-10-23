package com.nilemnod.mangareader.data.source

import com.nilemnod.mangareader.data.Database
import com.nilemnod.mangareader.data.subscribeToList
import com.nilemnod.mangareader.domain.manga.interactor.NetworkToLocalManga
import com.nilemnod.mangareader.domain.source.api.ContentSource
import com.nilemnod.mangareader.domain.source.api.FilterList
import com.nilemnod.mangareader.domain.source.model.SourceWithCount
import com.nilemnod.mangareader.domain.source.repository.SourcePagingSource
import com.nilemnod.mangareader.domain.source.repository.SourceRepository
import com.nilemnod.mangareader.domain.source.service.SourceManager
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import com.nilemnod.mangareader.domain.source.model.Source as DomainSource

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class SourceRepositoryImpl(
    private val sourceManager: SourceManager,
    private val database: Database,
    private val networkToLocalManga: NetworkToLocalManga,
) : SourceRepository {

    override fun getSources(): Flow<List<DomainSource>> {
        return sourceManager.contentSources.map { sources ->
            sources.map {
                mapSourceToDomainSource(it).copy(
                    supportsLatest = it.supportsLatest,
                )
            }
        }
    }

    override fun getOnlineSources(): Flow<List<DomainSource>> {
        return sourceManager.contentSources.map { sources ->
            sources
                .filter { it.web != null }
                .map(::mapSourceToDomainSource)
        }
    }

    override fun getSourcesWithFavoriteCount(): Flow<List<Pair<DomainSource, Long>>> {
        val sourceIdWithFavoriteCountFlow = database.mangaQueries
            .getSourceIdWithFavoriteCount()
            .subscribeToList()
        return combine(sourceIdWithFavoriteCountFlow, sourceManager.contentSources) { sourceIdWithFavoriteCount, _ ->
            sourceIdWithFavoriteCount
        }
            .map {
                it.map { (sourceId, count) ->
                    val source = sourceManager.contentOrStub(sourceId)
                    val domainSource = mapSourceToDomainSource(source).copy(
                        isStub = source.isStub,
                    )
                    domainSource to count
                }
            }
    }

    override fun getSourcesWithNonLibraryManga(): Flow<List<SourceWithCount>> {
        return database.mangaQueries
            .getSourceIdsWithNonLibraryManga()
            .subscribeToList()
            .map { sourceId ->
                sourceId.map { (sourceId, count) ->
                    val source = sourceManager.contentOrStub(sourceId)
                    val domainSource = mapSourceToDomainSource(source).copy(
                        isStub = source.isStub,
                    )
                    SourceWithCount(domainSource, count)
                }
            }
    }

    override fun search(
        sourceId: Long,
        query: String,
        filterList: FilterList,
    ): SourcePagingSource {
        return SourceSearchPagingSource(
            { sourceManager.contentOrStub(sourceId) },
            query,
            filterList,
            networkToLocalManga,
        )
    }

    override fun getPopular(sourceId: Long): SourcePagingSource {
        return SourcePopularPagingSource({ sourceManager.contentOrStub(sourceId) }, networkToLocalManga)
    }

    override fun getLatest(sourceId: Long): SourcePagingSource {
        return SourceLatestPagingSource({ sourceManager.contentOrStub(sourceId) }, networkToLocalManga)
    }

    private fun mapSourceToDomainSource(source: ContentSource): DomainSource = DomainSource(
        id = source.id,
        lang = source.lang,
        name = source.name,
        supportsLatest = false,
        isStub = false,
    )
}
