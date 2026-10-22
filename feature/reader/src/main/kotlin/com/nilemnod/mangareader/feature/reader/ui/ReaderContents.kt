package com.nilemnod.mangareader.feature.reader.ui

import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import coil3.ImageLoader
import coil3.request.ImageRequest
import coil3.size.Size
import com.nilemnod.mangareader.feature.reader.model.ReaderChapterContent
import com.nilemnod.mangareader.feature.reader.model.ReaderPage
import com.nilemnod.mangareader.feature.reader.settings.PageFit
import com.nilemnod.mangareader.feature.reader.settings.ReaderSettings
import com.nilemnod.mangareader.feature.reader.settings.ReadingMode
import com.nilemnod.mangareader.feature.reader.settings.colorFilter
import kotlinx.coroutines.flow.Flow

internal sealed interface ReaderCommand {
    data class GoTo(val index: Int) : ReaderCommand
    data class ScrollScreen(val direction: Int) : ReaderCommand
    data class PanelStep(val delta: Int) : ReaderCommand
}

private enum class SpreadPart { Whole, Primary, Secondary }

@Immutable
private data class PageEntry(val page: ReaderPage, val part: SpreadPart) {
    val key: String = if (part == SpreadPart.Whole) page.source.key else "${page.source.key}#${part.name}"
}

@Composable
internal fun PagedReaderContent(
    chapter: ReaderChapterContent,
    settings: ReaderSettings,
    imageLoader: ImageLoader,
    commands: Flow<ReaderCommand>,
    onPageChanged: (Int) -> Unit,
    onOpenNext: () -> Unit,
    onStepPage: (Int) -> Unit,
    onTap: (Offset, IntSize) -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val lastIndex = (chapter.pages.size - 1).coerceAtLeast(0)
    val colorFilter = remember(settings.brightness, settings.contrast, settings.grayscale, settings.invertColors) {
        settings.colorFilter()
    }
    val reversed = settings.mode == ReadingMode.PagedRightToLeft
    val sessions = remember { mutableMapOf<String, PanelSession>() }
    val currentOnStepPage by rememberUpdatedState(onStepPage)
    val currentReversed by rememberUpdatedState(reversed)

    var spreads by remember(chapter.id) { mutableStateOf(emptySet<String>()) }
    val entries = remember(chapter.pages, spreads, settings.splitSpreads) {
        chapter.pages.flatMap { page ->
            if (settings.splitSpreads && page.source.key in spreads) {
                listOf(PageEntry(page, SpreadPart.Primary), PageEntry(page, SpreadPart.Secondary))
            } else {
                listOf(PageEntry(page, SpreadPart.Whole))
            }
        }
    }
    val currentEntries by rememberUpdatedState(entries)
    val animateCommands by rememberUpdatedState(!settings.eInkMode)

    val pagerState = rememberPagerState(
        initialPage = entries.indexOfFirst { it.page.index == chapter.startPage }.coerceAtLeast(0),
    ) {
        entries.size + 1
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { currentEntries.getOrNull(pagerState.currentPage)?.page?.index ?: lastIndex }
            .collect(onPageChanged)
    }
    LaunchedEffect(commands, lastIndex) {
        commands.collect { command ->
            when (command) {
                is ReaderCommand.GoTo -> {
                    val target = currentEntries.indexOfFirst { it.page.index == command.index.coerceIn(0, lastIndex) }
                        .coerceAtLeast(0)
                    if (animateCommands) {
                        pagerState.animateScrollToPage(target)
                    } else {
                        pagerState.scrollToPage(target)
                    }
                }
                is ReaderCommand.PanelStep -> {
                    val key = currentEntries.getOrNull(pagerState.currentPage)?.key
                    val consumed = key?.let { sessions[it]?.step(command.delta, currentReversed) } ?: false
                    if (!consumed) currentOnStepPage(command.delta)
                }
                is ReaderCommand.ScrollScreen -> Unit
            }
        }
    }

    val context = LocalContext.current
    LaunchedEffect(pagerState.currentPage, chapter.id) {
        for (offset in 1..PREFETCH_PAGES) {
            val page = currentEntries.getOrNull(pagerState.currentPage + offset)?.page ?: break
            imageLoader.enqueue(
                ImageRequest.Builder(context)
                    .data(page.source)
                    .size(Size(PREFETCH_SIZE, PREFETCH_SIZE))
                    .build(),
            )
        }
    }

    HorizontalPager(
        state = pagerState,
        modifier = modifier.fillMaxSize(),
        reverseLayout = reversed,
        beyondViewportPageCount = 1,
        key = { entries.getOrNull(it)?.key ?: TRANSITION_KEY },
    ) { index ->
        val entry = entries.getOrNull(index)
        if (entry == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = { onTap(it, size) })
                    },
            ) {
                ChapterTransition(
                    finishedTitle = chapter.title,
                    nextTitle = chapter.nextTitle.takeIf { chapter.hasNext },
                    onOpenNext = onOpenNext,
                )
            }
        } else {
            val half = when (entry.part) {
                SpreadPart.Whole -> null
                SpreadPart.Primary -> if (reversed) PageHalf.Right else PageHalf.Left
                SpreadPart.Secondary -> if (reversed) PageHalf.Left else PageHalf.Right
            }
            val onWide: (() -> Unit)? = if (settings.splitSpreads && entry.part == SpreadPart.Whole) {
                { spreads = spreads + entry.page.source.key }
            } else {
                null
            }
            if (settings.fit == PageFit.Screen) {
                ZoomablePage(
                    page = entry.page,
                    doubleTapZoom = settings.doubleTapZoom,
                    doubleTapScale = settings.doubleTapScale,
                    panelZoom = settings.panelZoom,
                    cropBorders = settings.cropBorders,
                    half = half,
                    onWide = onWide,
                    imageLoader = imageLoader,
                    colorFilter = colorFilter,
                    onTap = onTap,
                    onLongPress = onLongPress,
                    sessions = sessions,
                    sessionKey = entry.key,
                )
            } else {
                ScrollingPage(
                    page = entry.page,
                    fit = settings.fit,
                    imageLoader = imageLoader,
                    colorFilter = colorFilter,
                    cropBorders = settings.cropBorders,
                    half = half,
                    onWide = onWide,
                    onTap = onTap,
                    onLongPress = onLongPress,
                )
            }
        }
    }
}

