package com.nilemnod.mangareader.domain.source.service

import com.nilemnod.mangareader.domain.source.api.ContentSource
import com.nilemnod.mangareader.domain.source.model.StubSource
import eu.kanade.tachiyomi.source.Source
import eu.kanade.tachiyomi.source.online.HttpSource
import kotlinx.coroutines.flow.Flow

interface SourceManager {

    val sources: Flow<List<Source>>

    suspend fun get(sourceKey: Long): Source?

    suspend fun getOrStub(sourceKey: Long): Source

    suspend fun getAll(): List<Source>

    suspend fun getOnlineSources(): List<HttpSource>

    suspend fun getStubSources(): List<StubSource>

    val contentSources: Flow<List<ContentSource>>

    suspend fun content(sourceKey: Long): ContentSource?

    suspend fun contentOrStub(sourceKey: Long): ContentSource

    suspend fun allContent(): List<ContentSource>
}
