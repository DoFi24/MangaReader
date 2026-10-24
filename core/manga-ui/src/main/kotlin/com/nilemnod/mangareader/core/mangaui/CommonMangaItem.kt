package com.nilemnod.mangareader.core.mangaui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nilemnod.mangareader.core.ui.components.BadgeGroup
import com.nilemnod.mangareader.core.ui.i18n.stringResource
import com.nilemnod.mangareader.core.ui.util.selectedBackground
import com.nilemnod.mangareader.i18n.MR
import com.nilemnod.mangareader.icons.custommaterialsymbols.CustomMaterialSymbols
import com.nilemnod.mangareader.icons.custommaterialsymbols.edgefilled.Downloaded
import com.nilemnod.mangareader.icons.custommaterialsymbols.edgefilled.NextChapter
import com.nilemnod.mangareader.domain.manga.model.MangaCover as MangaCoverModel

object CommonMangaItemDefaults {
    val GridHorizontalSpacer = 8.dp
    val GridVerticalSpacer = 8.dp

    @Suppress("ConstPropertyName")
    const val BrowseFavoriteCoverAlpha = 0.5f
}

private val ContinueReadingButtonSizeSmall = 28.dp
private val ContinueReadingButtonSizeLarge = 32.dp

private val ContinueReadingButtonIconSizeSmall = 16.dp
private val ContinueReadingButtonIconSizeLarge = 20.dp

private val ContinueReadingButtonGridPadding = 6.dp
private val ContinueReadingButtonListSpacing = 8.dp

private val SelectedOutlineWidth = 3.dp
private val SelectedCheckSize = 24.dp

/**
 * Layout of grid list item with title overlaying the cover.
 * Accepts null [title] for a cover-only view.
 */
@Composable
fun MangaCompactGridItem(
    coverData: MangaCoverModel,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    isSelected: Boolean = false,
    title: String? = null,
    onClickContinueReading: (() -> Unit)? = null,
    coverAlpha: Float = 1f,
    coverBadgeStart: @Composable (RowScope.() -> Unit)? = null,
    coverBadgeEnd: @Composable (RowScope.() -> Unit)? = null,
) {
    GridItemSelectable(
        onClick = onClick,
        onLongClick = onLongClick,
    ) {
        MangaGridCover(
            isSelected = isSelected,
            cover = {
                MangaCover.Book(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(coverAlpha),
                    data = coverData,
                )
            },
            badgesStart = coverBadgeStart,
            badgesEnd = coverBadgeEnd,
            content = {
                if (title != null) {
                    CoverTextOverlay(
                        title = title,
                        onClickContinueReading = onClickContinueReading,
                    )
                } else if (onClickContinueReading != null) {
                    ContinueReadingButton(
                        size = ContinueReadingButtonSizeLarge,
                        iconSize = ContinueReadingButtonIconSizeLarge,
                        onClick = onClickContinueReading,
                        modifier = Modifier
                            .padding(ContinueReadingButtonGridPadding)
                            .align(Alignment.BottomEnd),
                    )
                }
            },
        )
    }
}

/**
 * Title overlay for [MangaCompactGridItem]
 */
@Composable
private fun BoxScope.CoverTextOverlay(
    title: String,
    onClickContinueReading: (() -> Unit)? = null,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
            .background(
                Brush.verticalGradient(
                    0f to Color.Transparent,
                    1f to Color(0xAA000000),
                ),
            )
            .fillMaxHeight(0.33f)
            .fillMaxWidth()
            .align(Alignment.BottomCenter),
    )
    Row(
        modifier = Modifier.align(Alignment.BottomStart),
        verticalAlignment = Alignment.Bottom,
    ) {
        GridItemTitle(
            modifier = Modifier
                .weight(1f)
                .padding(8.dp),
            title = title,
            style = MaterialTheme.typography.labelMedium.copy(
                color = Color.White,
                shadow = Shadow(
                    color = Color.Black,
                    blurRadius = 4f,
                ),
            ),
            minLines = 1,
        )
        if (onClickContinueReading != null) {
            ContinueReadingButton(
                size = ContinueReadingButtonSizeSmall,
                iconSize = ContinueReadingButtonIconSizeSmall,
                onClick = onClickContinueReading,
                modifier = Modifier.padding(
                    end = ContinueReadingButtonGridPadding,
                    bottom = ContinueReadingButtonGridPadding,
                ),
            )
        }
    }
}

/**
 * Layout of grid list item with title below the cover.
 */
