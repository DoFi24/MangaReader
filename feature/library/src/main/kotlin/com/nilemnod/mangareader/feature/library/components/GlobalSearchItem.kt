package com.nilemnod.mangareader.feature.library.components

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import com.nilemnod.mangareader.core.ui.i18n.stringResource
import com.nilemnod.mangareader.i18n.MR
@Composable
internal fun GlobalSearchItem(
    searchQuery: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TextButton(
        modifier = modifier,
        onClick = onClick,
    ) {
        Text(
            text = stringResource(MR.strings.action_global_search_query, searchQuery),
            modifier = Modifier.zIndex(99f),
        )
    }
}
