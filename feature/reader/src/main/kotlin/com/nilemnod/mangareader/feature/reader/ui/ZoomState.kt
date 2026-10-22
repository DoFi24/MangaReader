package com.nilemnod.mangareader.feature.reader.ui

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.unit.IntSize
import kotlin.math.abs
import kotlin.math.min

@Stable
internal class ZoomState {

    var scale by mutableFloatStateOf(1f)
        private set

    var offset by mutableStateOf(Offset.Zero)
        private set

    var viewport by mutableStateOf(IntSize.Zero)

    fun reset() {
        scale = 1f
        offset = Offset.Zero
    }

    suspend fun zoomToRect(rect: Rect) {
        if (viewport.width == 0 || viewport.height == 0 || rect.width <= 0f || rect.height <= 0f) return
        val targetScale = (min(viewport.width / rect.width, viewport.height / rect.height) * PANEL_FILL)
            .coerceIn(MIN_SCALE, MAX_SCALE)
        val center = Offset(viewport.width / 2f, viewport.height / 2f)
        val maxX = viewport.width * (targetScale - 1f) / 2f
        val maxY = viewport.height * (targetScale - 1f) / 2f
        val desired = (center - rect.center) * targetScale
        val targetOffset = Offset(desired.x.coerceIn(-maxX, maxX), desired.y.coerceIn(-maxY, maxY))
        animateTo(targetScale, targetOffset)
    }

    suspend fun animateReset() = animateTo(MIN_SCALE, Offset.Zero)

    private suspend fun animateTo(targetScale: Float, targetOffset: Offset) {
        val startScale = scale
        val startOffset = offset
        animate(0f, 1f, animationSpec = tween(PANEL_ANIMATION_MS)) { fraction, _ ->
            scale = startScale + (targetScale - startScale) * fraction
            offset = startOffset + (targetOffset - startOffset) * fraction
        }
    }

    fun applyTransform(zoomChange: Float, pan: Offset, centroid: Offset): Boolean {
        if (viewport.width == 0 || viewport.height == 0) return false
        val newScale = (scale * zoomChange).coerceIn(MIN_SCALE, MAX_SCALE)
        val ratio = newScale / scale
        val center = Offset(viewport.width / 2f, viewport.height / 2f)
        val relative = centroid - center
        val desired = relative - (relative - offset) * ratio + pan
        val maxX = viewport.width * (newScale - 1f) / 2f
        val maxY = viewport.height * (newScale - 1f) / 2f
        val clamped = Offset(desired.x.coerceIn(-maxX, maxX), desired.y.coerceIn(-maxY, maxY))
        val moved = abs(clamped.x - offset.x) > 0.5f || abs(clamped.y - offset.y) > 0.5f || newScale != scale
        scale = newScale
        offset = clamped
        return moved
    }

    companion object {
        const val MIN_SCALE = 1f
        const val MAX_SCALE = 8f
        private const val PANEL_FILL = 0.95f
        private const val PANEL_ANIMATION_MS = 250
    }
}

internal fun Modifier.zoomGestures(
    zoom: ZoomState,
    doubleTapZoom: Boolean,
    onDoubleTap: (Offset) -> Unit,
    onTap: (Offset, IntSize) -> Unit,
    onLongPress: () -> Unit,
): Modifier = this
    .pointerInput(doubleTapZoom) {
        detectTapGestures(
            onTap = { onTap(it, size) },
            onLongPress = { onLongPress() },
            onDoubleTap = if (doubleTapZoom) onDoubleTap else null,
        )
    }
    .pointerInput(Unit) {
        awaitEachGesture {
            awaitFirstDown(requireUnconsumed = false)
            do {
                val event = awaitPointerEvent(PointerEventPass.Main)
                val multiTouch = event.changes.count { it.pressed } > 1
                if (multiTouch || zoom.scale > 1f) {
                    val zoomChange = event.calculateZoom()
                    val pan = event.calculatePan()
                    val centroid = event.calculateCentroid(useCurrent = false)
                    if (centroid != Offset.Unspecified) {
                        val moved = zoom.applyTransform(zoomChange, pan, centroid)
                        if (moved || multiTouch) {
                            event.changes.forEach { if (it.positionChanged()) it.consume() }
                        }
                    }
                }
            } while (event.changes.any { it.pressed })
        }
    }
