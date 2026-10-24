package com.nilemnod.mangareader.core.ui.components.material

import androidx.annotation.IntRange
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SliderState
import androidx.compose.material3.rememberSliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun Slider(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: IntProgression = 0..1,
    @IntRange(from = 0) steps: Int = with(valueRange) { (last - first) - 1 },
    onValueChangeFinished: (() -> Unit)? = null,
    colors: SliderColors = SliderDefaults.colors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    thumb: @Composable (SliderState) -> Unit = { SliderThumb(enabled = enabled) },
    track: @Composable (SliderState) -> Unit = { sliderState ->
        SliderTrack(sliderState = sliderState, enabled = enabled)
    },
) {
    val state = key(steps, valueRange) {
        rememberSliderState(
            value = value.toFloat(),
            steps = steps,
            trackRange = with(valueRange) { first.toFloat()..last.toFloat() },
        )
    }
    state.value = value.toFloat()
    Slider(
        state = state,
        modifier = modifier,
        enabled = enabled,
        onValueChange = { onValueChange(it.roundToInt()) },
        onValueChangeFinished = onValueChangeFinished,
        colors = colors,
        interactionSource = interactionSource,
        thumb = thumb,
        track = track,
    )
}

@Composable
fun SliderThumb(enabled: Boolean = true) {
    Box(
        modifier = Modifier
            .size(20.dp)
            .background(
                if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                CircleShape,
            ),
    )
}

@Composable
fun SliderTrack(
    sliderState: SliderState,
    enabled: Boolean = true,
) {
    SliderDefaults.Track(
        sliderState = sliderState,
        modifier = Modifier.height(4.dp),
        enabled = enabled,
        colors = SliderDefaults.colors(
            activeTrackColor = MaterialTheme.colorScheme.primary,
            inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant,
        ),
        drawStopIndicator = null,
        drawTick = { _, _ -> },
        thumbTrackGapSize = 0.dp,
        trackInsideCornerSize = 2.dp,
    )
}
