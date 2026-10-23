package com.nilemnod.mangareader.feature.settings.more

import androidx.compose.runtime.Composable
import com.nilemnod.mangareader.core.ui.components.AppBar
import com.nilemnod.mangareader.core.ui.components.ScrollbarLazyColumn
import com.nilemnod.mangareader.core.ui.components.material.Scaffold
import com.nilemnod.mangareader.core.ui.components.preference.PreferenceGroupHeader
import com.nilemnod.mangareader.core.ui.components.preference.SwitchPreferenceWidget
import com.nilemnod.mangareader.core.ui.components.preference.TextPreferenceWidget
import com.nilemnod.mangareader.core.ui.i18n.pluralStringResource
import com.nilemnod.mangareader.core.ui.i18n.stringResource
import com.nilemnod.mangareader.feature.settings.more.DownloadQueueState
import com.nilemnod.mangareader.i18n.MR
import com.nilemnod.mangareader.icons.custommaterialsymbols.CustomMaterialSymbols
import com.nilemnod.mangareader.icons.custommaterialsymbols.edge.About
import com.nilemnod.mangareader.icons.custommaterialsymbols.edge.Category
import com.nilemnod.mangareader.icons.custommaterialsymbols.edge.Download
import com.nilemnod.mangareader.icons.custommaterialsymbols.edge.DownloadedOnly
import com.nilemnod.mangareader.icons.custommaterialsymbols.edge.Incognito
import com.nilemnod.mangareader.icons.custommaterialsymbols.edge.Settings
import com.nilemnod.mangareader.icons.custommaterialsymbols.edge.Statistics
import com.nilemnod.mangareader.icons.custommaterialsymbols.edge.Storage
@Composable
fun MoreScreen(
    downloadQueueStateProvider: () -> DownloadQueueState,
    downloadedOnly: Boolean,
    onDownloadedOnlyChange: (Boolean) -> Unit,
    incognitoMode: Boolean,
    onIncognitoModeChange: (Boolean) -> Unit,
    onClickDownloadQueue: () -> Unit,
    onClickCategories: () -> Unit,
    onClickStats: () -> Unit,
    onClickDataAndStorage: () -> Unit,
    onClickSettings: () -> Unit,
    onClickAbout: () -> Unit,
) {
    Scaffold(
        topBar = { scrollBehavior ->
            AppBar(
                title = stringResource(MR.strings.label_more),
                scrollBehavior = scrollBehavior,
            )
        },
    ) { contentPadding ->
        ScrollbarLazyColumn(contentPadding = contentPadding) {
            item {
                SwitchPreferenceWidget(
                    title = stringResource(MR.strings.pref_incognito_mode),
                    subtitle = stringResource(MR.strings.pref_incognito_mode_summary),
                    icon = CustomMaterialSymbols.Edge.Incognito,
                    checked = incognitoMode,
                    onCheckedChanged = onIncognitoModeChange,
                )
            }
            item {
                SwitchPreferenceWidget(
                    title = stringResource(MR.strings.label_downloaded_only),
                    subtitle = stringResource(MR.strings.downloaded_only_summary),
                    icon = CustomMaterialSymbols.Edge.DownloadedOnly,
                    checked = downloadedOnly,
                    onCheckedChanged = onDownloadedOnlyChange,
                )
            }

            item { PreferenceGroupHeader(title = stringResource(MR.strings.more_group_manage)) }

            item {
                val downloadQueueState = downloadQueueStateProvider()
                TextPreferenceWidget(
                    title = stringResource(MR.strings.label_download_queue),
                    subtitle = when (downloadQueueState) {
                        DownloadQueueState.Stopped -> null
                        is DownloadQueueState.Paused -> {
                            val pending = downloadQueueState.pending
                            if (pending == 0) {
                                stringResource(MR.strings.paused)
                            } else {
                                "${stringResource(MR.strings.paused)} • ${
                                    pluralStringResource(
                                        MR.plurals.download_queue_summary,
                                        count = pending,
                                        pending,
                                    )
                                }"
                            }
                        }
                        is DownloadQueueState.Downloading -> {
                            val pending = downloadQueueState.pending
                            pluralStringResource(MR.plurals.download_queue_summary, count = pending, pending)
                        }
                    },
                    icon = CustomMaterialSymbols.Edge.Download,
                    onPreferenceClick = onClickDownloadQueue,
                )
            }
            item {
                TextPreferenceWidget(
                    title = stringResource(MR.strings.categories),
                    icon = CustomMaterialSymbols.Edge.Category,
                    onPreferenceClick = onClickCategories,
                )
            }
            item {
                TextPreferenceWidget(
                    title = stringResource(MR.strings.label_stats),
                    icon = CustomMaterialSymbols.Edge.Statistics,
                    onPreferenceClick = onClickStats,
                )
            }
            item {
                TextPreferenceWidget(
                    title = stringResource(MR.strings.label_data_storage),
                    icon = CustomMaterialSymbols.Edge.Storage,
                    onPreferenceClick = onClickDataAndStorage,
                )
            }

            item { PreferenceGroupHeader(title = stringResource(MR.strings.more_group_app)) }

            item {
                TextPreferenceWidget(
                    title = stringResource(MR.strings.label_settings),
                    icon = CustomMaterialSymbols.Edge.Settings,
                    onPreferenceClick = onClickSettings,
                )
            }
            item {
                TextPreferenceWidget(
                    title = stringResource(MR.strings.pref_category_about),
                    icon = CustomMaterialSymbols.Edge.About,
                    onPreferenceClick = onClickAbout,
                )
            }
        }
    }
}
