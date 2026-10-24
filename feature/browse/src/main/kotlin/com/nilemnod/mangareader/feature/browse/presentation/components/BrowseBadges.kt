package com.nilemnod.mangareader.feature.browse.presentation.components

import androidx.compose.runtime.Composable
import com.nilemnod.mangareader.core.ui.components.Badge
import com.nilemnod.mangareader.icons.custommaterialsymbols.CustomMaterialSymbols
import com.nilemnod.mangareader.icons.custommaterialsymbols.edge.Library
@Composable
internal fun InLibraryBadge(enabled: Boolean) {
    if (enabled) {
        Badge(
            imageVector = CustomMaterialSymbols.Edge.Library,
        )
    }
}
