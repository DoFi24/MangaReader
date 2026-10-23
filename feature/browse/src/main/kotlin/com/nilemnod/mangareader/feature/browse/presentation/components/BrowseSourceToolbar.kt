package com.nilemnod.mangareader.feature.browse.presentation.components

import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.nilemnod.mangareader.core.ui.components.AppBar
import com.nilemnod.mangareader.core.ui.components.AppBarActions
import com.nilemnod.mangareader.core.ui.components.AppBarTitle
import com.nilemnod.mangareader.core.ui.components.DropdownMenu
import com.nilemnod.mangareader.core.ui.components.RadioMenuItem
import com.nilemnod.mangareader.core.ui.components.SearchToolbar
import com.nilemnod.mangareader.core.ui.i18n.stringResource
import com.nilemnod.mangareader.domain.library.model.LibraryDisplayMode
import com.nilemnod.mangareader.domain.source.api.ContentSource
import com.nilemnod.mangareader.i18n.MR
import com.nilemnod.mangareader.icons.materialsymbols.MaterialSymbols
import com.nilemnod.mangareader.icons.materialsymbols.automirroredroundedfilled.ViewList
import com.nilemnod.mangareader.icons.materialsymbols.roundedfilled.ViewCompact
import com.nilemnod.mangareader.icons.materialsymbols.roundedfilled.ViewModule
import com.nilemnod.mangareader.source.local.LocalSource
import com.nilemnod.mangareader.source.local.isLocal

@Composable
fun BrowseSourceToolbar(
    searchQuery: String?,
    onSearchQueryChange: (String?) -> Unit,
    source: ContentSource?,
    displayMode: LibraryDisplayMode,
    onDisplayModeChange: (LibraryDisplayMode) -> Unit,
    navigateUp: () -> Unit,
    onWebViewClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onSearch: (String) -> Unit,
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    // Avoid capturing unstable source in actions lambda
    val title = source?.name
    val isLocalSource = source?.isLocal() == true
    val isConfigurableSource = source?.hasSettings == true

    var selectingDisplayMode by remember { mutableStateOf(false) }

    SearchToolbar(
        navigateUp = navigateUp,
        titleContent = { AppBarTitle(title) },
        searchQuery = searchQuery,
        onChangeSearchQuery = onSearchQueryChange,
        onSearch = onSearch,
        onClickCloseSearch = navigateUp,
        actions = {
            AppBarActions(
                actions = buildList {
                    add(
                        AppBar.Action(
                            title = stringResource(MR.strings.action_display_mode),
                            icon = when (displayMode) {
                                LibraryDisplayMode.List -> MaterialSymbols.AutoMirroredRoundedFilled.ViewList
                                LibraryDisplayMode.ComfortableGrid -> MaterialSymbols.RoundedFilled.ViewModule
                                LibraryDisplayMode.CompactGrid,
                                LibraryDisplayMode.CoverOnlyGrid,
                                -> MaterialSymbols.RoundedFilled.ViewCompact
                            },
                            onClick = { selectingDisplayMode = true },
                        ),
                    )
                    if (!isLocalSource) {
                        add(
                            AppBar.OverflowAction(
                                title = stringResource(MR.strings.action_open_in_web_view),
                                onClick = onWebViewClick,
                            ),
                        )
                    }
                    if (isConfigurableSource) {
                        add(
                            AppBar.OverflowAction(
                                title = stringResource(MR.strings.action_settings),
                                onClick = onSettingsClick,
                            ),
                        )
                    }
                },
            )

            DropdownMenu(
                expanded = selectingDisplayMode,
                onDismissRequest = { selectingDisplayMode = false },
            ) {
                RadioMenuItem(
                    text = { Text(text = stringResource(MR.strings.action_display_comfortable_grid)) },
                    isChecked = displayMode == LibraryDisplayMode.ComfortableGrid,
                ) {
                    selectingDisplayMode = false
                    onDisplayModeChange(LibraryDisplayMode.ComfortableGrid)
                }
                RadioMenuItem(
                    text = { Text(text = stringResource(MR.strings.action_display_grid)) },
                    isChecked = displayMode == LibraryDisplayMode.CompactGrid,
                ) {
                    selectingDisplayMode = false
                    onDisplayModeChange(LibraryDisplayMode.CompactGrid)
                }
                RadioMenuItem(
                    text = { Text(text = stringResource(MR.strings.action_display_list)) },
                    isChecked = displayMode == LibraryDisplayMode.List,
                ) {
                    selectingDisplayMode = false
                    onDisplayModeChange(LibraryDisplayMode.List)
                }
            }
        },
        scrollBehavior = scrollBehavior,
    )
}
