package com.nilemnod.mangareader.core.ui.components.preference

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.nilemnod.mangareader.core.ui.theme.MangaReaderPreviewTheme
import com.nilemnod.mangareader.core.ui.util.secondaryItemAlpha
import com.nilemnod.mangareader.icons.materialsymbols.MaterialSymbols
import com.nilemnod.mangareader.icons.materialsymbols.rounded.Preview
@Composable
fun TextPreferenceWidget(
    modifier: Modifier = Modifier,
    title: String? = null,
    subtitle: String? = null,
    icon: ImageVector? = null,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    widget: @Composable (() -> Unit)? = null,
    onPreferenceClick: (() -> Unit)? = null,
) {
    BasePreferenceWidget(
        modifier = modifier,
        title = title,
        subcomponent = if (!subtitle.isNullOrBlank()) {
            {
                Text(
                    text = subtitle,
                    modifier = Modifier
                        .padding(horizontal = PrefsHorizontalPadding)
                        .secondaryItemAlpha(),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 10,
                )
            }
        } else {
            null
        },
        icon = if (icon != null) {
            {
                Icon(
                    imageVector = icon,
                    tint = iconTint,
                    contentDescription = null,
                )
            }
        } else {
            null
        },
        onClick = onPreferenceClick,
        widget = widget,
    )
}

@PreviewLightDark
@Composable
private fun TextPreferenceWidgetPreview() {
    MangaReaderPreviewTheme {
        Surface {
            Column {
                TextPreferenceWidget(
                    title = "Text preference with icon",
                    subtitle = "Text preference summary",
                    icon = MaterialSymbols.Rounded.Preview,
                    onPreferenceClick = {},
                )
                TextPreferenceWidget(
                    title = "Text preference",
                    subtitle = "Text preference summary",
                    onPreferenceClick = {},
                )
            }
        }
    }
}
