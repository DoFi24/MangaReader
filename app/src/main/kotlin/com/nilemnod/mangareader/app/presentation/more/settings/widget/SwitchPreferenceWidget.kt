package com.nilemnod.mangareader.app.presentation.more.settings.widget

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.nilemnod.mangareader.core.ui.components.preference.TextPreferenceWidget
import com.nilemnod.mangareader.core.ui.components.preference.TrailingWidgetBuffer
import com.nilemnod.mangareader.core.ui.theme.MangaReaderPreviewTheme
import com.nilemnod.mangareader.icons.materialsymbols.MaterialSymbols
import com.nilemnod.mangareader.icons.materialsymbols.rounded.Preview

@Composable
fun SwitchPreferenceWidget(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    checked: Boolean = false,
    onCheckedChanged: (Boolean) -> Unit,
) {
    TextPreferenceWidget(
        modifier = modifier,
        title = title,
        subtitle = subtitle,
        icon = icon,
        widget = {
            Switch(
                checked = checked,
                onCheckedChange = null,
                modifier = Modifier.padding(start = TrailingWidgetBuffer),
            )
        },
        onPreferenceClick = { onCheckedChanged(!checked) },
    )
}

@PreviewLightDark
@Composable
private fun SwitchPreferenceWidgetPreview() {
    MangaReaderPreviewTheme {
        Surface {
            Column {
                SwitchPreferenceWidget(
                    title = "Text preference with icon",
                    subtitle = "Text preference summary",
                    icon = MaterialSymbols.Rounded.Preview,
                    checked = true,
                    onCheckedChanged = {},
                )
                SwitchPreferenceWidget(
                    title = "Text preference",
                    subtitle = "Text preference summary",
                    checked = false,
                    onCheckedChanged = {},
                )
                SwitchPreferenceWidget(
                    title = "Text preference no summary",
                    checked = false,
                    onCheckedChanged = {},
                )
                SwitchPreferenceWidget(
                    title = "Another text preference no summary",
                    checked = false,
                    onCheckedChanged = {},
                )
            }
        }
    }
}
