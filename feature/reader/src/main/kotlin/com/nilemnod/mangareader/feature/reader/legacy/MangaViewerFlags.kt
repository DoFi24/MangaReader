package com.nilemnod.mangareader.feature.reader.legacy

import com.nilemnod.mangareader.core.reader.ReaderOrientation
import com.nilemnod.mangareader.core.reader.ReadingMode
import com.nilemnod.mangareader.domain.manga.model.Manga

val Manga.readingMode: Long
    get() = viewerFlags and ReadingMode.MASK.toLong()

val Manga.readerOrientation: Long
    get() = viewerFlags and ReaderOrientation.MASK.toLong()
