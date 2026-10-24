package com.nilemnod.mangareader.feature.library.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.nilemnod.mangareader.core.ui.components.Badge
import com.nilemnod.mangareader.core.ui.theme.MangaReaderPreviewTheme
import com.nilemnod.mangareader.icons.custommaterialsymbols.CustomMaterialSymbols
import com.nilemnod.mangareader.icons.custommaterialsymbols.edge.LocalSource
@Composable
internal fun DownloadsBadge(count: Int) {
    if (count > 0) {
        Badge(
            text = "$count",
            color = MaterialTheme.colorScheme.tertiaryContainer,
            textColor = MaterialTheme.colorScheme.tertiary,
        )
    }
}

@Composable
internal fun UnreadBadge(count: Long) {
    if (count > 0) {
        Badge(text = "$count")
    }
}

@Composable
internal fun LanguageBadge(
    isLocal: Boolean,
    sourceLanguage: String,
) {
    if (isLocal) {
        Badge(
            imageVector = CustomMaterialSymbols.Edge.LocalSource,
            color = MaterialTheme.colorScheme.secondaryContainer,
            iconColor = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    } else if (sourceLanguage.isNotEmpty()) {
        Badge(
            text = sourceLanguage.uppercase(),
            color = MaterialTheme.colorScheme.secondaryContainer,
            textColor = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}

@PreviewLightDark
@Composable
private fun BadgePreview() {
    MangaReaderPreviewTheme {
        Column {
            DownloadsBadge(count = 10)
            UnreadBadge(count = 10)
            LanguageBadge(isLocal = true, sourceLanguage = "EN")
            LanguageBadge(isLocal = false, sourceLanguage = "EN")
        }
    }
}
