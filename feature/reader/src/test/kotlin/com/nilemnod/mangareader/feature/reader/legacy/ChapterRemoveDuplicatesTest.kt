package com.nilemnod.mangareader.feature.reader.legacy

import com.nilemnod.mangareader.domain.chapter.model.Chapter
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class ChapterRemoveDuplicatesTest {

    private fun chapter(id: Long, number: Double, scanlator: String? = null): Chapter {
        return Chapter.create().copy(id = id, chapterNumber = number, scanlator = scanlator)
    }

    @Test
    fun `keeps unique chapters untouched`() {
        val chapters = listOf(chapter(1, 1.0), chapter(2, 2.0), chapter(3, 3.0))

        chapters.removeDuplicates(chapters[0]).map { it.id } shouldContainExactly listOf(1L, 2L, 3L)
    }

    @Test
    fun `prefers the current chapter among duplicates`() {
        val current = chapter(2, 1.0, "B")
        val chapters = listOf(chapter(1, 1.0, "A"), current, chapter(3, 2.0, "A"))

        val result = chapters.removeDuplicates(current)

        result.first { it.chapterNumber == 1.0 }.id shouldBe 2L
    }

    @Test
    fun `prefers the scanlator of the current chapter`() {
        val current = chapter(10, 5.0, "B")
        val chapters = listOf(
            chapter(1, 1.0, "A"),
            chapter(2, 1.0, "B"),
            chapter(3, 2.0, "A"),
            chapter(4, 2.0, "B"),
        )

        val result = chapters.removeDuplicates(current)

        result.map { it.id } shouldContainExactly listOf(2L, 4L)
    }

    @Test
    fun `falls back to the first duplicate`() {
        val current = chapter(10, 5.0, "C")
        val chapters = listOf(chapter(1, 1.0, "A"), chapter(2, 1.0, "B"))

        chapters.removeDuplicates(current).map { it.id } shouldContainExactly listOf(1L)
    }
}
