package com.nilemnod.mangareader.domain.extension.repository

import com.nilemnod.mangareader.app.extension.model.Extension
import kotlinx.coroutines.flow.Flow
import com.nilemnod.mangareader.domain.extension.model.ExtensionStore

interface ExtensionStoreRepository {
    suspend fun insert(indexUrl: String): Result<Unit>

    suspend fun insertFromPreference(indexUrl: String, name: String)

    suspend fun upsert(store: ExtensionStore)

    suspend fun refreshAll()

    suspend fun fetchExtensions(): List<Extension.Available>

    suspend fun getAll(): List<ExtensionStore>

    fun getAllAsFlow(): Flow<List<ExtensionStore>>

    fun getCountAsFlow(): Flow<Long>

    suspend fun remove(indexUrl: String)
}
