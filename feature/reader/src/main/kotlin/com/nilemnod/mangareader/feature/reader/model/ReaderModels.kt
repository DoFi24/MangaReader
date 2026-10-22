package com.nilemnod.mangareader.feature.reader.model

import androidx.compose.runtime.Immutable
import com.nilemnod.mangareader.feature.reader.settings.ReadingMode
import kotlinx.coroutines.flow.MutableSharedFlow
import okio.BufferedSource

@Immutable
class PageSource(
    val key: String,
    val open: suspend () -> BufferedSource,
) {
    override fun equals(other: Any?): Boolean = other is PageSource && other.key == key

    override fun hashCode(): Int = key.hashCode()
}

@Immutable
data class ReaderPage(
    val index: Int,
    val source: PageSource,
)

@Immutable
data class ReaderChapterContent(
    val id: Long,
    val title: String,
    val pages: List<ReaderPage>,
    val hasPrevious: Boolean,
    val hasNext: Boolean,
    val previousTitle: String? = null,
    val nextTitle: String? = null,
    val readingMode: ReadingMode = ReadingMode.PagedRightToLeft,
    val startPage: Int = 0,
)

interface ReaderHost {
    fun onPageChanged(chapterId: Long, pageIndex: Int)
    fun onChapterFinished(chapterId: Long)
    fun onRequestPreviousChapter()
    fun onRequestNextChapter()
    fun onAppendNextChapter()
    fun onPrependPreviousChapter()
    fun onReadingModeChanged(mode: ReadingMode)
    fun onSavePage(chapterId: Long, pageIndex: Int)
    fun onSharePage(chapterId: Long, pageIndex: Int)
    fun onExit()
}

class ReaderRemote {
    internal val steps = MutableSharedFlow<Int>(extraBufferCapacity = 4)

    fun step(delta: Int) {
        steps.tryEmit(delta)
    }
}
