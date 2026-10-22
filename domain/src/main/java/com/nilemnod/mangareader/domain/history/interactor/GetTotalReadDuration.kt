package com.nilemnod.mangareader.domain.history.interactor

import dev.zacsweers.metro.Inject
import com.nilemnod.mangareader.domain.history.repository.HistoryRepository

@Inject
class GetTotalReadDuration(
    private val repository: HistoryRepository,
) {

    suspend fun await(): Long {
        return repository.getTotalReadDuration()
    }
}
