package com.nilemnod.mangareader.domain.history.interactor

import com.nilemnod.mangareader.domain.history.model.History
import com.nilemnod.mangareader.domain.history.model.HistoryWithRelations
import com.nilemnod.mangareader.domain.history.repository.HistoryRepository
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow

@Inject
class GetHistory(
    private val repository: HistoryRepository,
) {

    suspend fun await(mangaId: Long): List<History> {
        return repository.getHistoryByMangaId(mangaId)
    }

    fun subscribe(query: String): Flow<List<HistoryWithRelations>> {
        return repository.getHistory(query)
    }
}
