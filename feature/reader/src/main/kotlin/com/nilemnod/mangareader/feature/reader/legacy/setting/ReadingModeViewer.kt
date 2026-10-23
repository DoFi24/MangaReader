package com.nilemnod.mangareader.feature.reader.legacy.setting

import com.nilemnod.mangareader.core.metro.metroGraph
import com.nilemnod.mangareader.core.reader.ReadingMode
import com.nilemnod.mangareader.feature.reader.legacy.ReaderActivity
import com.nilemnod.mangareader.feature.reader.legacy.di.ReaderGraph
import com.nilemnod.mangareader.feature.reader.legacy.viewer.Viewer
import com.nilemnod.mangareader.feature.reader.legacy.viewer.pager.L2RPagerViewer
import com.nilemnod.mangareader.feature.reader.legacy.viewer.pager.R2LPagerViewer
import com.nilemnod.mangareader.feature.reader.legacy.viewer.pager.VerticalPagerViewer
import com.nilemnod.mangareader.feature.reader.legacy.viewer.webgpu.WebGpuViewer
import com.nilemnod.mangareader.feature.reader.legacy.viewer.webgpu.WebGpuViewerContinuous
import com.nilemnod.mangareader.feature.reader.legacy.viewer.webtoon.WebtoonViewer

fun ReadingMode.Companion.toViewer(preference: Int?, activity: ReaderActivity): Viewer {
    if (activity.metroGraph<ReaderGraph>().readerPreferences.highQualityRenderer.get()) {
        return when (fromPreference(preference)) {
            ReadingMode.LEFT_TO_RIGHT -> WebGpuViewer(activity, isReversed = false, isVertical = false)
            ReadingMode.RIGHT_TO_LEFT -> WebGpuViewer(activity, isReversed = true, isVertical = false)
            ReadingMode.VERTICAL -> WebGpuViewer(activity, isReversed = false, isVertical = true)
            ReadingMode.WEBTOON -> WebGpuViewerContinuous(activity, useGap = false)
            ReadingMode.CONTINUOUS_VERTICAL -> WebGpuViewerContinuous(activity, useGap = true)
            ReadingMode.DEFAULT -> throw IllegalStateException("Preference value must be resolved: $preference")
        }
    }
    return when (fromPreference(preference)) {
        ReadingMode.LEFT_TO_RIGHT -> L2RPagerViewer(activity)
        ReadingMode.RIGHT_TO_LEFT -> R2LPagerViewer(activity)
        ReadingMode.VERTICAL -> VerticalPagerViewer(activity)
        ReadingMode.WEBTOON -> WebtoonViewer(activity)
        ReadingMode.CONTINUOUS_VERTICAL -> WebtoonViewer(activity, isContinuous = false)
        ReadingMode.DEFAULT -> throw IllegalStateException("Preference value must be resolved: $preference")
    }
}
