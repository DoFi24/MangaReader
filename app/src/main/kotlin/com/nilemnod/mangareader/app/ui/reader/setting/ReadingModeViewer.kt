package com.nilemnod.mangareader.app.ui.reader.setting

import com.nilemnod.mangareader.app.di.appGraph
import com.nilemnod.mangareader.app.ui.reader.ReaderActivity
import com.nilemnod.mangareader.app.ui.reader.viewer.Viewer
import com.nilemnod.mangareader.app.ui.reader.viewer.pager.L2RPagerViewer
import com.nilemnod.mangareader.app.ui.reader.viewer.pager.R2LPagerViewer
import com.nilemnod.mangareader.app.ui.reader.viewer.pager.VerticalPagerViewer
import com.nilemnod.mangareader.app.ui.reader.viewer.webgpu.WebGpuViewer
import com.nilemnod.mangareader.app.ui.reader.viewer.webgpu.WebGpuViewerContinuous
import com.nilemnod.mangareader.app.ui.reader.viewer.webtoon.WebtoonViewer
import com.nilemnod.mangareader.core.reader.ReadingMode

fun ReadingMode.Companion.toViewer(preference: Int?, activity: ReaderActivity): Viewer {
    if (activity.appGraph.basePreferences.highQualityRenderer.get()) {
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
