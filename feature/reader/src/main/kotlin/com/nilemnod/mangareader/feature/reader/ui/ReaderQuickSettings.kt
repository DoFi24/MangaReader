package com.nilemnod.mangareader.feature.reader.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nilemnod.mangareader.feature.reader.R
import com.nilemnod.mangareader.feature.reader.settings.ContrastPreset
import com.nilemnod.mangareader.feature.reader.settings.PageFit
import com.nilemnod.mangareader.feature.reader.settings.ReaderSettings
import com.nilemnod.mangareader.feature.reader.settings.ReadingMode
import com.nilemnod.mangareader.feature.reader.settings.matchingPreset
import com.nilemnod.mangareader.feature.reader.settings.withPreset
import com.nilemnod.mangareader.i18n.MR
import com.nilemnod.mangareader.presentation.core.i18n.stringResource

@Composable
internal fun ReaderQuickSettings(
    settings: ReaderSettings,
    onUpdate: ((ReaderSettings) -> ReaderSettings) -> Unit,
    onModeChange: (ReadingMode) -> Unit,
    onOpenAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val paged = settings.mode != ReadingMode.Webtoon
    val preset = settings.matchingPreset()
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ChoiceRow(
                options = listOf(
                    ReadingMode.PagedLeftToRight to MR.strings.left_to_right_viewer,
                    ReadingMode.PagedRightToLeft to MR.strings.right_to_left_viewer,
                    ReadingMode.Webtoon to MR.strings.webtoon_viewer,
                ),
                selected = settings.mode,
                onSelect = onModeChange,
            )
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AssistChip(
                    onClick = {
                        val next = ContrastPreset.entries[((preset?.ordinal ?: -1) + 1) % ContrastPreset.entries.size]
                        onUpdate { it.withPreset(next) }
                    },
                    label = {
                        Text(
                            androidx.compose.ui.res.stringResource(
                                when (preset) {
                                    ContrastPreset.Normal -> R.string.reader_preset_normal
                                    ContrastPreset.Text -> R.string.reader_preset_text
                                    ContrastPreset.Strong -> R.string.reader_preset_strong
                                    ContrastPreset.Mono -> R.string.reader_preset_mono
                                    null -> R.string.reader_contrast
                                },
                            ),
                        )
                    },
                )
                if (paged) {
                    AssistChip(
                        onClick = {
                            val next = PageFit.entries[(settings.fit.ordinal + 1) % PageFit.entries.size]
                            onUpdate { it.copy(fit = next) }
                        },
                        label = {
                            Text(
                                stringResource(
                                    when (settings.fit) {
                                        PageFit.Screen -> MR.strings.scale_type_fit_screen
                                        PageFit.Width -> MR.strings.scale_type_fit_width
                                        PageFit.Height -> MR.strings.scale_type_fit_height
                                    },
                                ),
                            )
                        },
                    )
                }
                if (paged && settings.fit == PageFit.Screen) {
                    FilterChip(
                        selected = settings.panelNavigation,
                        onClick = { onUpdate { it.copy(panelNavigation = !it.panelNavigation) } },
                        label = { Text(androidx.compose.ui.res.stringResource(R.string.reader_panel_navigation)) },
                    )
                }
                FilterChip(
                    selected = settings.cropBorders,
                    onClick = { onUpdate { it.copy(cropBorders = !it.cropBorders) } },
                    label = { Text(androidx.compose.ui.res.stringResource(R.string.reader_crop_borders)) },
                )
                AssistChip(
                    onClick = onOpenAll,
                    label = { Text(stringResource(MR.strings.action_settings)) },
                )
            }
        }
    }
}
