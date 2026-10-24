package com.nilemnod.mangareader.feature.updates

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nilemnod.mangareader.core.mangaui.ChapterDownloadAction
import com.nilemnod.mangareader.core.mangaui.ChapterDownloadIndicator
import com.nilemnod.mangareader.core.mangaui.DotSeparatorText
import com.nilemnod.mangareader.core.mangaui.MangaCover
import com.nilemnod.mangareader.core.ui.components.ListGroupHeader
import com.nilemnod.mangareader.core.ui.components.material.padding
import com.nilemnod.mangareader.core.ui.components.relativeDateText
import com.nilemnod.mangareader.core.ui.i18n.stringResource
import com.nilemnod.mangareader.core.ui.util.relativeTimeSpanString
import com.nilemnod.mangareader.core.ui.util.selectedBackground
import com.nilemnod.mangareader.domain.download.model.Download
import com.nilemnod.mangareader.domain.updates.model.UpdatesWithRelations
import com.nilemnod.mangareader.i18n.MR
import com.nilemnod.mangareader.icons.custommaterialsymbols.CustomMaterialSymbols
import com.nilemnod.mangareader.icons.custommaterialsymbols.edgefilled.Bookmark
internal fun LazyListScope.updatesLastUpdatedItem(
    lastUpdated: Long,
) {
    item(key = "updates-lastUpdated") {
        UpdatesLastUpdatedHeader(
            lastUpdated = lastUpdated,
            modifier = Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null),
        )
    }
}

@Composable
internal fun UpdatesLastUpdatedHeader(
    lastUpdated: Long,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .padding(horizontal = MaterialTheme.padding.medium, vertical = MaterialTheme.padding.small),
    ) {
        Text(
            text = stringResource(MR.strings.updates_last_update_info, relativeTimeSpanString(lastUpdated)),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

internal fun LazyListScope.updatesUiItems(
    uiModels: List<UpdatesUiModel>,
    selectionMode: Boolean,
    onUpdateSelected: (UpdatesItem, Boolean, Boolean) -> Unit,
    onClickCover: (UpdatesItem) -> Unit,
    onClickUpdate: (UpdatesItem) -> Unit,
    onDownloadChapter: (List<UpdatesItem>, ChapterDownloadAction) -> Unit,
) {
    items(
        items = uiModels,
        contentType = {
            when (it) {
                is UpdatesUiModel.Header -> "header"
                is UpdatesUiModel.Item -> "item"
            }
        },
        key = {
            when (it) {
                is UpdatesUiModel.Header -> "updatesHeader-${it.hashCode()}"
                is UpdatesUiModel.Item -> "updates-${it.item.update.mangaId}-${it.item.update.chapterId}"
            }
        },
    ) { item ->
        when (item) {
            is UpdatesUiModel.Header -> {
                ListGroupHeader(
                    modifier = Modifier.animateItem(),
                    text = relativeDateText(item.date),
                )
            }
            is UpdatesUiModel.Item -> {
                val updatesItem = item.item
                UpdatesUiItem(
                    modifier = Modifier.animateItem(),
                    update = updatesItem.update,
                    selected = updatesItem.selected,
                    readProgress = updatesItem.update.lastPageRead
                        .takeIf { !updatesItem.update.read && it > 0L }
                        ?.let {
                            stringResource(
                                MR.strings.chapter_progress,
                                it + 1,
                            )
                        },
                    onLongClick = {
                        onUpdateSelected(updatesItem, !updatesItem.selected, true)
                    },
                    onClick = {
                        when {
                            selectionMode -> onUpdateSelected(updatesItem, !updatesItem.selected, false)
                            else -> onClickUpdate(updatesItem)
                        }
                    },
                    onClickCover = { onClickCover(updatesItem) }.takeIf { !selectionMode },
                    onDownloadChapter = { action: ChapterDownloadAction ->
                        onDownloadChapter(listOf(updatesItem), action)
                    }.takeIf { !selectionMode },
                    downloadStateProvider = updatesItem.downloadStateProvider,
                    downloadProgressProvider = updatesItem.downloadProgressProvider,
                )
            }
        }
    }
}

@Composable
private fun UpdatesUiItem(
    update: UpdatesWithRelations,
    selected: Boolean,
    readProgress: String?,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onClickCover: (() -> Unit)?,
    onDownloadChapter: ((ChapterDownloadAction) -> Unit)?,
    // Download Indicator
    downloadStateProvider: () -> Download.State,
    downloadProgressProvider: () -> Int,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current

    Row(
        modifier = modifier
            .selectedBackground(selected)
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    onLongClick()
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                },
            )
            .height(72.dp)
            .padding(start = MaterialTheme.padding.medium, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        MangaCover.Book(
            modifier = Modifier
                .size(width = 42.dp, height = 56.dp)
                .alpha(if (update.read) READ_COVER_ALPHA else 1f),
            data = update.coverData,
            onClick = onClickCover,
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = update.mangaTitle,
                    maxLines = 1,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (update.read) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(weight = 1f, fill = false),
                )
                if (!update.read) {
                    val unreadDescription = stringResource(MR.strings.unread)
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                            .semantics { contentDescription = unreadDescription },
                    )
                }
                if (update.bookmark) {
                    Icon(
                        imageVector = CustomMaterialSymbols.EdgeFilled.Bookmark,
                        contentDescription = stringResource(MR.strings.action_filter_bookmarked),
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = update.chapterName,
                    maxLines = 1,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(weight = 1f, fill = false),
                )
                if (readProgress != null) {
                    DotSeparatorText()
                    Text(
                        text = readProgress,
                        maxLines = 1,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        ChapterDownloadIndicator(
            enabled = onDownloadChapter != null,
            downloadStateProvider = downloadStateProvider,
            downloadProgressProvider = downloadProgressProvider,
            onClick = { onDownloadChapter?.invoke(it) },
        )
    }
}

private const val READ_COVER_ALPHA = 0.6f
