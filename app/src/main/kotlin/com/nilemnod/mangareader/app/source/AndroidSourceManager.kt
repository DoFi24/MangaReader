package com.nilemnod.mangareader.app.source

import com.nilemnod.mangareader.app.extension.ExtensionManager
import com.nilemnod.mangareader.app.source.compat.ExtensionContentSource
import com.nilemnod.mangareader.core.download.DownloadManager
import com.nilemnod.mangareader.core.metro.AppCoroutineScope
import com.nilemnod.mangareader.domain.source.api.ContentSource
import com.nilemnod.mangareader.domain.source.model.StubSource
import com.nilemnod.mangareader.domain.source.repository.StubSourceRepository
import com.nilemnod.mangareader.domain.source.service.SourceManager
import com.nilemnod.mangareader.source.local.LocalSource
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import eu.kanade.tachiyomi.source.Source
import eu.kanade.tachiyomi.source.online.HttpSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class AndroidSourceManager(
    @AppCoroutineScope private val scope: CoroutineScope,
    private val extensionManager: ExtensionManager,
    private val sourceRepository: StubSourceRepository,
    private val localSource: LocalSource,
    private val downloadManager: Lazy<DownloadManager>,
) : SourceManager {

    /**
     * Null until the extensions have loaded, so that nothing observes the empty seed value.
     */
    private val sourcesMapFlow = MutableStateFlow<Map<Long, Source>?>(null)

    private val stubSourcesMap = ConcurrentHashMap<Long, StubSource>()

    private val contentCache = ConcurrentHashMap<Long, ExtensionContentSource>()

    override val sources: Flow<List<Source>> = sourcesMapFlow
        .filterNotNull()
        .map { it.values.toList() }

    init {
        scope.launch {
            extensionManager.loadedExtensionsFlow
                .collectLatest { extensions ->
                    val mutableMap = ConcurrentHashMap<Long, Source>(
                        mapOf(LocalSource.ID to localSource),
                    )
                    extensions.forEach { extension ->
                        extension.sources.forEach {
                            mutableMap[it.id] = it
                            registerStubSource(StubSource.from(it))
                        }
                    }
                    sourcesMapFlow.value = mutableMap
                }
        }

        scope.launch {
            sourceRepository.subscribeAll()
                .collectLatest { sources ->
                    sources.forEach {
                        stubSourcesMap[it.id] = it
                    }
                }
        }
    }

    /**
     * Awaits the extensions to have loaded before returning the sources.
     */
    private suspend fun sourcesMap(): Map<Long, Source> = sourcesMapFlow.filterNotNull().first()

    override suspend fun get(sourceKey: Long): Source? {
        return sourcesMap()[sourceKey]
    }

    override suspend fun getOrStub(sourceKey: Long): Source {
        return sourcesMap()[sourceKey] ?: stubSourcesMap.getOrPut(sourceKey) {
            createStubSource(sourceKey)
        }
    }

    override suspend fun getAll(): List<Source> {
        return sourcesMap().values.toList()
    }

    override suspend fun getOnlineSources(): List<HttpSource> {
        return sourcesMap().values.filterIsInstance<HttpSource>()
    }

    override val contentSources: Flow<List<ContentSource>> = sources.map { list -> list.map(::contentFor) }

    override suspend fun content(sourceKey: Long): ContentSource? = get(sourceKey)?.let(::contentFor)

    override suspend fun contentOrStub(sourceKey: Long): ContentSource = contentFor(getOrStub(sourceKey))

    override suspend fun allContent(): List<ContentSource> = getAll().map(::contentFor)

    private fun contentFor(source: Source): ContentSource {
        val cached = contentCache[source.id]
        if (cached != null && cached.native === source) return cached
        return ExtensionContentSource(source).also { contentCache[source.id] = it }
    }

    override suspend fun getOnlineContent(): List<ContentSource> =
        getOnlineSources().map(::contentFor)

    override suspend fun getStubContent(): List<ContentSource> =
        getStubSources().map(::contentFor)

    override suspend fun getStubSources(): List<StubSource> {
        val onlineSourceIds = getOnlineSources().map { it.id }
        return stubSourcesMap.values.filterNot { it.id in onlineSourceIds }
    }

    private fun registerStubSource(source: StubSource) {
        scope.launch {
            val dbSource = sourceRepository.getStubSource(source.id)
            if (dbSource == source) return@launch
            sourceRepository.upsertStubSource(source.id, source.lang, source.name)
            if (dbSource != null) {
                downloadManager.value.renameSource(contentFor(dbSource), contentFor(source))
            }
        }
    }

    private suspend fun createStubSource(id: Long): StubSource {
        sourceRepository.getStubSource(id)?.let {
            return it
        }
        extensionManager.getSourceData(id)?.let {
            registerStubSource(it)
            return it
        }
        return StubSource(id = id, lang = "", name = "")
    }
}
