package com.nilemnod.mangareader.compat.tachiyomi

import com.nilemnod.mangareader.domain.source.api.MangaStatus
import com.nilemnod.mangareader.domain.source.api.SourceChapter
import com.nilemnod.mangareader.domain.source.api.SourceManga
import com.nilemnod.mangareader.domain.source.api.UpdateStrategy
import eu.kanade.tachiyomi.source.model.SManga
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class ModelMappingTest {

    @Test
    fun `manga survives a round trip through the native model`() {
        val manga = SourceManga(
            url = "/manga/1",
            title = "Title",
            author = "Author",
            artist = "Artist",
            description = "Description",
            genres = listOf("Action", "Drama"),
            status = MangaStatus.COMPLETED,
            thumbnailUrl = "https://example.org/cover.jpg",
            updateStrategy = UpdateStrategy.ONLY_FETCH_ONCE,
            initialized = true,
        )

        manga.toNative().toSource() shouldBe manga
    }

    @Test
    fun `genres are split from the native comma separated string`() {
        val native = SManga.create().also {
            it.title = "Title"
            it.url = "/1"
            it.genre = "Action, Drama, Action"
        }

        native.toSource().genres shouldBe listOf("Action", "Drama")
    }

    @Test
    fun `unknown status code falls back to unknown`() {
        MangaStatus.fromCode(42) shouldBe MangaStatus.UNKNOWN
    }

    @Test
    fun `chapter survives a round trip through the native model`() {
        val chapter = SourceChapter(
            url = "/chapter/1",
            name = "Chapter 1",
            chapterNumber = 1.5,
            scanlator = "Group",
            dateUpload = 1_700_000_000_000,
        )

        chapter.toNative().toSource() shouldBe chapter
    }
}