@Composable
fun MangaComfortableGridItem(
    coverData: MangaCoverModel,
    title: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    isSelected: Boolean = false,
    titleMaxLines: Int = 2,
    coverAlpha: Float = 1f,
    coverBadgeStart: (@Composable RowScope.() -> Unit)? = null,
    coverBadgeEnd: (@Composable RowScope.() -> Unit)? = null,
    onClickContinueReading: (() -> Unit)? = null,
) {
    GridItemSelectable(
        onClick = onClick,
        onLongClick = onLongClick,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            MangaGridCover(
                isSelected = isSelected,
                cover = {
                    MangaCover.Book(
                        modifier = Modifier
                            .fillMaxWidth()
                            .alpha(coverAlpha),
                        data = coverData,
                    )
                },
                badgesStart = coverBadgeStart,
                badgesEnd = coverBadgeEnd,
                content = {
                    if (onClickContinueReading != null) {
                        ContinueReadingButton(
                            size = ContinueReadingButtonSizeLarge,
                            iconSize = ContinueReadingButtonIconSizeLarge,
                            onClick = onClickContinueReading,
                            modifier = Modifier
                                .padding(ContinueReadingButtonGridPadding)
                                .align(Alignment.BottomEnd),
                        )
                    }
                },
            )
            GridItemTitle(
                title = title,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                minLines = 2,
                maxLines = titleMaxLines,
            )
        }
    }
}

/**
 * Common cover layout to add contents to be drawn on top of the cover.
 */
@Composable
private fun MangaGridCover(
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    cover: @Composable BoxScope.() -> Unit = {},
    badgesStart: (@Composable RowScope.() -> Unit)? = null,
    badgesEnd: (@Composable RowScope.() -> Unit)? = null,
    content: @Composable (BoxScope.() -> Unit)? = null,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(MangaCover.Book.ratio)
            .clip(MaterialTheme.shapes.small),
    ) {
        cover()
        content?.invoke(this)
        if (isSelected) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .border(SelectedOutlineWidth, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small),
            )
            Icon(
                imageVector = CustomMaterialSymbols.EdgeFilled.Downloaded,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(6.dp)
                    .size(SelectedCheckSize)
                    .background(MaterialTheme.colorScheme.surface, CircleShape)
                    .align(Alignment.BottomEnd),
            )
        }
        if (badgesStart != null) {
            BadgeGroup(
                modifier = Modifier
                    .padding(4.dp)
                    .align(Alignment.TopStart),
                content = badgesStart,
            )
        }

        if (badgesEnd != null) {
            BadgeGroup(
                modifier = Modifier
                    .padding(4.dp)
                    .align(Alignment.TopEnd),
                content = badgesEnd,
            )
        }
    }
}

@Composable
private fun GridItemTitle(
    title: String,
    style: TextStyle,
    minLines: Int,
    modifier: Modifier = Modifier,
    maxLines: Int = 2,
) {
    Text(
        modifier = modifier,
        text = title,
        minLines = minLines,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        style = style,
    )
}

/**
 * Wrapper for grid items to handle selection state, click and long click.
 */
@Composable
private fun GridItemSelectable(
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            ),
    ) {
        content()
    }
}

/**
 * Layout of list item.
 */
@Composable
fun MangaListItem(
    coverData: MangaCoverModel,
    title: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    badge: @Composable (RowScope.() -> Unit),
    isSelected: Boolean = false,
    coverAlpha: Float = 1f,
    onClickContinueReading: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .selectedBackground(isSelected)
            .height(80.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MangaCover.Book(
            modifier = Modifier
                .size(width = 48.dp, height = 64.dp)
                .alpha(coverAlpha),
            data = coverData,
        )
        Text(
            text = title,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .weight(1f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyLarge,
        )
        BadgeGroup(content = badge)
        if (onClickContinueReading != null) {
            ContinueReadingButton(
                size = ContinueReadingButtonSizeSmall,
                iconSize = ContinueReadingButtonIconSizeSmall,
                onClick = onClickContinueReading,
                modifier = Modifier.padding(start = ContinueReadingButtonListSpacing),
            )
        }
    }
}

@Composable
private fun ContinueReadingButton(
    size: Dp,
    iconSize: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        FilledIconButton(
            onClick = onClick,
            shape = MaterialTheme.shapes.small,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ),
            modifier = Modifier.size(size),
        ) {
            Icon(
                imageVector = CustomMaterialSymbols.EdgeFilled.NextChapter,
                contentDescription = stringResource(MR.strings.action_resume),
                modifier = Modifier.size(iconSize),
            )
        }
    }
}
