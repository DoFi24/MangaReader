package com.nilemnod.mangareader.app.domain.manga.model

import android.content.Context
import com.nilemnod.mangareader.app.di.appGraph
import com.nilemnod.mangareader.core.common.preference.TriState
import com.nilemnod.mangareader.core.reader.ReaderOrientation
import com.nilemnod.mangareader.core.reader.ReadingMode
import com.nilemnod.mangareader.core.storage.CoverCache
import com.nilemnod.mangareader.domain.manga.model.Manga
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

// TODO: move these into the domain model
val Manga.readingMode: Long
    get() = viewerFlags and ReadingMode.MASK.toLong()

val Manga.readerOrientation: Long
    get() = viewerFlags and ReaderOrientation.MASK.toLong()
