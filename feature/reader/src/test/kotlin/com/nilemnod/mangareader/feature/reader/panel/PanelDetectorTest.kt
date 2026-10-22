package com.nilemnod.mangareader.feature.reader.panel

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.IntSize
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.floats.plusOrMinus
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class PanelDetectorTest {

    private val width = 400
    private val height = 600

    private fun page(background: Int, panelColor: Int, panels: List<IntArray>): IntArray {
        val pixels = IntArray(width * height) { background }
        panels.forEach { (left, top, right, bottom) ->
            for (y in top until bottom) {
                for (x in left until right) pixels[y * width + x] = panelColor
            }
        }
        return pixels
    }

    private val grid = listOf(
        intArrayOf(20, 20, 190, 290),
        intArrayOf(210, 20, 380, 290),
        intArrayOf(20, 310, 190, 580),
        intArrayOf(210, 310, 380, 580),
    )

    @Test
    fun `finds four panels in a two by two grid`() {
        val layout = PanelDetector.analyzeLuminance(page(255, 40, grid), width, height)

        layout.shouldNotBeNull()
        layout.panels shouldHaveSize 4
        val first = layout.panels.minWith(compareBy({ it.top }, { it.left }))
        first.left shouldBe (20f / width).plusOrMinus(0.02f)
        first.top shouldBe (20f / height).plusOrMinus(0.02f)
        first.right shouldBe (190f / width).plusOrMinus(0.02f)
        first.bottom shouldBe (290f / height).plusOrMinus(0.02f)
    }

    @Test
    fun `finds panels on a dark background`() {
        val layout = PanelDetector.analyzeLuminance(page(0, 210, grid), width, height)

        layout.shouldNotBeNull()
        layout.panels shouldHaveSize 4
    }

    @Test
    fun `keeps a single inset panel`() {
        val layout = PanelDetector.analyzeLuminance(
            page(255, 40, listOf(intArrayOf(80, 120, 320, 480))),
            width,
            height,
        )

        layout.shouldNotBeNull()
        layout.panels shouldHaveSize 1
    }

    @Test
    fun `returns nothing for a blank page`() {
        PanelDetector.analyzeLuminance(IntArray(width * height) { 255 }, width, height).shouldBeNull()
    }

    @Test
    fun `returns nothing for a page without gutters`() {
        val pixels = IntArray(width * height) { 110 + (it % 7) }

        PanelDetector.analyzeLuminance(pixels, width, height).shouldBeNull()
    }

    @Test
    fun `returns nothing for inconsistent dimensions`() {
        PanelDetector.analyzeLuminance(IntArray(10), width, height).shouldBeNull()
        PanelDetector.analyzeLuminance(IntArray(0), 0, 0).shouldBeNull()
    }

    private val sideBySide = PanelLayout(
        aspect = 1f,
        panels = listOf(Rect(0f, 0f, 0.5f, 1f), Rect(0.5f, 0f, 1f, 1f)),
    )

    @Test
    fun `maps the tapped panel into viewport coordinates`() {
        val panel = sideBySide.panelAt(Offset(300f, 100f), IntSize(1000, 500))

        panel shouldBe Rect(250f, 0f, 500f, 500f)
    }

    @Test
    fun `picks the nearest panel when the tap lands outside the image`() {
        val panel = sideBySide.panelAt(Offset(900f, 100f), IntSize(1000, 500))

        panel shouldBe Rect(500f, 0f, 750f, 500f)
    }

    @Test
    fun `fits the image by width in a tall viewport`() {
        val panel = sideBySide.panelAt(Offset(100f, 500f), IntSize(500, 1000))

        panel shouldBe Rect(0f, 250f, 250f, 750f)
    }

    @Test
    fun `returns nothing without panels or viewport`() {
        PanelLayout(1f, emptyList()).panelAt(Offset.Zero, IntSize(100, 100)).shouldBeNull()
        sideBySide.panelAt(Offset.Zero, IntSize.Zero).shouldBeNull()
    }

    private val grid2x2 = PanelLayout(
        aspect = 1f,
        panels = listOf(
            Rect(0.5f, 0.5f, 1f, 1f),
            Rect(0f, 0f, 0.5f, 0.5f),
            Rect(0.5f, 0f, 1f, 0.5f),
            Rect(0f, 0.5f, 0.5f, 1f),
        ),
    )

    @Test
    fun `reads panels left to right then top to bottom`() {
        grid2x2.readingOrder(rightToLeft = false) shouldBe listOf(
            Rect(0f, 0f, 0.5f, 0.5f),
            Rect(0.5f, 0f, 1f, 0.5f),
            Rect(0f, 0.5f, 0.5f, 1f),
            Rect(0.5f, 0.5f, 1f, 1f),
        )
    }

    @Test
    fun `reads panels right to left then top to bottom`() {
        grid2x2.readingOrder(rightToLeft = true) shouldBe listOf(
            Rect(0.5f, 0f, 1f, 0.5f),
            Rect(0f, 0f, 0.5f, 0.5f),
            Rect(0.5f, 0.5f, 1f, 1f),
            Rect(0f, 0.5f, 0.5f, 1f),
        )
    }

    @Test
    fun `keeps a tall panel in the row where it starts`() {
        val layout = PanelLayout(
            aspect = 1f,
            panels = listOf(
                Rect(0f, 0f, 0.5f, 1f),
                Rect(0.5f, 0f, 1f, 0.45f),
                Rect(0.5f, 0.55f, 1f, 1f),
            ),
        )

        layout.readingOrder(rightToLeft = false) shouldBe listOf(
            Rect(0f, 0f, 0.5f, 1f),
            Rect(0.5f, 0f, 1f, 0.45f),
            Rect(0.5f, 0.55f, 1f, 1f),
        )
    }

    @Test
    fun `has an empty reading order without panels`() {
        PanelLayout(1f, emptyList()).readingOrder(rightToLeft = false) shouldBe emptyList()
    }
}
