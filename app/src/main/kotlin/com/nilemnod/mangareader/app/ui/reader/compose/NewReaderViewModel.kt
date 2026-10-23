package com.nilemnod.mangareader.app.ui.reader.compose

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.nilemnod.mangareader.app.domain.manga.interactor.SetMangaViewerFlags
import com.nilemnod.mangareader.app.domain.manga.model.readingMode
import com.nilemnod.mangareader.app.ui.reader.loader.ChapterLoader
import com.nilemnod.mangareader.app.ui.reader.loader.DownloadPageLoader
import com.nilemnod.mangareader.app.ui.reader.model.ReaderChapter
import com.nilemnod.mangareader.app.util.chapter.filterDownloaded
import com.nilemnod.mangareader.app.util.chapter.removeDuplicates
import com.nilemnod.mangareader.core.android.storage.cacheImageDir
import com.nilemnod.mangareader.core.android.system.activeNetworkState
import com.nilemnod.mangareader.core.common.i18n.stringResource
import com.nilemnod.mangareader.core.common.storage.DiskUtil
import com.nilemnod.mangareader.core.common.util.lang.byteSize
import com.nilemnod.mangareader.core.common.util.system.logcat
import com.nilemnod.mangareader.core.download.ChapterCache
import com.nilemnod.mangareader.core.download.DownloadCache
import com.nilemnod.mangareader.core.download.DownloadManager
import com.nilemnod.mangareader.core.download.DownloadProvider
import com.nilemnod.mangareader.core.metro.AppCoroutineScope
import com.nilemnod.mangareader.core.reader.ReaderPreferences
import com.nilemnod.mangareader.core.storage.saver.Image
import com.nilemnod.mangareader.core.storage.saver.ImageSaver
import com.nilemnod.mangareader.core.storage.saver.Location
import com.nilemnod.mangareader.domain.base.service.ModePreferences
import com.nilemnod.mangareader.domain.chapter.interactor.GetChaptersByMangaId
import com.nilemnod.mangareader.domain.chapter.interactor.UpdateChapter
import com.nilemnod.mangareader.domain.chapter.model.Chapter
import com.nilemnod.mangareader.domain.chapter.model.ChapterUpdate
import com.nilemnod.mangareader.domain.chapter.service.getChapterSort
import com.nilemnod.mangareader.domain.download.model.Download
import com.nilemnod.mangareader.domain.download.service.DownloadPreferences
import com.nilemnod.mangareader.domain.history.interactor.GetNextChapters
import com.nilemnod.mangareader.domain.history.interactor.UpsertHistory
import com.nilemnod.mangareader.domain.history.model.HistoryUpdate
import com.nilemnod.mangareader.domain.library.service.LibraryPreferences
import com.nilemnod.mangareader.domain.manga.interactor.GetManga
import com.nilemnod.mangareader.domain.manga.model.Manga
import com.nilemnod.mangareader.domain.source.api.Page
import com.nilemnod.mangareader.domain.source.interactor.GetIncognitoState
import com.nilemnod.mangareader.domain.source.service.SourceManager
import com.nilemnod.mangareader.feature.reader.SaveImageNotifier
import com.nilemnod.mangareader.feature.reader.model.PageSource
import com.nilemnod.mangareader.feature.reader.model.ReaderChapterContent
import com.nilemnod.mangareader.i18n.MR
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ViewModelAssistedFactoryKey
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import logcat.LogPriority
import okio.BufferedSource
import okio.buffer
import okio.source
import kotlin.time.Clock
import kotlin.time.Instant
import com.nilemnod.mangareader.core.reader.ReadingMode as AppReadingMode
import com.nilemnod.mangareader.feature.reader.model.ReaderPage as ComposeReaderPage
import com.nilemnod.mangareader.feature.reader.settings.ReadingMode as ReaderMode

