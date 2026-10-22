package com.nilemnod.mangareader.feature.reader.image

import androidx.compose.ui.unit.IntRect
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class CropBordersTest {

    private fun bounds(
        width: Int,
        height: Int,
        pixels: (x: Int, y: Int) -> Int,
    ): IntRect? {
        return findContentBounds(width, height) { y, row ->
            for (x in row.indices) row[x] = pixels(x, y)
        }
    }

    private val white = 0xFFFFFFFF.toInt()
    private val dark = 0xFF202020.toInt()

    @Test
    fun `crops white margins around the content`() {
        val result = bounds(100, 100) { x, y -> if (x in 20..79 && y in 30..89) dark else white }

        result shouldBe IntRect(20, 30, 80, 90)
    }

    @Test
    fun `crops dark margins around light content`() {
        val result = bounds(100, 100) { x, y -> if (x in 10..89 && y in 10..89) white else dark }

        result shouldBe IntRect(10, 10, 90, 90)
    }

    @Test
    fun `ignores a page that is already tight`() {
        bounds(100, 100) { _, _ -> dark }.shouldBeNull()
    }

    @Test
    fun `ignores a blank page`() {
        bounds(100, 100) { _, _ -> white }.shouldBeNull()
    }

    @Test
    fun `ignores pages with different corner colors`() {
        val result = bounds(100, 100) { x, y ->
            when {
                x < 50 && y < 50 -> white
                x >= 50 && y >= 50 -> dark
                else -> white
            }
        }

        result.shouldBeNull()
    }

    @Test
    fun `does not crop away most of the page`() {
        val result = bounds(100, 100) { x, y -> if (x in 45..54 && y in 45..54) dark else white }

        result.shouldBeNull()
    }

    @Test
    fun `ignores tiny images`() {
        bounds(8, 8) { _, _ -> white }.shouldBeNull()
    }

    @Test
    fun `ignores isolated noise pixels`() {
        val result = bounds(200, 200) { x, y ->
            when {
                x in 40..159 && y in 40..159 -> dark
                x == 5 && y == 5 -> dark
                else -> white
            }
        }

        result shouldBe IntRect(40, 40, 160, 160)
    }
}
