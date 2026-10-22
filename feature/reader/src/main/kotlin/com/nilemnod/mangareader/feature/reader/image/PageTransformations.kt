package com.nilemnod.mangareader.feature.reader.image

import android.graphics.Bitmap
import androidx.compose.ui.unit.IntRect
import coil3.size.Size
import coil3.transform.Transformation
import kotlin.math.abs

internal fun pixelLuminance(color: Int): Int {
    val r = (color shr 16) and 0xFF
    val g = (color shr 8) and 0xFF
    val b = color and 0xFF
    return (r * 299 + g * 587 + b * 114) / 1000
}

internal class CropBordersTransformation : Transformation() {

    override val cacheKey: String = "crop_borders_v1"

    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        val width = input.width
        val height = input.height
        val bounds = findContentBounds(width, height) { y, row -> input.getPixels(row, 0, width, 0, y, width, 1) }
            ?: return input
        return Bitmap.createBitmap(input, bounds.left, bounds.top, bounds.width, bounds.height)
    }
}

internal fun findContentBounds(width: Int, height: Int, readRow: (y: Int, row: IntArray) -> Unit): IntRect? {
    if (width < CROP_MIN_SIDE || height < CROP_MIN_SIDE) return null

    val row = IntArray(width)
    val rowCounts = IntArray(height)
    val columnCounts = IntArray(width)

    readRow(0, row)
    val topLeft = pixelLuminance(row[0])
    val topRight = pixelLuminance(row[width - 1])
    readRow(height - 1, row)
    val bottomLeft = pixelLuminance(row[0])
    val bottomRight = pixelLuminance(row[width - 1])
    val corners = intArrayOf(topLeft, topRight, bottomLeft, bottomRight)
    if (corners.max() - corners.min() > CROP_TOLERANCE) return null
    val background = corners.average().toInt()

    for (y in 0 until height) {
        readRow(y, row)
        var count = 0
        for (x in 0 until width) {
            if (abs(pixelLuminance(row[x]) - background) > CROP_TOLERANCE) {
                count++
                columnCounts[x]++
            }
        }
        rowCounts[y] = count
    }

    val rowLimit = width * CROP_NOISE_FRACTION
    val columnLimit = height * CROP_NOISE_FRACTION
    val top = rowCounts.indexOfFirst { it > rowLimit }
    val bottom = rowCounts.indexOfLast { it > rowLimit }
    val left = columnCounts.indexOfFirst { it > columnLimit }
    val right = columnCounts.indexOfLast { it > columnLimit }
    if (top < 0 || left < 0 || bottom <= top || right <= left) return null

    val cropWidth = right - left + 1
    val cropHeight = bottom - top + 1
    if (cropWidth * cropHeight < width.toLong() * height * CROP_MIN_KEPT_AREA) return null
    if (cropWidth == width && cropHeight == height) return null
    return IntRect(left, top, right + 1, bottom + 1)
}

private const val CROP_MIN_SIDE = 16
private const val CROP_TOLERANCE = 32
private const val CROP_NOISE_FRACTION = 0.005f
private const val CROP_MIN_KEPT_AREA = 0.3f

internal class HalfTransformation(private val left: Boolean) : Transformation() {

    override val cacheKey: String = if (left) "half_left_v1" else "half_right_v1"

    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        val half = input.width / 2
        if (half <= 0) return input
        val x = if (left) 0 else input.width - half
        return Bitmap.createBitmap(input, x, 0, half, input.height)
    }
}
