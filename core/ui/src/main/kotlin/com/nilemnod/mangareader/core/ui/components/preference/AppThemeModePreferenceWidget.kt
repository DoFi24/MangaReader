package com.nilemnod.mangareader.core.ui.components.preference

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.nilemnod.mangareader.core.ui.components.preference.BasePreferenceWidget
import com.nilemnod.mangareader.core.ui.components.preference.PrefsHorizontalPadding
import com.nilemnod.mangareader.core.ui.i18n.stringResource
import com.nilemnod.mangareader.core.ui.preference.model.ThemeMode
import com.nilemnod.mangareader.i18n.MR
private val options = mapOf(
    ThemeMode.LIGHT to MR.strings.theme_light,
    ThemeMode.DARK to MR.strings.theme_dark,
    ThemeMode.SYSTEM to MR.strings.theme_system,
)

@Composable
fun AppThemeModePreferenceWidget(
    value: ThemeMode,
    onItemClick: (ThemeMode) -> Unit,
) {
    BasePreferenceWidget(
        subcomponent = {
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PrefsHorizontalPadding),
            ) {
                options.onEachIndexed { index, (mode, labelRes) ->
                    SegmentedButton(
                        selected = mode == value,
                        onClick = { onItemClick(mode) },
                        shape = SegmentedButtonDefaults.itemShape(
                            index,
                            options.size,
                        ),
                    ) {
                        Text(stringResource(labelRes))
                    }
                }
            }
        },
    )
}
