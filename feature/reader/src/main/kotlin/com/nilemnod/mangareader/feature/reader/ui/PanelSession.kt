package com.nilemnod.mangareader.feature.reader.ui

import androidx.compose.ui.geometry.Offset
import com.nilemnod.mangareader.feature.reader.model.PageSource
import com.nilemnod.mangareader.feature.reader.panel.PanelDetector
import com.nilemnod.mangareader.feature.reader.panel.PanelLayout
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class PanelSession(
    private val source: PageSource,
    private val zoom: ZoomState,
) {

    private val mutex = Mutex()
    private var layout: PanelLayout? = null
    private var detected = false
    private var index = -1

    private suspend fun layout(): PanelLayout? = mutex.withLock {
        if (!detected) {
            layout = runCatching { PanelDetector.detect(source) }.getOrNull()
            detected = true
        }
        layout
    }

    suspend fun zoomToPanelAt(position: Offset): Boolean {
        val panel = layout()?.panelAt(position, zoom.viewport) ?: return false
        zoom.zoomToRect(panel)
        return true
    }

    suspend fun step(delta: Int, rightToLeft: Boolean): Boolean {
        if (zoom.scale <= RESET_SCALE) index = -1
        val layout = layout() ?: return false
        val ordered = layout.readingOrder(rightToLeft)
        if (ordered.isEmpty()) return false

        val target = index + delta
        return when {
            target in ordered.indices -> {
                index = target
                zoom.zoomToRect(layout.toViewport(ordered[target], zoom.viewport))
                true
            }
            target == -1 && index == 0 -> {
                index = -1
                zoom.animateReset()
                true
            }
            else -> false
        }
    }

    private companion object {
        const val RESET_SCALE = 1.01f
    }
}
