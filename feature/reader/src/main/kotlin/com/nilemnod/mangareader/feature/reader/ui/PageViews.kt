package com.nilemnod.mangareader.feature.reader.ui

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import coil3.request.transformations
import coil3.size.Size
import coil3.transform.Transformation
import com.nilemnod.mangareader.core.ui.i18n.stringResource
import com.nilemnod.mangareader.feature.reader.image.CropBordersTransformation
import com.nilemnod.mangareader.feature.reader.image.HalfTransformation
import com.nilemnod.mangareader.feature.reader.model.ReaderPage
import com.nilemnod.mangareader.feature.reader.settings.PageFit
import com.nilemnod.mangareader.i18n.MR
import kotlinx.coroutines.launch

internal enum class PageHalf { Left, Right }

@Composable
internal fun PageImage(
    page: ReaderPage,
    imageLoader: ImageLoader,
    contentScale: ContentScale,
    colorFilter: ColorFilter?,
    targetSize: Size?,
    modifier: Modifier = Modifier,
    cropBorders: Boolean = false,
    half: PageHalf? = null,
    onWide: (() -> Unit)? = null,
) {
    var attempt by remember(page.source.key) { mutableIntStateOf(0) }
    key(attempt) {
        val context = LocalContext.current
        val request = remember(page.source, targetSize, cropBorders, half) {
            val transformations = buildList<Transformation> {
                if (cropBorders) add(CropBordersTransformation())
                half?.let { add(HalfTransformation(it == PageHalf.Left)) }
            }
            ImageRequest.Builder(context)
                .data(page.source)
                .apply {
                    if (targetSize != null) size(targetSize)
                    if (transformations.isNotEmpty()) transformations(transformations)
                }
                .build()
        }
        SubcomposeAsyncImage(
            model = request,
            contentDescription = null,
            imageLoader = imageLoader,
            modifier = modifier,
            contentScale = contentScale,
            colorFilter = colorFilter,
            onSuccess = { state ->
                val image = state.result.image
                if (half == null && onWide != null && image.width > image.height) onWide()
            },
            loading = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 240.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            },
            error = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 240.dp)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = stringResource(MR.strings.transition_pages_error),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Button(onClick = { attempt++ }) {
                        Text(stringResource(MR.strings.action_retry))
                    }
                }
            },
        )
    }
}

@Composable
internal fun ZoomablePage(
    page: ReaderPage,
    doubleTapZoom: Boolean,
    doubleTapScale: Float,
    panelZoom: Boolean,
    cropBorders: Boolean,
    half: PageHalf?,
    onWide: (() -> Unit)?,
    imageLoader: ImageLoader,
    colorFilter: ColorFilter?,
    onTap: (Offset, IntSize) -> Unit,
    onLongPress: () -> Unit,
    sessions: MutableMap<String, PanelSession>,
    sessionKey: String,
    modifier: Modifier = Modifier,
) {
    val zoom = remember(page.source.key) { ZoomState() }
    val viewport = zoom.viewport
    val scope = rememberCoroutineScope()
    val session = remember(page.source.key) { PanelSession(page.source, zoom) }
    DisposableEffect(sessions, sessionKey, session) {
        sessions[sessionKey] = session
        onDispose { if (sessions[sessionKey] === session) sessions.remove(sessionKey) }
    }
    val currentPanelZoom by rememberUpdatedState(panelZoom)
    val currentScale by rememberUpdatedState(doubleTapScale)
    val onDoubleTap: (Offset) -> Unit = { position ->
        if (zoom.scale > 1f) {
            zoom.reset()
        } else {
            scope.launch {
                val zoomed = currentPanelZoom && session.zoomToPanelAt(position)
                if (!zoomed) zoom.applyTransform(currentScale, Offset.Zero, position)
            }
        }
    }
    val targetSize = remember(viewport, half) {
        if (viewport.width == 0 || viewport.height == 0) {
            null
        } else {
            val factor = if (half == null) RESOLUTION_FACTOR else RESOLUTION_FACTOR * 2
            Size(viewport.width * factor, viewport.height * factor)
        }
    }
    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { zoom.viewport = it }
            .zoomGestures(zoom, doubleTapZoom, onDoubleTap, onTap, onLongPress)
            .clipToBounds(),
        contentAlignment = Alignment.Center,
    ) {
        PageImage(
            page = page,
            imageLoader = imageLoader,
            contentScale = ContentScale.Fit,
            colorFilter = colorFilter,
            targetSize = targetSize,
            cropBorders = cropBorders,
            half = half,
            onWide = onWide,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = zoom.scale
                    scaleY = zoom.scale
                    translationX = zoom.offset.x
                    translationY = zoom.offset.y
                },
        )
    }
}

@Composable
internal fun ScrollingPage(
    page: ReaderPage,
    fit: PageFit,
    imageLoader: ImageLoader,
    colorFilter: ColorFilter?,
    cropBorders: Boolean,
    half: PageHalf?,
    onWide: (() -> Unit)?,
    onTap: (Offset, IntSize) -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val vertical = fit == PageFit.Width
    val scrollState = rememberScrollState()
    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { onTap(it, size) }, onLongPress = { onLongPress() })
            }
            .then(
                if (vertical) {
                    Modifier.verticalScroll(scrollState)
                } else {
                    Modifier.horizontalScroll(scrollState)
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        PageImage(
            page = page,
            imageLoader = imageLoader,
            contentScale = if (vertical) ContentScale.FillWidth else ContentScale.FillHeight,
            colorFilter = colorFilter,
            targetSize = null,
            cropBorders = cropBorders,
            half = half,
            onWide = onWide,
            modifier = if (vertical) Modifier.fillMaxWidth() else Modifier.fillMaxHeight(),
        )
    }
}

private const val RESOLUTION_FACTOR = 2
