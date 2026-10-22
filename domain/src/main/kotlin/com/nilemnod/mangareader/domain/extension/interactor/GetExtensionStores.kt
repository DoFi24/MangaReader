package com.nilemnod.mangareader.domain.extension.interactor

import com.nilemnod.mangareader.domain.extension.model.ExtensionStore
import com.nilemnod.mangareader.domain.extension.repository.ExtensionStoreRepository
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow

@Inject
class GetExtensionStores(
    private val repository: ExtensionStoreRepository,
) {
    suspend fun get(): List<ExtensionStore> = repository.getAll()

    fun subscribe(): Flow<List<ExtensionStore>> = repository.getAllAsFlow()
}
