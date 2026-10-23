package com.nilemnod.mangareader.feature.reader.legacy.viewer

import com.nilemnod.mangareader.feature.reader.legacy.model.ReaderChapter
import com.nilemnod.mangareader.domain.chapter.service.calculateChapterGap as domainCalculateChapterGap

fun calculateChapterGap(higherReaderChapter: ReaderChapter?, lowerReaderChapter: ReaderChapter?): Int {
    return domainCalculateChapterGap(
        higherReaderChapter?.chapter,
        lowerReaderChapter?.chapter,
    )
}