@Immutable
private sealed interface WebtoonEntry {
    val chapter: ReaderChapterContent
    val key: String

    @Immutable
    class Page(override val chapter: ReaderChapterContent, val page: ReaderPage) : WebtoonEntry {
        override val key: String = page.source.key
    }

    @Immutable
    class Divider(override val chapter: ReaderChapterContent) : WebtoonEntry {
        override val key: String = "$TRANSITION_KEY:${chapter.id}"
    }
}

@Composable
internal fun WebtoonReaderContent(
    chapters: List<ReaderChapterContent>,
    startPage: Int,
    settings: ReaderSettings,
    imageLoader: ImageLoader,
    commands: Flow<ReaderCommand>,
    onPositionChanged: (chapterId: Long, pageIndex: Int) -> Unit,
    onReachedEnd: (chapterId: Long) -> Unit,
    onNearEnd: () -> Unit,
    onOpenNext: () -> Unit,
    onTap: (Offset, IntSize) -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val entries = remember(chapters) {
        buildList {
            chapters.forEach { chapter ->
                chapter.pages.forEach { add(WebtoonEntry.Page(chapter, it)) }
                add(WebtoonEntry.Divider(chapter))
            }
        }
    }
    val lastEntry = entries.lastIndex
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = startPage.coerceIn(0, (chapters.first().pages.size - 1).coerceAtLeast(0)),
    )
    val colorFilter = remember(settings.brightness, settings.contrast, settings.grayscale, settings.invertColors) {
        settings.colorFilter()
    }
    val currentEntries by rememberUpdatedState(entries)
    val currentOnPositionChanged by rememberUpdatedState(onPositionChanged)
    val currentOnReachedEnd by rememberUpdatedState(onReachedEnd)
    val currentOnNearEnd by rememberUpdatedState(onNearEnd)

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }.collect { index ->
            val entry = currentEntries.getOrNull(index) ?: return@collect
            val pageIndex = when (entry) {
                is WebtoonEntry.Page -> entry.page.index
                is WebtoonEntry.Divider -> (entry.chapter.pages.size - 1).coerceAtLeast(0)
            }
            currentOnPositionChanged(entry.chapter.id, pageIndex)
        }
    }
    LaunchedEffect(listState, entries) {
        snapshotFlow {
            val info = listState.layoutInfo
            info.visibleItemsInfo.mapNotNull { item ->
                (currentEntries.getOrNull(item.index) as? WebtoonEntry.Divider)
                    ?.takeIf { item.offset + item.size <= info.viewportEndOffset }
                    ?.chapter?.id
            }
        }.collect { ids -> ids.forEach(currentOnReachedEnd) }
    }
    LaunchedEffect(listState, entries) {
        snapshotFlow {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible >= lastEntry - PRELOAD_ENTRIES
        }.collect { near -> if (near) currentOnNearEnd() }
    }
    LaunchedEffect(commands) {
        commands.collect { command ->
            when (command) {
                is ReaderCommand.GoTo -> {
                    val active = currentEntries.getOrNull(listState.firstVisibleItemIndex)?.chapter ?: return@collect
                    val start = currentEntries.indexOfFirst { it.chapter.id == active.id }
                    listState.scrollToItem((start + command.index).coerceIn(0, currentEntries.lastIndex))
                }
                is ReaderCommand.ScrollScreen -> {
                    val height = listState.layoutInfo.viewportSize.height
                    listState.animateScrollBy(command.direction * height * SCREEN_SCROLL_FRACTION)
                }
                is ReaderCommand.PanelStep -> Unit
            }
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { onTap(it, size) }, onLongPress = { onLongPress() })
            },
    ) {
        items(entries, key = { it.key }) { entry ->
            when (entry) {
                is WebtoonEntry.Page -> PageImage(
                    page = entry.page,
                    imageLoader = imageLoader,
                    contentScale = ContentScale.FillWidth,
                    colorFilter = colorFilter,
                    targetSize = null,
                    cropBorders = settings.cropBorders,
                    modifier = Modifier.fillMaxWidth(),
                )
                is WebtoonEntry.Divider -> {
                    val chapter = entry.chapter
                    if (chapter === chapters.last()) {
                        ChapterTransition(
                            finishedTitle = chapter.title,
                            nextTitle = chapter.nextTitle.takeIf { chapter.hasNext },
                            onOpenNext = onOpenNext,
                            modifier = Modifier.fillParentMaxHeight(),
                        )
                    } else {
                        ChapterDivider(
                            finishedTitle = chapter.title,
                            nextTitle = chapter.nextTitle,
                        )
                    }
                }
            }
        }
    }
}

private const val TRANSITION_KEY = "chapter_transition"
private const val PRELOAD_ENTRIES = 8
private const val PREFETCH_PAGES = 2
private const val PREFETCH_SIZE = 2400
private const val SCREEN_SCROLL_FRACTION = 0.85f
