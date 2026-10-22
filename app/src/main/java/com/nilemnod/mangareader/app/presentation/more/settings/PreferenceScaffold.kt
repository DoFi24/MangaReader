package com.nilemnod.mangareader.app.presentation.more.settings

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import com.nilemnod.mangareader.core.ui.components.AppBar
import com.nilemnod.mangareader.core.ui.components.material.Scaffold
import com.nilemnod.mangareader.core.ui.i18n.stringResource
import dev.icerock.moko.resources.StringResource

@Composable
fun PreferenceScaffold(
    titleRes: StringResource,
    actions: @Composable RowScope.() -> Unit = {},
    onBackPressed: (() -> Unit)? = null,
    itemsProvider: @Composable () -> List<Preference>,
) {
    Scaffold(
        topBar = {
            AppBar(
                title = stringResource(titleRes),
                navigateUp = onBackPressed,
                actions = actions,
                scrollBehavior = it,
            )
        },
        content = { contentPadding ->
            PreferenceScreen(
                items = itemsProvider(),
                contentPadding = contentPadding,
            )
        },
    )
}
