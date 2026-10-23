package com.nilemnod.mangareader.domain.history.interactor

import com.nilemnod.mangareader.domain.history.model.HistoryUpdate
import com.nilemnod.mangareader.domain.history.repository.HistoryRepository
import dev.zacsweers.metro.Inject

@Inject
class UpsertHistory(
    private val historyRepository: HistoryRepository,
) {

    suspend fun await(historyUpdate: HistoryUpdate) {
        historyRepository.upsertHistory(historyUpdate)
    }
}
