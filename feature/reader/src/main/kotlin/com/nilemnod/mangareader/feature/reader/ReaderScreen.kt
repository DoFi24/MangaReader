package com.nilemnod.mangareader.feature.reader

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.request.ImageRequest
import coil3.size.Size
import com.nilemnod.mangareader.feature.reader.model.ReaderChapterContent
import com.nilemnod.mangareader.feature.reader.model.ReaderHost
import com.nilemnod.mangareader.feature.reader.model.ReaderRemote
import com.nilemnod.mangareader.feature.reader.settings.PageFit
import com.nilemnod.mangareader.feature.reader.settings.ReaderBackground
import com.nilemnod.mangareader.feature.reader.settings.ReaderSettings
import com.nilemnod.mangareader.feature.reader.settings.ReaderSettingsStore
import com.nilemnod.mangareader.feature.reader.settings.ReadingMode
import com.nilemnod.mangareader.feature.reader.ui.PageActionsSheet
import com.nilemnod.mangareader.feature.reader.ui.PageNumberBadge
import com.nilemnod.mangareader.feature.reader.ui.PagedReaderContent
import com.nilemnod.mangareader.feature.reader.ui.ReaderBottomBar
import com.nilemnod.mangareader.feature.reader.ui.ReaderCommand
import com.nilemnod.mangareader.feature.reader.ui.ReaderQuickSettings
import com.nilemnod.mangareader.feature.reader.ui.ReaderSettingsSheet
import com.nilemnod.mangareader.feature.reader.ui.ReaderTopBar
import com.nilemnod.mangareader.feature.reader.ui.WebtoonReaderContent
import com.nilemnod.mangareader.i18n.MR
import com.nilemnod.mangareader.presentation.core.i18n.stringResource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow

