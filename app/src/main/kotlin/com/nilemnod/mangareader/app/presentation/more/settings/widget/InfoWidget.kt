package com.nilemnod.mangareader.app.presentation.more.settings.widget

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.nilemnod.mangareader.core.ui.components.material.padding
import com.nilemnod.mangareader.core.ui.components.preference.PrefsHorizontalPadding
import com.nilemnod.mangareader.core.ui.i18n.stringResource
import com.nilemnod.mangareader.core.ui.theme.MangaReaderPreviewTheme
import com.nilemnod.mangareader.core.ui.util.secondaryItemAlpha
import com.nilemnod.mangareader.i18n.MR
import com.nilemnod.mangareader.icons.materialsymbols.MaterialSymbols
import com.nilemnod.mangareader.icons.materialsymbols.rounded.Info
@Composable
internal fun InfoWidget(text: String) {
    Column(
        modifier = Modifier
            .padding(
                horizontal = PrefsHorizontalPadding,
                vertical = MaterialTheme.padding.medium,
            )
            .secondaryItemAlpha(),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.padding.medium),
    ) {
        Icon(
            imageVector = MaterialSymbols.Rounded.Info,
            contentDescription = null,
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@PreviewLightDark
@Composable
private fun InfoWidgetPreview() {
    MangaReaderPreviewTheme {
        Surface {
            InfoWidget(text = stringResource(MR.strings.download_ahead_info))
        }
    }
}
