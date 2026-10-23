package com.nilemnod.mangareader.domain.migration.service

import com.nilemnod.mangareader.domain.manga.model.Manga

interface MangaMigrator {

    suspend operator fun invoke(current: Manga, target: Manga, replace: Boolean)
}
