package com.nilemnod.mangareader.domain.source.service

import com.nilemnod.mangareader.domain.source.api.ContentSource
import kotlinx.coroutines.flow.Flow

interface SourceManager {

    val sources: Flow<List<ContentSource>>

    suspend fun get(sourceKey: Long): ContentSource?

    suspend fun getOrStub(sourceKey: Long): ContentSource

    suspend fun getAll(): List<ContentSource>

    suspend fun getOnlineSources(): List<ContentSource>

    suspend fun getStubSources(): List<ContentSource>
}
