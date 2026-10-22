package com.nilemnod.mangareader.feature.reader.settings

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix

enum class ReadingMode {
    PagedLeftToRight,
    PagedRightToLeft,
    Webtoon,
}

enum class PageFit {
    Screen,
    Width,
    Height,
}

enum class ReaderBackground {
    Black,
    Gray,
    White,
    Automatic,
}

enum class ContrastPreset(
    val brightness: Float,
    val contrast: Float,
    val grayscale: Boolean,
) {
    Normal(1f, 1f, false),
    Text(1.05f, 1.3f, false),
    Strong(1.1f, 1.7f, false),
    Mono(1.1f, 1.7f, true),
}

@Immutable
data class ReaderSettings(
    val mode: ReadingMode = ReadingMode.PagedRightToLeft,
    val fit: PageFit = PageFit.Screen,
    val background: ReaderBackground = ReaderBackground.Black,
    val brightness: Float = 1f,
    val contrast: Float = 1f,
    val grayscale: Boolean = false,
    val invertColors: Boolean = false,
    val keepScreenOn: Boolean = true,
    val showPageNumber: Boolean = true,
    val invertTapZones: Boolean = false,
    val doubleTapZoom: Boolean = true,
    val doubleTapScale: Float = 2.5f,
    val panelZoom: Boolean = true,
    val panelNavigation: Boolean = false,
    val cropBorders: Boolean = false,
    val splitSpreads: Boolean = false,
    val eInkMode: Boolean = false,
    val volumeKeys: Boolean = false,
    val invertVolumeKeys: Boolean = false,
) {
    companion object {
        const val MIN_BRIGHTNESS = 0.5f
        const val MAX_BRIGHTNESS = 1.5f
        const val MIN_CONTRAST = 0.5f
        const val MAX_CONTRAST = 2f
        const val MIN_DOUBLE_TAP_SCALE = 1.5f
        const val MAX_DOUBLE_TAP_SCALE = 5f
    }
}

fun ReaderSettings.withPreset(preset: ContrastPreset): ReaderSettings {
    return copy(brightness = preset.brightness, contrast = preset.contrast, grayscale = preset.grayscale)
}

fun ReaderSettings.matchingPreset(): ContrastPreset? {
    return ContrastPreset.entries.firstOrNull {
        it.brightness == brightness && it.contrast == contrast && it.grayscale == grayscale
    }
}

internal fun ReaderSettings.colorFilter(): ColorFilter? {
    if (brightness == 1f && contrast == 1f && !grayscale && !invertColors) return null
    var matrix = IDENTITY
    if (grayscale) matrix = compose(matrix, GRAYSCALE)
    if (invertColors) matrix = compose(matrix, INVERT)
    if (contrast != 1f || brightness != 1f) {
        val translate = 255f * ((1f - contrast) / 2f + (brightness - 1f))
        matrix = compose(
            matrix,
            floatArrayOf(
                contrast, 0f, 0f, 0f, translate,
                0f, contrast, 0f, 0f, translate,
                0f, 0f, contrast, 0f, translate,
                0f, 0f, 0f, 1f, 0f,
            ),
        )
    }
    return ColorFilter.colorMatrix(ColorMatrix(matrix))
}

private val IDENTITY = floatArrayOf(
    1f, 0f, 0f, 0f, 0f,
    0f, 1f, 0f, 0f, 0f,
    0f, 0f, 1f, 0f, 0f,
    0f, 0f, 0f, 1f, 0f,
)

private val GRAYSCALE = floatArrayOf(
    0.2126f, 0.7152f, 0.0722f, 0f, 0f,
    0.2126f, 0.7152f, 0.0722f, 0f, 0f,
    0.2126f, 0.7152f, 0.0722f, 0f, 0f,
    0f, 0f, 0f, 1f, 0f,
)

private val INVERT = floatArrayOf(
    -1f, 0f, 0f, 0f, 255f,
    0f, -1f, 0f, 0f, 255f,
    0f, 0f, -1f, 0f, 255f,
    0f, 0f, 0f, 1f, 0f,
)

private fun compose(first: FloatArray, then: FloatArray): FloatArray {
    val result = FloatArray(20)
    for (row in 0 until 4) {
        for (col in 0 until 5) {
            var sum = if (col == 4) then[row * 5 + 4] else 0f
            for (k in 0 until 4) {
                sum += then[row * 5 + k] * first[k * 5 + col]
            }
            result[row * 5 + col] = sum
        }
    }
    return result
}
