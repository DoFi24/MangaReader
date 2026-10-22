package com.nilemnod.mangareader.feature.reader.settings

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class ReaderSettingsTest {

    @Test
    fun `default settings match the normal preset`() {
        ReaderSettings().matchingPreset() shouldBe ContrastPreset.Normal
    }

    @Test
    fun `every preset is recognized after being applied`() {
        ContrastPreset.entries.forEach { preset ->
            ReaderSettings().withPreset(preset).matchingPreset() shouldBe preset
        }
    }

    @Test
    fun `applying a preset keeps unrelated settings`() {
        val base = ReaderSettings(mode = ReadingMode.Webtoon, invertColors = true, cropBorders = true)

        val result = base.withPreset(ContrastPreset.Mono)

        result.mode shouldBe ReadingMode.Webtoon
        result.invertColors shouldBe true
        result.cropBorders shouldBe true
        result.grayscale shouldBe true
    }

    @Test
    fun `manual adjustments leave no preset selected`() {
        ReaderSettings(brightness = 1.2f).matchingPreset().shouldBeNull()
        ReaderSettings().withPreset(ContrastPreset.Text).copy(contrast = 1.31f).matchingPreset().shouldBeNull()
    }

    @Test
    fun `presets differ from each other`() {
        ContrastPreset.entries.map { Triple(it.brightness, it.contrast, it.grayscale) }.toSet().size shouldBe
            ContrastPreset.entries.size
    }

    @Test
    fun `default color filter is skipped`() {
        ReaderSettings().colorFilter().shouldBeNull()
    }

    @Test
    fun `double tap scale range is sane`() {
        (ReaderSettings.MIN_DOUBLE_TAP_SCALE < ReaderSettings().doubleTapScale) shouldBe true
        (ReaderSettings().doubleTapScale < ReaderSettings.MAX_DOUBLE_TAP_SCALE) shouldBe true
    }
}
