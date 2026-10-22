package com.nilemnod.mangareader.domain.source.interactor

import com.nilemnod.mangareader.domain.source.model.SourceWithCount
import com.nilemnod.mangareader.domain.source.repository.SourceRepository
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow

@Inject
class GetSourcesWithNonLibraryManga(
    private val repository: SourceRepository,
) {

    fun subscribe(): Flow<List<SourceWithCount>> {
        return repository.getSourcesWithNonLibraryManga()
    }
}
