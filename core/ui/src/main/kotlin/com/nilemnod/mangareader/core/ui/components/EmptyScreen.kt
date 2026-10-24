package com.nilemnod.mangareader.core.ui.components

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.nilemnod.mangareader.core.ui.screens.EmptyScreen
import com.nilemnod.mangareader.core.ui.screens.EmptyScreenAction
import com.nilemnod.mangareader.core.ui.theme.MangaReaderPreviewTheme
import com.nilemnod.mangareader.i18n.MR
import com.nilemnod.mangareader.icons.custommaterialsymbols.CustomMaterialSymbols
import com.nilemnod.mangareader.icons.custommaterialsymbols.edge.Refresh
import com.nilemnod.mangareader.icons.materialsymbols.MaterialSymbols
import com.nilemnod.mangareader.icons.materialsymbols.automirroredrounded.Help
@PreviewLightDark
@Composable
private fun NoActionPreview() {
    MangaReaderPreviewTheme {
        Surface {
            EmptyScreen(
                stringRes = MR.strings.empty_screen,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun WithActionPreview() {
    MangaReaderPreviewTheme {
        Surface {
            EmptyScreen(
                stringRes = MR.strings.empty_screen,
                actions = listOf(
                    EmptyScreenAction(
                        stringRes = MR.strings.action_retry,
                        icon = CustomMaterialSymbols.Edge.Refresh,
                        onClick = {},
                    ),
                    EmptyScreenAction(
                        stringRes = MR.strings.getting_started_guide,
                        icon = MaterialSymbols.AutoMirroredRounded.Help,
                        onClick = {},
                    ),
                ),
            )
        }
    }
}
