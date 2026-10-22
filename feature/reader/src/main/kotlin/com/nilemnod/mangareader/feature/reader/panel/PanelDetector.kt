package com.nilemnod.mangareader.feature.reader.panel

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import com.nilemnod.mangareader.feature.reader.image.pixelLuminance
import com.nilemnod.mangareader.feature.reader.model.PageSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.max

internal class PanelLayout(
    private val aspect: Float,
    val panels: List<Rect>,
) {

    fun panelAt(position: Offset, viewport: IntSize): Rect? {
        if (panels.isEmpty() || viewport.width == 0 || viewport.height == 0) return null
        val (origin, imageSize) = frame(viewport)
        val normalized = Offset(
            (position.x - origin.x) / imageSize.width,
            (position.y - origin.y) / imageSize.height,
        )
        val panel = panels.firstOrNull { it.contains(normalized) }
            ?: panels.minBy { distance(it, normalized) }
        return toViewport(panel, viewport)
    }

    fun toViewport(panel: Rect, viewport: IntSize): Rect {
        val (origin, imageSize) = frame(viewport)
        return Rect(
            left = origin.x + panel.left * imageSize.width,
            top = origin.y + panel.top * imageSize.height,
            right = origin.x + panel.right * imageSize.width,
            bottom = origin.y + panel.bottom * imageSize.height,
        )
    }

    fun readingOrder(rightToLeft: Boolean): List<Rect> {
        val remaining = panels.sortedBy { it.top }.toMutableList()
        val ordered = mutableListOf<Rect>()
        while (remaining.isNotEmpty()) {
            val anchor = remaining.first()
            val row = remaining.filter { it.center.y < anchor.bottom }
            remaining.removeAll(row)
            ordered += if (rightToLeft) row.sortedByDescending { it.left } else row.sortedBy { it.left }
        }
        return ordered
    }

    private fun frame(viewport: IntSize): Pair<Offset, Size> {
        val viewportAspect = viewport.width.toFloat() / viewport.height
        val imageSize = if (viewportAspect > aspect) {
            Size(viewport.height * aspect, viewport.height.toFloat())
        } else {
            Size(viewport.width.toFloat(), viewport.width / aspect)
        }
        val origin = Offset((viewport.width - imageSize.width) / 2f, (viewport.height - imageSize.height) / 2f)
        return origin to imageSize
    }

    private fun distance(rect: Rect, point: Offset): Float {
        val dx = max(max(rect.left - point.x, point.x - rect.right), 0f)
        val dy = max(max(rect.top - point.y, point.y - rect.bottom), 0f)
        return dx * dx + dy * dy
    }
}

internal object PanelDetector {

    suspend fun detect(source: PageSource): PanelLayout? = withContext(Dispatchers.Default) {
        val bitmap = decode(source) ?: return@withContext null
        try {
            analyze(bitmap)
        } finally {
            bitmap.recycle()
        }
    }

    private suspend fun decode(source: PageSource): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        source.open().use { BitmapFactory.decodeStream(it.inputStream(), null, bounds) }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
        var sample = 1
        while (max(bounds.outWidth, bounds.outHeight) / (sample * 2) >= MAX_SIDE) sample *= 2
        val options = BitmapFactory.Options().apply { inSampleSize = sample }
        return source.open().use { BitmapFactory.decodeStream(it.inputStream(), null, options) }
    }

    private fun analyze(bitmap: Bitmap): PanelLayout? {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        return analyzeLuminance(IntArray(pixels.size) { pixelLuminance(pixels[it]) }, width, height)
    }

    internal fun analyzeLuminance(luminance: IntArray, width: Int, height: Int): PanelLayout? {
        if (width <= 0 || height <= 0 || luminance.size < width * height) return null
        val background = backgroundLuminance(luminance, width, height)
        val mask = Mask(width, height, BooleanArray(width * height) { abs(luminance[it] - background) > TOLERANCE })

        val rects = cut(mask, IntRect(0, 0, width, height), 0)
            .filter { it.width >= width * MIN_PANEL_FRACTION && it.height >= height * MIN_PANEL_FRACTION }
        if (rects.isEmpty()) return null

        val panels = rects.map {
            Rect(
                it.left.toFloat() / width,
                it.top.toFloat() / height,
                it.right.toFloat() / width,
                it.bottom.toFloat() / height,
            )
        }
        if (panels.size == 1 && panels[0].width * panels[0].height > MAX_SINGLE_PANEL_AREA) return null
        return PanelLayout(width.toFloat() / height, panels)
    }

    private fun backgroundLuminance(luminance: IntArray, width: Int, height: Int): Int {
        val histogram = IntArray(256)
        val ring = BORDER_RING
        for (y in 0 until height) {
            for (x in 0 until width) {
                if (x < ring || y < ring || x >= width - ring || y >= height - ring) {
                    histogram[luminance[y * width + x]]++
                }
            }
        }
        val total = histogram.sum()
        var seen = 0
        for (value in 0..255) {
            seen += histogram[value]
            if (seen * 2 >= total) return value
        }
        return 255
    }

    private class Mask(val width: Int, val height: Int, val data: BooleanArray)

    private fun cut(mask: Mask, region: IntRect, depth: Int): List<IntRect> {
        if (depth < MAX_DEPTH) {
            for (rows in listOf(true, false)) {
                val runs = contentRuns(mask, region, rows)
                if (runs.size > 1) return runs.flatMap { cut(mask, slice(region, it, rows), depth + 1) }
                val run = runs.singleOrNull() ?: continue
                val full = if (rows) region.top until region.bottom else region.left until region.right
                if (run != full) return cut(mask, slice(region, run, rows), depth + 1)
            }
        }
        return listOf(region)
    }

    private fun slice(region: IntRect, run: IntRange, rows: Boolean): IntRect {
        return if (rows) {
            IntRect(region.left, run.first, region.right, run.last + 1)
        } else {
            IntRect(run.first, region.top, run.last + 1, region.bottom)
        }
    }

    private fun contentRuns(mask: Mask, region: IntRect, rows: Boolean): List<IntRange> {
        val start = if (rows) region.top else region.left
        val end = if (rows) region.bottom else region.right
        val cross = if (rows) region.width else region.height
        val limit = cross * GUTTER_FILL

        val runs = mutableListOf<IntRange>()
        var runStart = -1
        var lastContent = -1
        var gap = 0
        for (i in start until end) {
            var count = 0
            if (rows) {
                for (x in region.left until region.right) if (mask.data[i * mask.width + x]) count++
            } else {
                for (y in region.top until region.bottom) if (mask.data[y * mask.width + i]) count++
            }
            if (count > limit) {
                if (runStart < 0) runStart = i
                lastContent = i
                gap = 0
            } else if (runStart >= 0) {
                gap++
                if (gap >= MIN_GAP) {
                    runs += runStart..lastContent
                    runStart = -1
                    gap = 0
                }
            }
        }
        if (runStart >= 0) runs += runStart..lastContent
        return runs.filter { it.last - it.first + 1 >= MIN_SIZE }
    }

    private const val MAX_SIDE = 800
    private const val BORDER_RING = 2
    private const val TOLERANCE = 32
    private const val GUTTER_FILL = 0.01f
    private const val MIN_GAP = 3
    private const val MIN_SIZE = 6
    private const val MAX_DEPTH = 6
    private const val MIN_PANEL_FRACTION = 0.05f
    private const val MAX_SINGLE_PANEL_AREA = 0.85f
}
