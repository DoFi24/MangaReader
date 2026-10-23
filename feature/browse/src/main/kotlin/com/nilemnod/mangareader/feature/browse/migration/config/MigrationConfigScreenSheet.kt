package com.nilemnod.mangareader.feature.browse.migration.config

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.util.fastForEach
import com.nilemnod.mangareader.core.common.preference.Preference
import com.nilemnod.mangareader.core.common.preference.getAndSet
import com.nilemnod.mangareader.core.common.preference.toggle
import com.nilemnod.mangareader.core.ui.components.AdaptiveSheet
import com.nilemnod.mangareader.core.ui.components.material.Button
import com.nilemnod.mangareader.core.ui.components.material.padding
import com.nilemnod.mangareader.core.ui.i18n.stringResource
import com.nilemnod.mangareader.core.ui.theme.active
import com.nilemnod.mangareader.core.ui.theme.header
import com.nilemnod.mangareader.core.ui.util.collectAsState
import com.nilemnod.mangareader.core.ui.util.fastFilterNot
import com.nilemnod.mangareader.domain.migration.models.MigrationFlag
import com.nilemnod.mangareader.domain.source.service.SourcePreferences
import com.nilemnod.mangareader.feature.browse.migration.utils.getLabel
import com.nilemnod.mangareader.i18n.MR
import com.nilemnod.mangareader.icons.materialsymbols.MaterialSymbols
import com.nilemnod.mangareader.icons.materialsymbols.rounded.Check
import com.nilemnod.mangareader.icons.materialsymbols.roundedfilled.Warning
@Composable
fun MigrationConfigScreenSheet(
    preferences: SourcePreferences,
    onDismissRequest: () -> Unit,
    onStartMigration: (extraSearchQuery: String?) -> Unit,
) {
    var extraSearchQuery by rememberSaveable { mutableStateOf("") }
    val migrationFlags by preferences.migrationFlags.collectAsState()
    AdaptiveSheet(onDismissRequest = onDismissRequest) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(top = MaterialTheme.padding.medium),
            ) {
                Text(
                    text = stringResource(MR.strings.migrationConfigScreen_dataToMigrateHeader),
                    style = MaterialTheme.typography.header,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = MaterialTheme.padding.extraSmall)
                        .padding(horizontal = MaterialTheme.padding.medium),
                )
                Spacer(modifier = Modifier.height(MaterialTheme.padding.extraSmall))
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = MaterialTheme.padding.medium)
                        .padding(bottom = MaterialTheme.padding.extraSmall),
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.padding.small),
                ) {
                    MigrationFlag.entries
                        .fastFilterNot { it == MigrationFlag.REMOVE_DOWNLOAD }
                        .fastForEach { flag ->
                            val selected = flag in migrationFlags
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    preferences.migrationFlags.getAndSet { currentFlags ->
                                        if (flag in currentFlags) {
                                            currentFlags - flag
                                        } else {
                                            currentFlags + flag
                                        }
                                    }
                                },
                                label = { Text(stringResource(flag.getLabel())) },
                                leadingIcon = {
                                    if (selected) {
                                        Icon(
                                            imageVector = MaterialSymbols.Rounded.Check,
                                            contentDescription = null,
                                        )
                                    }
                                },
                            )
                        }
                }
                val removeDownloads = MigrationFlag.REMOVE_DOWNLOAD in migrationFlags
                MigrationSheetSwitchItem(
                    title = stringResource(MR.strings.migrationConfigScreen_removeDownloadsTitle),
                    subtitle = null,
                    checked = removeDownloads,
                    onClick = {
                        preferences.migrationFlags.getAndSet {
                            if (removeDownloads) {
                                it - MigrationFlag.REMOVE_DOWNLOAD
                            } else {
                                it + MigrationFlag.REMOVE_DOWNLOAD
                            }
                        }
                    },
                )
                MigrationSheetDividerItem()
                OutlinedTextField(
                    value = extraSearchQuery,
                    onValueChange = { extraSearchQuery = it },
                    label = { Text(stringResource(MR.strings.migrationConfigScreen_additionalSearchQueryLabel)) },
                    supportingText = {
                        Text(stringResource(MR.strings.migrationConfigScreen_additionalSearchQuerySupportingText))
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = MaterialTheme.padding.medium,
                            vertical = MaterialTheme.padding.extraSmall,
                        ),
                )
                MigrationSheetSwitchItem(
                    title = stringResource(MR.strings.migrationConfigScreen_hideUnmatchedTitle),
                    subtitle = null,
                    preference = preferences.migrationHideUnmatched,
                )
                MigrationSheetSwitchItem(
                    title = stringResource(MR.strings.migrationConfigScreen_hideWithoutUpdatesTitle),
                    subtitle = stringResource(MR.strings.migrationConfigScreen_hideWithoutUpdatesSubtitle),
                    preference = preferences.migrationHideWithoutUpdates,
                )
                MigrationSheetDividerItem()
                MigrationSheetWarningItem(stringResource(MR.strings.migrationConfigScreen_enhancedOptionsWarning))
                MigrationSheetSwitchItem(
                    title = stringResource(MR.strings.migrationConfigScreen_deepSearchModeTitle),
                    subtitle = stringResource(MR.strings.migrationConfigScreen_deepSearchModeSubtitle),
                    preference = preferences.migrationDeepSearchMode,
                )
                MigrationSheetSwitchItem(
                    title = stringResource(MR.strings.migrationConfigScreen_prioritizeByChaptersTitle),
                    subtitle = stringResource(MR.strings.migrationConfigScreen_prioritizeByChaptersSubtitle),
                    preference = preferences.migrationPrioritizeByChapters,
                )
            }
            HorizontalDivider()
            Button(
                onClick = {
                    val cleanedExtraSearchQuery = extraSearchQuery.trim().ifBlank { null }
                    onStartMigration(cleanedExtraSearchQuery)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = MaterialTheme.padding.medium,
                        vertical = MaterialTheme.padding.small,
                    ),
            ) {
                Text(text = stringResource(MR.strings.migrationConfigScreen_continueButtonText))
            }
        }
    }
}

@Composable
private fun MigrationSheetSwitchItem(
    title: String,
    subtitle: String?,
    preference: Preference<Boolean>,
) {
    MigrationSheetSwitchItem(
        title = title,
        subtitle = subtitle,
        checked = preference.collectAsState().value,
        onClick = { preference.toggle() },
    )
}

@Composable
private fun MigrationSheetSwitchItem(
    title: String,
    subtitle: String?,
    checked: Boolean,
    onClick: () -> Unit,
) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = null,
            )
        },
        supportingContent = subtitle?.let { { Text(text = subtitle) } },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        content = { Text(text = title) },
    )
}

@Composable
private fun MigrationSheetDividerItem() {
    HorizontalDivider(modifier = Modifier.padding(vertical = MaterialTheme.padding.extraSmall))
}

@Composable
private fun MigrationSheetWarningItem(
    text: String,
) {
    ListItem(
        leadingContent = {
            Icon(
                imageVector = MaterialSymbols.RoundedFilled.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.active,
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier,
        )
    }
}
