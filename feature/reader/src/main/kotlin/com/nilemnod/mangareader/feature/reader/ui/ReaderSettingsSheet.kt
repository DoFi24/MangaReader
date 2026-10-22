package com.nilemnod.mangareader.feature.reader.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nilemnod.mangareader.core.ui.i18n.stringResource
import com.nilemnod.mangareader.feature.reader.R
import com.nilemnod.mangareader.feature.reader.settings.ContrastPreset
import com.nilemnod.mangareader.feature.reader.settings.PageFit
import com.nilemnod.mangareader.feature.reader.settings.ReaderBackground
import com.nilemnod.mangareader.feature.reader.settings.ReaderSettings
import com.nilemnod.mangareader.feature.reader.settings.ReadingMode
import com.nilemnod.mangareader.feature.reader.settings.matchingPreset
import com.nilemnod.mangareader.feature.reader.settings.withPreset
import com.nilemnod.mangareader.i18n.MR
import dev.icerock.moko.resources.StringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReaderSettingsSheet(
    settings: ReaderSettings,
    onUpdate: ((ReaderSettings) -> ReaderSettings) -> Unit,
    onModeChange: (ReadingMode) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        scrimColor = Color.Transparent,
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SettingsGroup(stringResource(MR.strings.pref_viewer_type)) {
                ChoiceRow(
                    options = listOf(
                        ReadingMode.PagedLeftToRight to MR.strings.left_to_right_viewer,
                        ReadingMode.PagedRightToLeft to MR.strings.right_to_left_viewer,
                        ReadingMode.Webtoon to MR.strings.webtoon_viewer,
                    ),
                    selected = settings.mode,
                    onSelect = onModeChange,
                )
            }
            SettingsGroup(stringResource(MR.strings.pref_image_scale_type)) {
                ChoiceRow(
                    options = listOf(
                        PageFit.Screen to MR.strings.scale_type_fit_screen,
                        PageFit.Width to MR.strings.scale_type_fit_width,
                        PageFit.Height to MR.strings.scale_type_fit_height,
                    ),
                    selected = settings.fit,
                    onSelect = { value -> onUpdate { it.copy(fit = value) } },
                )
            }
            SettingsGroup(stringResource(MR.strings.pref_reader_theme)) {
                ChoiceRow(
                    options = listOf(
                        ReaderBackground.White to MR.strings.white_background,
                        ReaderBackground.Gray to MR.strings.gray_background,
                        ReaderBackground.Black to MR.strings.black_background,
                        ReaderBackground.Automatic to MR.strings.automatic_background,
                    ),
                    selected = settings.background,
                    onSelect = { value -> onUpdate { it.copy(background = value) } },
                )
            }
            SettingsGroup(androidx.compose.ui.res.stringResource(R.string.reader_text_boost)) {
                PresetRow(
                    options = listOf(
                        ContrastPreset.Normal to R.string.reader_preset_normal,
                        ContrastPreset.Text to R.string.reader_preset_text,
                        ContrastPreset.Strong to R.string.reader_preset_strong,
                        ContrastPreset.Mono to R.string.reader_preset_mono,
                    ),
                    selected = settings.matchingPreset(),
                    onSelect = { preset -> onUpdate { it.withPreset(preset) } },
                )
            }
            SettingsGroup(stringResource(MR.strings.pref_custom_brightness)) {
                Slider(
                    value = settings.brightness,
                    onValueChange = { value -> onUpdate { it.copy(brightness = value) } },
                    valueRange = ReaderSettings.MIN_BRIGHTNESS..ReaderSettings.MAX_BRIGHTNESS,
                )
            }
            SettingsGroup(androidx.compose.ui.res.stringResource(R.string.reader_contrast)) {
                Slider(
                    value = settings.contrast,
                    onValueChange = { value -> onUpdate { it.copy(contrast = value) } },
                    valueRange = ReaderSettings.MIN_CONTRAST..ReaderSettings.MAX_CONTRAST,
                )
            }
            if (settings.mode != ReadingMode.Webtoon && settings.fit == PageFit.Screen) {
                SwitchRow(
                    label = androidx.compose.ui.res.stringResource(R.string.reader_panel_navigation),
                    checked = settings.panelNavigation,
                    onCheckedChange = { value -> onUpdate { it.copy(panelNavigation = value) } },
                )
            }
            SwitchRow(
                label = androidx.compose.ui.res.stringResource(R.string.reader_crop_borders),
                checked = settings.cropBorders,
                onCheckedChange = { value -> onUpdate { it.copy(cropBorders = value) } },
            )
            if (settings.mode != ReadingMode.Webtoon) {
                SwitchRow(
                    label = androidx.compose.ui.res.stringResource(R.string.reader_split_spreads),
                    checked = settings.splitSpreads,
                    onCheckedChange = { value -> onUpdate { it.copy(splitSpreads = value) } },
                )
            }
            SwitchRow(
                label = androidx.compose.ui.res.stringResource(R.string.reader_eink_mode),
                checked = settings.eInkMode,
                onCheckedChange = { value -> onUpdate { it.copy(eInkMode = value) } },
            )
            SwitchRow(
                label = stringResource(MR.strings.pref_grayscale),
                checked = settings.grayscale,
                onCheckedChange = { value -> onUpdate { it.copy(grayscale = value) } },
            )
            SwitchRow(
                label = stringResource(MR.strings.pref_inverted_colors),
                checked = settings.invertColors,
                onCheckedChange = { value -> onUpdate { it.copy(invertColors = value) } },
            )
            SwitchRow(
                label = stringResource(MR.strings.pref_double_tap_zoom),
                checked = settings.doubleTapZoom,
                onCheckedChange = { value -> onUpdate { it.copy(doubleTapZoom = value) } },
            )
            if (settings.doubleTapZoom) {
                SwitchRow(
                    label = androidx.compose.ui.res.stringResource(R.string.reader_panel_zoom),
                    checked = settings.panelZoom,
                    onCheckedChange = { value -> onUpdate { it.copy(panelZoom = value) } },
                )
                SettingsGroup(androidx.compose.ui.res.stringResource(R.string.reader_double_tap_scale)) {
                    Slider(
                        value = settings.doubleTapScale,
                        onValueChange = { value -> onUpdate { it.copy(doubleTapScale = value) } },
                        valueRange = ReaderSettings.MIN_DOUBLE_TAP_SCALE..ReaderSettings.MAX_DOUBLE_TAP_SCALE,
                    )
                }
            }
            SwitchRow(
                label = stringResource(MR.strings.pref_show_page_number),
                checked = settings.showPageNumber,
                onCheckedChange = { value -> onUpdate { it.copy(showPageNumber = value) } },
            )
            SwitchRow(
                label = stringResource(MR.strings.pref_read_with_tapping_inverted),
                checked = settings.invertTapZones,
                onCheckedChange = { value -> onUpdate { it.copy(invertTapZones = value) } },
            )
            SwitchRow(
                label = stringResource(MR.strings.pref_read_with_volume_keys),
                checked = settings.volumeKeys,
                onCheckedChange = { value -> onUpdate { it.copy(volumeKeys = value) } },
            )
            SwitchRow(
                label = stringResource(MR.strings.pref_read_with_volume_keys_inverted),
                checked = settings.invertVolumeKeys,
                onCheckedChange = { value -> onUpdate { it.copy(invertVolumeKeys = value) } },
            )
            SwitchRow(
                label = stringResource(MR.strings.pref_keep_screen_on),
                checked = settings.keepScreenOn,
                onCheckedChange = { value -> onUpdate { it.copy(keepScreenOn = value) } },
            )
        }
    }
}

@Composable
private fun SettingsGroup(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
        )
        content()
    }
}

@Composable
internal fun <T> ChoiceRow(
    options: List<Pair<T, StringResource>>,
    selected: T,
    onSelect: (T) -> Unit,
) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, (value, label) ->
            SegmentedButton(
                selected = value == selected,
                onClick = { onSelect(value) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
            ) {
                Text(
                    text = stringResource(label),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}

@Composable
private fun <T> PresetRow(
    options: List<Pair<T, Int>>,
    selected: T?,
    onSelect: (T) -> Unit,
) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, (value, label) ->
            SegmentedButton(
                selected = value == selected,
                onClick = { onSelect(value) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
            ) {
                Text(
                    text = androidx.compose.ui.res.stringResource(label),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}

@Composable
private fun SwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