@Composable
fun ReaderScreen(
    chapter: ReaderChapterContent,
    settingsStore: ReaderSettingsStore,
    host: ReaderHost,
    imageLoader: ImageLoader,
    modifier: Modifier = Modifier,
    remote: ReaderRemote = remember { ReaderRemote() },
    chapters: List<ReaderChapterContent> = listOf(chapter),
) {
    val storedSettings by settingsStore.settings.collectAsState()
    var chromeVisible by rememberSaveable { mutableStateOf(false) }
    var settingsVisible by rememberSaveable { mutableStateOf(false) }
    var modeOverride by rememberSaveable { mutableStateOf<ReadingMode?>(null) }

    val mode = modeOverride ?: chapter.readingMode
    val settings = remember(storedSettings, mode) { storedSettings.copy(mode = mode) }

    val view = LocalView.current
    DisposableEffect(view, settings.keepScreenOn) {
        view.keepScreenOn = settings.keepScreenOn
        onDispose { view.keepScreenOn = false }
    }

    BackHandler(enabled = chromeVisible) { chromeVisible = false }

    val background = when (settings.background) {
        ReaderBackground.Black -> Color.Black
        ReaderBackground.Gray -> Color(GRAY_BACKGROUND)
        ReaderBackground.White -> Color.White
        ReaderBackground.Automatic -> MaterialTheme.colorScheme.background
    }
    val contentColor = if (background.luminance() < LUMINANCE_THRESHOLD) Color.White else Color.Black

    CompositionLocalProvider(LocalContentColor provides contentColor) {
        key(chapter.id) {
            ReaderChapterScreen(
                chapter = chapter,
                chapters = chapters,
                settings = settings,
                background = background,
                host = host,
                remote = remote,
                imageLoader = imageLoader,
                chromeVisible = chromeVisible,
                onChromeVisibleChange = { chromeVisible = it },
                settingsVisible = settingsVisible,
                onSettingsVisibleChange = { settingsVisible = it },
                onUpdateSettings = settingsStore::update,
                onModeChange = { newMode ->
                    modeOverride = newMode
                    host.onReadingModeChanged(newMode)
                },
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun ReaderChapterScreen(
    chapter: ReaderChapterContent,
    chapters: List<ReaderChapterContent>,
    settings: ReaderSettings,
    background: Color,
    host: ReaderHost,
    remote: ReaderRemote,
    imageLoader: ImageLoader,
    chromeVisible: Boolean,
    onChromeVisibleChange: (Boolean) -> Unit,
    settingsVisible: Boolean,
    onSettingsVisibleChange: (Boolean) -> Unit,
    onUpdateSettings: ((ReaderSettings) -> ReaderSettings) -> Unit,
    onModeChange: (ReadingMode) -> Unit,
    modifier: Modifier,
) {
    val all = chapters.ifEmpty { listOf(chapter) }
    var activeId by remember { mutableLongStateOf(chapter.id) }
    val active = if (settings.mode == ReadingMode.Webtoon) {
        all.firstOrNull { it.id == activeId } ?: all.first()
    } else {
        chapter
    }
    val pageCount = active.pages.size
    val lastIndex = (pageCount - 1).coerceAtLeast(0)
    var currentPage by remember { mutableIntStateOf(chapter.startPage.coerceIn(0, (chapter.pages.size - 1).coerceAtLeast(0))) }
    val commands = remember { MutableSharedFlow<ReaderCommand>(extraBufferCapacity = 8) }
    val finished = remember { mutableSetOf<Long>() }
    val reportFinished = { id: Long -> if (finished.add(id)) host.onChapterFinished(id) }
    var reportedId by remember { mutableLongStateOf(chapter.id) }
    var pageActionsVisible by remember { mutableStateOf(false) }
    val onLongPress = { pageActionsVisible = true }

    var flashVisible by remember { mutableStateOf(false) }
    var firstPageShown by remember { mutableStateOf(false) }
    LaunchedEffect(currentPage) {
        if (settings.eInkMode && firstPageShown) {
            flashVisible = true
            delay(FLASH_MS)
            flashVisible = false
        }
        firstPageShown = true
    }
    LaunchedEffect(active.id, currentPage) {
        if (reportedId != active.id) {
            val from = all.indexOfFirst { it.id == reportedId }
            val to = all.indexOfFirst { it.id == active.id }
            if (from in 0 until to) host.onPageChanged(reportedId, all[from].pages.lastIndex)
            reportedId = active.id
        }
        host.onPageChanged(active.id, currentPage)
        if (pageCount > 0 && currentPage == lastIndex) reportFinished(active.id)
    }

    val context = LocalContext.current
    val preloadNext = settings.mode != ReadingMode.Webtoon &&
        chapter.hasNext &&
        all.size == 1 &&
        currentPage >= lastIndex / 2
    LaunchedEffect(preloadNext, chapter.id) {
        if (preloadNext) host.onAppendNextChapter()
    }
    LaunchedEffect(all.size, settings.mode) {
        if (settings.mode == ReadingMode.Webtoon) return@LaunchedEffect
        all.getOrNull(1)?.pages?.take(PRELOAD_NEXT_PAGES)?.forEach { page ->
            imageLoader.enqueue(
                ImageRequest.Builder(context)
                    .data(page.source)
                    .size(Size(PRELOAD_SIZE, PRELOAD_SIZE))
                    .build(),
            )
        }
    }

    fun navigatePage(delta: Int) {
        val target = currentPage + delta
        when {
            target > lastIndex -> if (active.hasNext) host.onRequestNextChapter()
            target < 0 -> if (active.hasPrevious) host.onRequestPreviousChapter()
            else -> commands.tryEmit(ReaderCommand.GoTo(target))
        }
    }

    val panelNavigation = settings.panelNavigation &&
        settings.mode != ReadingMode.Webtoon &&
        settings.fit == PageFit.Screen
    fun navigate(delta: Int) {
        if (panelNavigation) {
            commands.tryEmit(ReaderCommand.PanelStep(delta))
        } else {
            navigatePage(delta)
        }
    }

    LaunchedEffect(remote, settings.mode) {
        remote.steps.collect { delta ->
            if (settings.mode == ReadingMode.Webtoon) {
                commands.tryEmit(ReaderCommand.ScrollScreen(delta))
            } else {
                navigate(delta)
            }
        }
    }

    val onTap: (Offset, IntSize) -> Unit = tap@{ offset, size ->
        if (size.width == 0 || size.height == 0) return@tap
        if (settings.mode == ReadingMode.Webtoon) {
            val y = offset.y / size.height
            when {
                y < EDGE_ZONE -> commands.tryEmit(ReaderCommand.ScrollScreen(-1))
                y > 1f - EDGE_ZONE -> commands.tryEmit(ReaderCommand.ScrollScreen(1))
                else -> onChromeVisibleChange(!chromeVisible)
            }
        } else {
            val x = offset.x / size.width
            val isLeft = x < EDGE_ZONE
            val isRight = x > 1f - EDGE_ZONE
            if (!isLeft && !isRight) {
                onChromeVisibleChange(!chromeVisible)
            } else {
                val rightIsNext = (settings.mode == ReadingMode.PagedLeftToRight) != settings.invertTapZones
                navigate(if (isRight == rightIsNext) 1 else -1)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize().background(background)) {
        if (chapter.pages.isEmpty()) {
            Text(
                text = stringResource(MR.strings.page_list_empty_error),
                modifier = Modifier.align(Alignment.Center),
            )
        } else if (settings.mode == ReadingMode.Webtoon) {
            WebtoonReaderContent(
                chapters = all,
                anchorId = chapter.id,
                startPage = currentPage,
                settings = settings,
                imageLoader = imageLoader,
                commands = commands,
                onPositionChanged = { id, index ->
                    activeId = id
                    currentPage = index
                },
                onReachedEnd = reportFinished,
                onNearEnd = { if (all.last().hasNext) host.onAppendNextChapter() },
                onNearStart = { if (all.first().hasPrevious) host.onPrependPreviousChapter() },
                onOpenNext = host::onRequestNextChapter,
                onTap = onTap,
                onLongPress = onLongPress,
            )
        } else {
            PagedReaderContent(
                chapter = chapter,
                settings = settings,
                imageLoader = imageLoader,
                commands = commands,
                onPageChanged = { currentPage = it },
                onOpenNext = host::onRequestNextChapter,
                onStepPage = ::navigatePage,
                onTap = onTap,
                onLongPress = onLongPress,
            )
        }

        if (flashVisible) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black))
        }

        if (!chromeVisible && settings.showPageNumber && pageCount > 0) {
            PageNumberBadge(
                currentPage = currentPage,
                pageCount = pageCount,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 8.dp),
            )
        }

        AnimatedVisibility(
            visible = chromeVisible,
            modifier = Modifier.align(Alignment.TopCenter),
            enter = fadeIn() + slideInVertically { -it },
            exit = fadeOut() + slideOutVertically { -it },
        ) {
            ReaderTopBar(
                title = active.title,
                onBack = host::onExit,
                onOpenSettings = { onSettingsVisibleChange(true) },
            )
        }

        AnimatedVisibility(
            visible = chromeVisible && pageCount > 0,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it },
        ) {
            Column {
                ReaderQuickSettings(
                    settings = settings,
                    onUpdate = onUpdateSettings,
                    onModeChange = onModeChange,
                    onOpenAll = { onSettingsVisibleChange(true) },
                )
                ReaderBottomBar(
                    currentPage = currentPage,
                    pageCount = pageCount,
                    rightToLeft = settings.mode == ReadingMode.PagedRightToLeft,
                    hasPrevious = active.hasPrevious,
                    hasNext = active.hasNext,
                    onSeek = { commands.tryEmit(ReaderCommand.GoTo(it)) },
                    onPreviousChapter = host::onRequestPreviousChapter,
                    onNextChapter = host::onRequestNextChapter,
                )
            }
        }

        if (pageActionsVisible) {
            PageActionsSheet(
                onSave = { host.onSavePage(active.id, currentPage) },
                onShare = { host.onSharePage(active.id, currentPage) },
                onDismiss = { pageActionsVisible = false },
            )
        }

        if (settingsVisible) {
            ReaderSettingsSheet(
                settings = settings,
                onUpdate = onUpdateSettings,
                onModeChange = onModeChange,
                onDismiss = { onSettingsVisibleChange(false) },
            )
        }
    }
}

private const val EDGE_ZONE = 0.3f
private const val PRELOAD_NEXT_PAGES = 2
private const val PRELOAD_SIZE = 2400
private const val FLASH_MS = 80L
private const val GRAY_BACKGROUND = 0xFF202125
private const val LUMINANCE_THRESHOLD = 0.5f