@AssistedInject
class NewReaderViewModel(
    @Assisted private val savedState: SavedStateHandle,
    @AppCoroutineScope private val appScope: CoroutineScope,
    private val context: Context,
    private val sourceManager: SourceManager,
    private val downloadManager: DownloadManager,
    private val downloadProvider: DownloadProvider,
    private val chapterCache: ChapterCache,
    private val downloadCache: DownloadCache,
    private val imageSaver: ImageSaver,
    private val modePreferences: ModePreferences,
    private val saveImageNotifier: SaveImageNotifier,
    private val downloadPreferences: DownloadPreferences,
    private val libraryPreferences: LibraryPreferences,
    private val getManga: GetManga,
    private val getChaptersByMangaId: GetChaptersByMangaId,
    private val getNextChapters: GetNextChapters,
    private val upsertHistory: UpsertHistory,
    private val updateChapter: UpdateChapter,
    private val setMangaViewerFlags: SetMangaViewerFlags,
    private val getIncognitoState: GetIncognitoState,
    private val readerPreferences: ReaderPreferences,
) : ViewModel() {

    @AssistedFactory
    @ViewModelAssistedFactoryKey(NewReaderViewModel::class)
    @ContributesIntoMap(AppScope::class)
    fun interface Factory : ViewModelAssistedFactory {
        override fun create(extras: CreationExtras): NewReaderViewModel {
            return create(extras.createSavedStateHandle())
        }

        fun create(@Assisted savedState: SavedStateHandle): NewReaderViewModel
    }

    sealed interface UiState {
        data object Loading : UiState
        data class Error(val error: Throwable) : UiState
        data class Content(
            val chapter: ReaderChapterContent,
            val chapters: List<ReaderChapterContent> = listOf(chapter),
        ) : UiState
    }

    sealed interface Event {
        data object PictureSaved : Event
        data class SharePage(val uri: Uri, val message: String) : Event
    }

    private val eventChannel = Channel<Event>(Channel.BUFFERED)
    val events: Flow<Event> = eventChannel.receiveAsFlow()

    private val mutableState = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = mutableState.asStateFlow()

    private val mangaId = savedState.get<Long>("manga") ?: -1L
    private val initialChapterId = savedState.get<Long>("chapter") ?: -1L

    private var manga: Manga? = null
    private var modeOverride: ReaderMode? = null
    private var loader: ChapterLoader? = null
    private var chapterList: List<ReaderChapter> = emptyList()
    private var current: ReaderChapter? = null
    private val window = mutableListOf<ReaderChapter>()
    private var rootContent: ReaderChapterContent? = null
    private val contents = mutableListOf<ReaderChapterContent>()
    private var incognito = false
    private var readStart: Instant? = null
    private var pendingSave: Job? = null
    private var unfilteredChapterList: List<Chapter>? = null
    private var chapterToDownload: Download? = null
    private val downloadAheadAmount = downloadPreferences.autoDownloadWhileReading.get()

    private val openMutex = Mutex()
    private val progressWrites = Mutex()

    init {
        if (mangaId == -1L || initialChapterId == -1L) {
            mutableState.value = UiState.Error(IllegalArgumentException("Invalid reader arguments"))
        } else {
            viewModelScope.launch(Dispatchers.IO) { initialize() }
        }
    }

    private suspend fun initialize() {
        try {
            val manga = getManga.await(mangaId) ?: error("Requested manga of id $mangaId not found")
            val source = sourceManager.getOrStub(manga.source)
            incognito = getIncognitoState.await(manga.source)
            this.manga = manga
            loader = ChapterLoader(
                context,
                downloadManager,
                downloadProvider,
                chapterCache,
                appScope,
                ::canLoadAhead,
                manga,
                source,
            )

            val startId = savedState.get<Long>(KEY_CHAPTER_ID)?.takeIf { it != -1L } ?: initialChapterId
            chapterList = loadChapterList(manga, startId)

            val target = chapterList.firstOrNull { it.chapter.id == startId }
                ?: error("Requested chapter of id $startId not found in chapter list")
            open(target, savedState.get<Int>(KEY_PAGE_INDEX))
        } catch (e: Throwable) {
            if (e is CancellationException) throw e
            logcat(LogPriority.ERROR, e)
            mutableState.value = UiState.Error(e)
        }
    }

    private suspend fun getUnfilteredChapterList(manga: Manga): List<Chapter> {
        return unfilteredChapterList
            ?: getChaptersByMangaId.await(manga.id, applyScanlatorFilter = false)
                .also { unfilteredChapterList = it }
    }

    private suspend fun loadChapterList(manga: Manga, selectedId: Long): List<ReaderChapter> {
        val chapters = getChaptersByMangaId.await(manga.id, applyScanlatorFilter = true)
            .let { filtered ->
                if (filtered.any { it.id == selectedId }) {
                    filtered
                } else {
                    filtered + getUnfilteredChapterList(manga).filter { it.id == selectedId }
                }
            }

        val selectedChapter = chapters.find { it.id == selectedId }
            ?: error("Requested chapter of id $selectedId not found in chapter list")

        val skipRead = readerPreferences.skipRead.get()
        val skipFiltered = readerPreferences.skipFiltered.get()
        val chaptersForReader = if (skipRead || skipFiltered) {
            val filteredChapters = chapters.filterNot { chapter ->
                when {
                    skipRead && chapter.read -> true
                    skipFiltered -> isHiddenByFilters(manga, chapter)
                    else -> false
                }
            }
            if (filteredChapters.any { it.id == selectedId }) {
                filteredChapters
            } else {
                filteredChapters + selectedChapter
            }
        } else {
            chapters
        }

        return chaptersForReader
            .sortedWith(getChapterSort(manga, sortDescending = false))
            .let { if (readerPreferences.skipDupe.get()) it.removeDuplicates(selectedChapter) else it }
            .let { if (modePreferences.downloadedOnly.get()) it.filterDownloaded(manga, downloadCache) else it }
            .map(::ReaderChapter)
    }

    private fun isHiddenByFilters(manga: Manga, chapter: Chapter): Boolean {
        val downloaded by lazy {
            downloadManager.isChapterDownloaded(chapter.name, chapter.scanlator, chapter.url, manga.title, manga.source)
        }
        return (manga.unreadFilterRaw == Manga.CHAPTER_SHOW_READ && !chapter.read) ||
            (manga.unreadFilterRaw == Manga.CHAPTER_SHOW_UNREAD && chapter.read) ||
            (manga.downloadedFilterRaw == Manga.CHAPTER_SHOW_DOWNLOADED && !downloaded) ||
            (manga.downloadedFilterRaw == Manga.CHAPTER_SHOW_NOT_DOWNLOADED && downloaded) ||
            (manga.bookmarkedFilterRaw == Manga.CHAPTER_SHOW_BOOKMARKED && !chapter.bookmark) ||
            (manga.bookmarkedFilterRaw == Manga.CHAPTER_SHOW_NOT_BOOKMARKED && chapter.bookmark)
    }

    private fun buildContent(
        target: ReaderChapter,
        pages: List<com.nilemnod.mangareader.app.ui.reader.model.ReaderPage>,
        start: Int,
    ): ReaderChapterContent {
        val position = chapterList.indexOf(target)
        return ReaderChapterContent(
            id = target.chapter.id,
            title = target.chapter.name,
            pages = pages.map { page ->
                ComposeReaderPage(page.index, createPageSource(target, page))
            },
            hasPrevious = position > 0,
            hasNext = position < chapterList.lastIndex,
            previousTitle = chapterList.getOrNull(position - 1)?.chapter?.name,
            nextTitle = chapterList.getOrNull(position + 1)?.chapter?.name,
            readingMode = resolveReadingMode(),
            startPage = start,
        )
    }

    private suspend fun open(target: ReaderChapter, startPage: Int?) {
        openMutex.withLock {
            val chapterLoader = loader ?: return
            chapterLoader.loadChapter(target)
            val pages = target.pages ?: error("Chapter pages are not available")

            target.ref()
            window.forEach { it.unref() }
            window.clear()
            window += target
            contents.clear()
            current = target

            savedState[KEY_CHAPTER_ID] = target.chapter.id
            savedState[KEY_PAGE_INDEX] = startPage ?: target.requestedPage

            chapterToDownload = downloadManager.getQueuedDownloadOrNull(target.chapter.id)?.also {
                downloadManager.cancelQueuedDownloads(listOf(it))
            }

            val start = (startPage ?: target.requestedPage).coerceIn(0, pages.lastIndex)
            readStart = Clock.System.now()

            val content = buildContent(target, pages, start)
            contents += content
            rootContent = content
            mutableState.value = UiState.Content(content, contents.toList())
        }
    }

    fun appendNextChapter() = extendWindow(1)

    fun prependPreviousChapter() = extendWindow(-1)

    private fun extendWindow(delta: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                openMutex.withLock {
                    val root = rootContent ?: return@withLock
                    val edge = (if (delta > 0) window.lastOrNull() else window.firstOrNull()) ?: return@withLock
                    val target = chapterList.getOrNull(chapterList.indexOf(edge) + delta) ?: return@withLock
                    val chapterLoader = loader ?: return@withLock
                    chapterLoader.loadChapter(target)
                    val pages = target.pages ?: return@withLock

                    target.ref()
                    val content = buildContent(target, pages, 0)
                    if (delta > 0) {
                        window += target
                        contents += content
                    } else {
                        window.add(0, target)
                        contents.add(0, content)
                    }
                    trimWindow()
                    mutableState.value = UiState.Content(root, contents.toList())
                }
            } catch (e: Throwable) {
                if (e is CancellationException) throw e
                logcat(LogPriority.ERROR, e)
            }
        }
    }

    private fun trimWindow() {
        while (window.size > MAX_WINDOW) {
            val index = window.indexOf(current).coerceAtLeast(0)
            val removeFirst = index > window.lastIndex - index
            val removed = if (removeFirst) window.removeAt(0) else window.removeAt(window.lastIndex)
            if (removeFirst) contents.removeAt(0) else contents.removeAt(contents.lastIndex)
            removed.unref()
        }
    }

    private fun createPageSource(
        chapter: ReaderChapter,
        page: com.nilemnod.mangareader.app.ui.reader.model.ReaderPage,
    ): PageSource {
        return PageSource(key = "${chapter.chapter.id}:${page.index}") {
            openPageStream(chapter, page)
        }
    }

    private suspend fun openPageStream(
        chapter: ReaderChapter,
        page: com.nilemnod.mangareader.app.ui.reader.model.ReaderPage,
    ): BufferedSource {
        val stream = awaitPageStream(chapter, page)
        return withContext(Dispatchers.IO) { stream().source().buffer() }
    }

    private suspend fun awaitPageStream(
        chapter: ReaderChapter,
        page: com.nilemnod.mangareader.app.ui.reader.model.ReaderPage,
    ): () -> java.io.InputStream {
        val pageLoader = chapter.pageLoader ?: error("Chapter is not loaded")
        if (page.status is Page.State.Error) pageLoader.retryPage(page)
        if (page.status !is Page.State.Ready) {
            viewModelScope.launch(Dispatchers.IO) { pageLoader.loadPage(page) }
        }
        val result = page.statusFlow.first { it is Page.State.Ready || it is Page.State.Error }
        if (result is Page.State.Error) throw result.error
        return page.stream ?: error("Page stream is not available")
    }

    private fun generateFilename(
        manga: Manga,
        chapter: ReaderChapter,
        page: com.nilemnod.mangareader.app.ui.reader.model.ReaderPage,
    ): String {
        val suffix = " - ${page.number}"
        return DiskUtil.buildValidFilename(
            "${manga.title} - ${chapter.chapter.name}",
            DiskUtil.MAX_FILE_NAME_BYTES - suffix.byteSize(),
        ) + suffix
    }

    fun savePage(chapterId: Long, pageIndex: Int) {
        val chapter = current?.takeIf { it.chapter.id == chapterId } ?: return
        val page = chapter.pages?.getOrNull(pageIndex) ?: return
        val manga = manga ?: return

        val notifier = saveImageNotifier
        notifier.onClear()

        val filename = generateFilename(manga, chapter, page)
        val relativePath = if (readerPreferences.folderPerManga.get()) {
            DiskUtil.buildValidFilename(manga.title)
        } else {
            ""
        }

        appScope.launch(Dispatchers.IO) {
            try {
                val stream = awaitPageStream(chapter, page)
                val uri = imageSaver.save(
                    Image.Page(
                        inputStream = stream,
                        name = filename,
                        location = Location.Pictures.create(relativePath),
                    ),
                )
                withContext(Dispatchers.Main) { notifier.onComplete(uri) }
                eventChannel.send(Event.PictureSaved)
            } catch (e: Throwable) {
                if (e is CancellationException) throw e
                logcat(LogPriority.ERROR, e)
                notifier.onError(e.message)
            }
        }
    }

    fun sharePage(chapterId: Long, pageIndex: Int) {
        val chapter = current?.takeIf { it.chapter.id == chapterId } ?: return
        val page = chapter.pages?.getOrNull(pageIndex) ?: return
        val manga = manga ?: return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val stream = awaitPageStream(chapter, page)
                context.cacheImageDir.deleteRecursively()
                val uri = imageSaver.save(
                    Image.Page(
                        inputStream = stream,
                        name = generateFilename(manga, chapter, page),
                        location = Location.Cache,
                    ),
                )
                val message = context.stringResource(
                    MR.strings.share_page_info,
                    manga.title,
                    chapter.chapter.name,
                    page.number,
                )
                eventChannel.send(Event.SharePage(uri, message))
            } catch (e: Throwable) {
                if (e is CancellationException) throw e
                logcat(LogPriority.ERROR, e)
            }
        }
    }

    fun onPageChanged(chapterId: Long, pageIndex: Int) {
        val readerChapter = window.firstOrNull { it.chapter.id == chapterId } ?: return
        if (readerChapter !== current) switchCurrent(readerChapter)
        savedState[KEY_PAGE_INDEX] = pageIndex
        readerChapter.requestedPage = pageIndex
        if (incognito) return

        val lastIndex = readerChapter.pages?.lastIndex ?: return
        val read = readerChapter.chapter.read || pageIndex == lastIndex
        readerChapter.update { it.copy(lastPageRead = pageIndex, read = read) }

        pendingSave?.cancel()
        if ((pageIndex + 1).toDouble() / (lastIndex + 1) > DOWNLOAD_AHEAD_THRESHOLD) {
            downloadNextChapters(readerChapter)
        }

        if (pageIndex == lastIndex) {
            pendingSave = null
            writeProgress(readerChapter, onComplete = true)
        } else {
            pendingSave = viewModelScope.launch {
                delay(PROGRESS_SAVE_DELAY)
                writeProgress(readerChapter)
            }
        }
    }

    private fun switchCurrent(target: ReaderChapter) {
        val previous = current
        if (previous != null) {
            pendingSave?.cancel()
            pendingSave = null
            if (!incognito) {
                writeProgress(previous)
                val start = readStart
                val end = Clock.System.now()
                appScope.launch(Dispatchers.IO) {
                    val duration = start?.let { (end - it).inWholeMilliseconds } ?: 0L
                    upsertHistory.await(HistoryUpdate(previous.chapter.id, end, duration))
                }
            }
        }
        readStart = Clock.System.now()
        current = target
        savedState[KEY_CHAPTER_ID] = target.chapter.id
    }

    private fun writeProgress(readerChapter: ReaderChapter, onComplete: Boolean = false) {
        appScope.launch(Dispatchers.IO) {
            progressWrites.withLock {
                if (onComplete) updateChapterProgressOnComplete(readerChapter)
                val chapter = readerChapter.chapter
                updateChapter.await(
                    ChapterUpdate(chapter.id) {
                        read = chapter.read
                        lastPageRead = chapter.lastPageRead
                    },
                )
            }
        }
    }

    private suspend fun updateChapterProgressOnComplete(readerChapter: ReaderChapter) {
        deleteChapterIfNeeded(readerChapter)

        if (!markDuplicatesAsRead()) return
        val manga = manga ?: return

        val duplicateUnreadChapters = getUnfilteredChapterList(manga)
            .filter {
                !it.read && it.isRecognizedNumber && it.chapterNumber == readerChapter.chapter.chapterNumber
            }
            .map { ChapterUpdate(it.id) { read = true } }
        updateChapter.awaitAll(duplicateUnreadChapters)
    }

    private fun markDuplicatesAsRead(): Boolean {
        return libraryPreferences.markDuplicateReadChapterAsRead.get()
            .contains(LibraryPreferences.MARK_DUPLICATE_CHAPTER_READ_EXISTING)
    }

    private suspend fun deleteChapterIfNeeded(currentChapter: ReaderChapter) {
        val removeAfterReadSlots = downloadPreferences.removeAfterReadSlots.get()
        if (removeAfterReadSlots == -1) return
        val manga = manga ?: return

        val position = chapterList.indexOf(currentChapter)
        val chapterToDelete = chapterList.getOrNull(position - removeAfterReadSlots)

        chapterToDownload = null

        if (chapterToDelete == null || !chapterToDelete.chapter.read) return

        val duplicateChapters = if (markDuplicatesAsRead()) {
            getUnfilteredChapterList(manga)
                .filter {
                    it.id != chapterToDelete.chapter.id &&
                        it.isRecognizedNumber &&
                        it.chapterNumber == chapterToDelete.chapter.chapterNumber
                }
                .map { it.copy(read = true) }
        } else {
            emptyList()
        }
        downloadManager.enqueueChaptersToDelete(listOf(chapterToDelete.chapter) + duplicateChapters, manga)
    }

    private fun downloadNextChapters(currentChapter: ReaderChapter) {
        if (downloadAheadAmount == 0) return
        val manga = manga ?: return
        if (currentChapter.pageLoader !is DownloadPageLoader) return
        val nextChapter = chapterList.getOrNull(chapterList.indexOf(currentChapter) + 1)?.chapter ?: return

        viewModelScope.launch(Dispatchers.IO) {
            val isNextChapterDownloaded = downloadManager.isChapterDownloaded(
                nextChapter.name,
                nextChapter.scanlator,
                nextChapter.url,
                manga.title,
                manga.source,
            )
            if (!isNextChapterDownloaded) return@launch

            val chaptersToDownload = getNextChapters.await(manga.id, nextChapter.id)
                .let { if (readerPreferences.skipDupe.get()) it.removeDuplicates(nextChapter) else it }
                .take(downloadAheadAmount)

            downloadManager.downloadChapters(manga, chaptersToDownload)
        }
    }

    fun onActivityFinish() {
        appScope.launch {
            progressWrites.withLock {}
            downloadManager.deletePendingChapters()
        }
    }

    private fun resolveReadingMode(): ReaderMode {
        modeOverride?.let { return it }
        val stored = AppReadingMode.fromPreference(manga?.readingMode?.toInt())
        val resolved = if (stored == AppReadingMode.DEFAULT) {
            AppReadingMode.fromPreference(readerPreferences.defaultReadingMode.get())
        } else {
            stored
        }
        return when (resolved) {
            AppReadingMode.LEFT_TO_RIGHT -> ReaderMode.PagedLeftToRight
            AppReadingMode.RIGHT_TO_LEFT -> ReaderMode.PagedRightToLeft
            AppReadingMode.VERTICAL,
            AppReadingMode.WEBTOON,
            AppReadingMode.CONTINUOUS_VERTICAL,
            -> ReaderMode.Webtoon
            AppReadingMode.DEFAULT -> ReaderMode.PagedRightToLeft
        }
    }

    fun setReadingMode(mode: ReaderMode) {
        modeOverride = mode
        val detached = current?.takeIf { mode != ReaderMode.Webtoon && it.chapter.id != rootContent?.id }
        if (detached != null) {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    flushProgress()
                    open(detached, detached.requestedPage)
                } catch (e: Throwable) {
                    if (e is CancellationException) throw e
                    logcat(LogPriority.ERROR, e)
                    mutableState.value = UiState.Error(e)
                }
            }
        }
        val currentManga = manga ?: return
        val flag = when (mode) {
            ReaderMode.PagedLeftToRight -> AppReadingMode.LEFT_TO_RIGHT
            ReaderMode.PagedRightToLeft -> AppReadingMode.RIGHT_TO_LEFT
            ReaderMode.Webtoon -> AppReadingMode.WEBTOON
        }.flagValue.toLong()
        appScope.launch(Dispatchers.IO) {
            setMangaViewerFlags.awaitSetReadingMode(currentManga.id, flag)
        }
    }

    fun loadNext() = openAdjacent(1)

    fun loadPrevious() = openAdjacent(-1)

    private fun openAdjacent(delta: Int) {
        val currentChapter = current ?: return
        val target = chapterList.getOrNull(chapterList.indexOf(currentChapter) + delta) ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                flushProgress()
                updateHistory()
                open(target, if (delta < 0) Int.MAX_VALUE else null)
            } catch (e: Throwable) {
                if (e is CancellationException) throw e
                logcat(LogPriority.ERROR, e)
                mutableState.value = UiState.Error(e)
            }
        }
    }

    fun restartReadTimer() {
        readStart = Clock.System.now()
    }

    fun saveHistory() {
        appScope.launch(Dispatchers.IO) {
            flushProgress()
            updateHistory()
        }
    }

    private fun flushProgress() {
        pendingSave?.cancel()
        pendingSave = null
        val readerChapter = current ?: return
        if (!incognito) writeProgress(readerChapter)
    }

    private suspend fun updateHistory() {
        val readerChapter = current ?: return
        if (incognito) return
        val end = Clock.System.now()
        val duration = readStart?.let { (end - it).inWholeMilliseconds } ?: 0L
        readStart = null
        upsertHistory.await(HistoryUpdate(readerChapter.chapter.id, end, duration))
    }

    private fun canLoadAhead(): Boolean {
        return !readerPreferences.loadAheadOnlyOverWifi.get() || context.activeNetworkState().isWifi
    }

    override fun onCleared() {
        window.forEach { it.unref() }
        window.clear()
        current = null
        chapterToDownload?.let {
            appScope.launch { downloadManager.addDownloadsToStartOfQueue(listOf(it)) }
        }
    }

    private companion object {
        const val MAX_WINDOW = 4
        const val KEY_CHAPTER_ID = "chapter_id"
        const val KEY_PAGE_INDEX = "page_index"
        const val PROGRESS_SAVE_DELAY = 1000L
        const val DOWNLOAD_AHEAD_THRESHOLD = 0.25
    }
}
