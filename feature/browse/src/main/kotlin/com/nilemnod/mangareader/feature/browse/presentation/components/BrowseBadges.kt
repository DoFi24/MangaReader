package com.nilemnod.mangareader.feature.browse.presentation.components

import androidx.compose.runtime.Composable
import com.nilemnod.mangareader.core.ui.components.Badge
import com.nilemnod.mangareader.icons.materialsymbols.MaterialSymbols
import com.nilemnod.mangareader.icons.materialsymbols.rounded.CollectionsBookmark
@Composable
internal fun InLibraryBadge(enabled: Boolean) {
    if (enabled) {
        Badge(
            imageVector = MaterialSymbols.Rounded.CollectionsBookmark,
        )
    }
}
