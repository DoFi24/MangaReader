package com.nilemnod.mangareader.domain.history.interactor

import dev.zacsweers.metro.Inject
import com.nilemnod.mangareader.domain.history.model.HistoryWithRelations
import com.nilemnod.mangareader.domain.history.repository.HistoryRepository

@Inject
class RemoveHistory(
    private val repository: HistoryRepository,
) {

    suspend fun awaitAll(): Boolean {
        return repository.deleteAllHistory()
    }

    suspend fun await(history: HistoryWithRelations) {
        repository.resetHistory(history.id)
    }

    suspend fun await(mangaId: Long) {
        repository.resetHistoryByMangaId(mangaId)
    }
}
