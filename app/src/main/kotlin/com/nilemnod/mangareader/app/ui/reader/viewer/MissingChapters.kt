package com.nilemnod.mangareader.app.ui.reader.viewer

import com.nilemnod.mangareader.app.ui.reader.model.ReaderChapter
import com.nilemnod.mangareader.domain.chapter.service.calculateChapterGap as domainCalculateChapterGap

fun calculateChapterGap(higherReaderChapter: ReaderChapter?, lowerReaderChapter: ReaderChapter?): Int {
    return domainCalculateChapterGap(
        higherReaderChapter?.chapter,
        lowerReaderChapter?.chapter,
    )
}
