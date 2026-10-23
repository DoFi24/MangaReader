package com.nilemnod.mangareader.domain.history.interactor

import com.nilemnod.mangareader.domain.history.repository.HistoryRepository
import dev.zacsweers.metro.Inject

@Inject
class GetTotalReadDuration(
    private val repository: HistoryRepository,
) {

    suspend fun await(): Long {
        return repository.getTotalReadDuration()
    }
}
