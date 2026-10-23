package com.nilemnod.mangareader.domain.chapter.model

import com.nilemnod.mangareader.core.common.extensions.toEpochMillisOrZero
import com.nilemnod.mangareader.core.common.extensions.toInstantOrNull
import com.nilemnod.mangareader.domain.source.api.SourceChapter

fun Chapter.toSourceChapter(): SourceChapter = SourceChapter(
    url = url,
    name = name,
    chapterNumber = chapterNumber,
    scanlator = scanlator,
    dateUpload = dateUpload.toEpochMillisOrZero(),
    memo = memo,
)

fun Chapter.copyFrom(other: SourceChapter): Chapter = copy(
    name = other.name,
    url = other.url,
    dateUpload = other.dateUpload.toInstantOrNull(),
    chapterNumber = other.chapterNumber,
    scanlator = other.scanlator?.ifBlank { null }?.trim(),
    memo = other.memo,
)
