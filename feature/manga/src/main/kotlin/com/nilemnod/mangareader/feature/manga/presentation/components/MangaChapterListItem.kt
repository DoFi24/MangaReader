package com.nilemnod.mangareader.feature.manga.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nilemnod.mangareader.core.mangaui.ChapterDownloadAction
import com.nilemnod.mangareader.core.mangaui.ChapterDownloadIndicator
import com.nilemnod.mangareader.core.mangaui.DotSeparatorText
import com.nilemnod.mangareader.core.ui.i18n.stringResource
import com.nilemnod.mangareader.core.ui.util.selectedBackground
import com.nilemnod.mangareader.domain.download.model.Download
import com.nilemnod.mangareader.domain.library.service.LibraryPreferences
import com.nilemnod.mangareader.i18n.MR
import com.nilemnod.mangareader.icons.custommaterialsymbols.CustomMaterialSymbols
import com.nilemnod.mangareader.icons.custommaterialsymbols.edge.Bookmark
import com.nilemnod.mangareader.icons.custommaterialsymbols.edge.Close
import com.nilemnod.mangareader.icons.custommaterialsymbols.edge.Delete
import com.nilemnod.mangareader.icons.custommaterialsymbols.edge.Download
import com.nilemnod.mangareader.icons.custommaterialsymbols.edge.MarkRead
import com.nilemnod.mangareader.icons.custommaterialsymbols.edge.MarkUnread
import com.nilemnod.mangareader.icons.custommaterialsymbols.edgefilled.Bookmark
import com.nilemnod.mangareader.icons.custommaterialsymbols.edgefilled.Downloaded
import me.saket.swipe.SwipeableActionsBox

@Composable
fun MangaChapterListItem(
    title: String,
    date: String?,
    readProgress: String?,
    scanlator: String?,
    read: Boolean,
    bookmark: Boolean,
    selected: Boolean,
    selectionMode: Boolean,
    downloadIndicatorEnabled: Boolean,
    downloadStateProvider: () -> Download.State,
    downloadProgressProvider: () -> Int,
    chapterSwipeStartAction: LibraryPreferences.ChapterSwipeAction,
    chapterSwipeEndAction: LibraryPreferences.ChapterSwipeAction,
    onLongClick: () -> Unit,
    onClick: () -> Unit,
    onDownloadClick: ((ChapterDownloadAction) -> Unit)?,
    onChapterSwipe: (LibraryPreferences.ChapterSwipeAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val unreadDescription = stringResource(MR.strings.unread)
    val start = getSwipeAction(
        action = chapterSwipeStartAction,
        read = read,
        bookmark = bookmark,
        downloadState = downloadStateProvider(),
        background = MaterialTheme.colorScheme.primaryContainer,
        onSwipe = { onChapterSwipe(chapterSwipeStartAction) },
    )
    val end = getSwipeAction(
        action = chapterSwipeEndAction,
        read = read,
        bookmark = bookmark,
        downloadState = downloadStateProvider(),
        background = MaterialTheme.colorScheme.primaryContainer,
        onSwipe = { onChapterSwipe(chapterSwipeEndAction) },
    )

    SwipeableActionsBox(
        modifier = Modifier.clipToBounds(),
        startActions = listOfNotNull(start),
        endActions = listOfNotNull(end),
        swipeThreshold = swipeActionThreshold,
        backgroundUntilSwipeThreshold = MaterialTheme.colorScheme.surfaceContainerLowest,
    ) {
        Row(
            modifier = modifier
                .selectedBackground(selected)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick,
                )
                .heightIn(min = 72.dp)
                .padding(start = 16.dp, top = 8.dp, end = 4.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier.size(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                when {
                    selected -> Icon(
                        imageVector = CustomMaterialSymbols.EdgeFilled.Downloaded,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    selectionMode -> Box(
                        modifier = Modifier
                            .size(20.dp)
                            .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape),
                    )
                    !read -> Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                            .semantics { contentDescription = unreadDescription },
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = title,
                        modifier = Modifier.weight(1f, fill = false),
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = if (read) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                    )
                    if (bookmark) {
                        Icon(
                            imageVector = CustomMaterialSymbols.EdgeFilled.Bookmark,
                            contentDescription = stringResource(MR.strings.action_filter_bookmarked),
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }

                Row {
                    val subtitleStyle = MaterialTheme.typography.bodyMedium
                        .merge(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    ProvideTextStyle(value = subtitleStyle) {
                        if (date != null) {
                            Text(
                                text = date,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            if (readProgress != null || scanlator != null) DotSeparatorText()
                        }
                        if (readProgress != null) {
                            Text(
                                text = readProgress,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            if (scanlator != null) DotSeparatorText()
                        }
                        if (scanlator != null) {
                            Text(
                                text = scanlator,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }

            ChapterDownloadIndicator(
                enabled = downloadIndicatorEnabled,
                downloadStateProvider = downloadStateProvider,
                downloadProgressProvider = downloadProgressProvider,
                onClick = { onDownloadClick?.invoke(it) },
            )
        }
    }
}

private fun getSwipeAction(
    action: LibraryPreferences.ChapterSwipeAction,
    read: Boolean,
    bookmark: Boolean,
    downloadState: Download.State,
    background: Color,
    onSwipe: () -> Unit,
): me.saket.swipe.SwipeAction? {
    return when (action) {
        LibraryPreferences.ChapterSwipeAction.ToggleRead -> swipeAction(
            icon = if (!read) CustomMaterialSymbols.Edge.MarkRead else CustomMaterialSymbols.Edge.MarkUnread,
            background = background,
            isUndo = read,
            onSwipe = onSwipe,
        )
        LibraryPreferences.ChapterSwipeAction.ToggleBookmark -> swipeAction(
            icon = if (!bookmark) CustomMaterialSymbols.Edge.Bookmark else CustomMaterialSymbols.EdgeFilled.Bookmark,
            background = background,
            isUndo = bookmark,
            onSwipe = onSwipe,
        )
        LibraryPreferences.ChapterSwipeAction.Download -> swipeAction(
            icon = when (downloadState) {
                Download.State.NOT_DOWNLOADED, Download.State.ERROR -> CustomMaterialSymbols.Edge.Download
                Download.State.QUEUE, Download.State.DOWNLOADING -> CustomMaterialSymbols.Edge.Close
                Download.State.DOWNLOADED -> CustomMaterialSymbols.Edge.Delete
            },
            background = background,
            onSwipe = onSwipe,
        )
        LibraryPreferences.ChapterSwipeAction.Disabled -> null
    }
}

private fun swipeAction(
    onSwipe: () -> Unit,
    icon: ImageVector,
    background: Color,
    isUndo: Boolean = false,
): me.saket.swipe.SwipeAction {
    return me.saket.swipe.SwipeAction(
        icon = {
            Icon(
                modifier = Modifier.padding(16.dp),
                imageVector = icon,
                tint = contentColorFor(background),
                contentDescription = null,
            )
        },
        background = background,
        onSwipe = onSwipe,
        isUndo = isUndo,
    )
}

private val swipeActionThreshold = 56.dp
