package com.nilemnod.mangareader.domain.chapter.service

import com.nilemnod.mangareader.domain.chapter.model.Chapter
import com.nilemnod.mangareader.domain.manga.model.Manga

interface NextUnreadChapterProvider {

    suspend fun get(manga: Manga): Chapter?
}
