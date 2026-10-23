package com.nilemnod.mangareader.core.mangaui

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastMaxOfOrNull
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.nilemnod.mangareader.core.ui.components.AdaptiveSheet
import com.nilemnod.mangareader.core.ui.components.Badge
import com.nilemnod.mangareader.core.ui.components.BadgeGroup
import com.nilemnod.mangareader.core.ui.components.TabbedDialogPaddings
import com.nilemnod.mangareader.core.ui.components.material.padding
import com.nilemnod.mangareader.core.ui.components.preference.LocalPreferenceMinHeight
import com.nilemnod.mangareader.core.ui.components.preference.TextPreferenceWidget
import com.nilemnod.mangareader.core.ui.i18n.pluralStringResource
import com.nilemnod.mangareader.core.ui.i18n.stringResource
import com.nilemnod.mangareader.core.ui.util.secondaryItemAlpha
import com.nilemnod.mangareader.domain.manga.model.Manga
import com.nilemnod.mangareader.domain.manga.model.MangaWithChapterCount
import com.nilemnod.mangareader.domain.source.api.ContentSource
import com.nilemnod.mangareader.domain.source.api.MangaStatus
import com.nilemnod.mangareader.domain.source.service.SourceManager
import com.nilemnod.mangareader.i18n.MR
import com.nilemnod.mangareader.icons.materialsymbols.MaterialSymbols
import com.nilemnod.mangareader.icons.materialsymbols.rounded.Add
import com.nilemnod.mangareader.icons.materialsymbols.rounded.AttachMoney
import com.nilemnod.mangareader.icons.materialsymbols.rounded.Block
import com.nilemnod.mangareader.icons.materialsymbols.rounded.Check
import com.nilemnod.mangareader.icons.materialsymbols.rounded.Close
import com.nilemnod.mangareader.icons.materialsymbols.rounded.DoneAll
import com.nilemnod.mangareader.icons.materialsymbols.rounded.Person
import com.nilemnod.mangareader.icons.materialsymbols.rounded.Schedule
import com.nilemnod.mangareader.icons.materialsymbols.roundedfilled.Brush
import com.nilemnod.mangareader.icons.materialsymbols.roundedfilled.Pause
import com.nilemnod.mangareader.icons.materialsymbols.roundedfilled.Warning

@Composable
fun DuplicateMangaDialog(
    duplicates: List<MangaWithChapterCount>,
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    onOpenManga: (manga: Manga) -> Unit,
    onMigrate: (manga: Manga) -> Unit,
    sourceManager: SourceManager,
    modifier: Modifier = Modifier,
) {
    val minHeight = LocalPreferenceMinHeight.current
    val horizontalPadding = PaddingValues(horizontal = TabbedDialogPaddings.Horizontal)
    val horizontalPaddingModifier = Modifier.padding(horizontalPadding)

    AdaptiveSheet(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = TabbedDialogPaddings.Vertical)
                .verticalScroll(rememberScrollState())
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.padding.medium),
        ) {
            Text(
                text = stringResource(MR.strings.possible_duplicates_title),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier
                    .then(horizontalPaddingModifier)
                    .padding(top = MaterialTheme.padding.small),
            )

            Text(
                text = stringResource(MR.strings.possible_duplicates_summary),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.then(horizontalPaddingModifier),
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.padding.small),
                modifier = Modifier.height(getMaximumMangaCardHeight(duplicates)),
                contentPadding = horizontalPadding,
            ) {
                items(
                    items = duplicates,
                    key = { it.manga.id },
                ) {
                    DuplicateMangaListItem(
                        duplicate = it,
                        getSource = { sourceManager.getOrStub(it.manga.source) },
                        onMigrate = { onMigrate(it.manga) },
                        onDismissRequest = onDismissRequest,
                        onOpenManga = { onOpenManga(it.manga) },
                    )
                }
            }

            Column(modifier = horizontalPaddingModifier) {
                HorizontalDivider()

                TextPreferenceWidget(
                    title = stringResource(MR.strings.action_add_anyway),
                    icon = MaterialSymbols.Rounded.Add,
                    onPreferenceClick = {
                        onDismissRequest()
                        onConfirm()
                    },
                    modifier = Modifier.clip(CircleShape),
                )
            }

            OutlinedButton(
                onClick = onDismissRequest,
                modifier = Modifier
                    .then(horizontalPaddingModifier)
                    .padding(bottom = MaterialTheme.padding.medium)
                    .heightIn(min = minHeight)
                    .fillMaxWidth(),
            ) {
                Text(
                    modifier = Modifier.padding(vertical = MaterialTheme.padding.extraSmall),
                    text = stringResource(MR.strings.action_cancel),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}

@Composable
private fun DuplicateMangaListItem(
    duplicate: MangaWithChapterCount,
    getSource: suspend () -> ContentSource,
    onDismissRequest: () -> Unit,
    onOpenManga: () -> Unit,
    onMigrate: () -> Unit,
) {
    val source by produceState<ContentSource?>(initialValue = null) { value = getSource() }
    val manga = duplicate.manga
    Column(
        modifier = Modifier
            .width(MangaCardWidth)
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surface)
            .combinedClickable(
                onLongClick = { onOpenManga() },
                onClick = {
                    onDismissRequest()
                    onMigrate()
                },
            )
            .padding(MaterialTheme.padding.small),
    ) {
        Box {
            MangaCover.Book(
                data = ImageRequest.Builder(LocalContext.current)
                    .data(manga)
                    .crossfade(true)
                    .build(),
                modifier = Modifier.fillMaxWidth(),
            )
            BadgeGroup(
                modifier = Modifier
                    .padding(4.dp)
                    .align(Alignment.TopStart),
            ) {
                Badge(
                    color = MaterialTheme.colorScheme.secondary,
                    textColor = MaterialTheme.colorScheme.onSecondary,
                    text = pluralStringResource(
                        MR.plurals.manga_num_chapters,
                        duplicate.chapterCount.toInt(),
                        duplicate.chapterCount,
                    ),
                )
            }
        }

        Spacer(modifier = Modifier.height(MaterialTheme.padding.extraSmall))

        Text(
            text = manga.title,
            style = MaterialTheme.typography.titleSmall,
            overflow = TextOverflow.Ellipsis,
            maxLines = 2,
        )

        if (!manga.author.isNullOrBlank()) {
            MangaDetailRow(
                text = manga.author!!,
                iconImageVector = MaterialSymbols.Rounded.Person,
                maxLines = 2,
            )
        }

        if (!manga.artist.isNullOrBlank() && manga.author != manga.artist) {
            MangaDetailRow(
                text = manga.artist!!,
                iconImageVector = MaterialSymbols.RoundedFilled.Brush,
                maxLines = 2,
            )
        }

        MangaDetailRow(
            text = when (manga.status) {
                MangaStatus.ONGOING.code -> stringResource(MR.strings.ongoing)
                MangaStatus.COMPLETED.code -> stringResource(MR.strings.completed)
                MangaStatus.LICENSED.code -> stringResource(MR.strings.licensed)
                MangaStatus.PUBLISHING_FINISHED.code -> stringResource(MR.strings.publishing_finished)
                MangaStatus.CANCELLED.code -> stringResource(MR.strings.cancelled)
                MangaStatus.ON_HIATUS.code -> stringResource(MR.strings.on_hiatus)
                else -> stringResource(MR.strings.unknown)
            },
            iconImageVector = when (manga.status) {
                MangaStatus.ONGOING.code -> MaterialSymbols.Rounded.Schedule
                MangaStatus.COMPLETED.code -> MaterialSymbols.Rounded.DoneAll
                MangaStatus.LICENSED.code -> MaterialSymbols.Rounded.AttachMoney
                MangaStatus.PUBLISHING_FINISHED.code -> MaterialSymbols.Rounded.Check
                MangaStatus.CANCELLED.code -> MaterialSymbols.Rounded.Close
                MangaStatus.ON_HIATUS.code -> MaterialSymbols.RoundedFilled.Pause
                else -> MaterialSymbols.Rounded.Block
            },
        )

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            if (source?.isStub == true) {
                Icon(
                    imageVector = MaterialSymbols.RoundedFilled.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
            Text(
                text = source?.name.orEmpty(),
                style = MaterialTheme.typography.labelSmall,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun MangaDetailRow(
    text: String,
    iconImageVector: ImageVector,
    maxLines: Int = 1,
) {
    Row(
        modifier = Modifier
            .secondaryItemAlpha()
            .padding(top = MaterialTheme.padding.extraSmall),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.padding.extraSmall),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = iconImageVector,
            contentDescription = null,
            modifier = Modifier.size(MangaDetailsIconWidth),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            overflow = TextOverflow.Ellipsis,
            maxLines = maxLines,
        )
    }
}

@Composable
private fun getMaximumMangaCardHeight(duplicates: List<MangaWithChapterCount>): Dp {
    val density = LocalDensity.current
    val typography = MaterialTheme.typography
    val textMeasurer = rememberTextMeasurer()

    val smallPadding = with(density) { MaterialTheme.padding.small.roundToPx() }
    val extraSmallPadding = with(density) { MaterialTheme.padding.extraSmall.roundToPx() }

    val width = with(density) { MangaCardWidth.roundToPx() - (2 * smallPadding) }
    val iconWidth = with(density) { MangaDetailsIconWidth.roundToPx() }

    val coverHeight = width / MangaCover.Book.ratio
    val constraints = Constraints(maxWidth = width)
    val detailsConstraints = Constraints(maxWidth = width - iconWidth - extraSmallPadding)

    return remember(
        duplicates,
        density,
        typography,
        textMeasurer,
        smallPadding,
        extraSmallPadding,
        coverHeight,
        constraints,
        detailsConstraints,
    ) {
        duplicates.fastMaxOfOrNull {
            calculateMangaCardHeight(
                manga = it.manga,
                density = density,
                typography = typography,
                textMeasurer = textMeasurer,
                smallPadding = smallPadding,
                extraSmallPadding = extraSmallPadding,
                coverHeight = coverHeight,
                constraints = constraints,
                detailsConstraints = detailsConstraints,
            )
        }
            ?: 0.dp
    }
}

private fun calculateMangaCardHeight(
    manga: Manga,
    density: Density,
    typography: Typography,
    textMeasurer: TextMeasurer,
    smallPadding: Int,
    extraSmallPadding: Int,
    coverHeight: Float,
    constraints: Constraints,
    detailsConstraints: Constraints,
): Dp {
    val titleHeight = textMeasurer.measureHeight(manga.title, typography.titleSmall, 2, constraints)
    val authorHeight = if (!manga.author.isNullOrBlank()) {
        textMeasurer.measureHeight(manga.author!!, typography.bodySmall, 2, detailsConstraints)
    } else {
        0
    }
    val artistHeight = if (!manga.artist.isNullOrBlank() && manga.author != manga.artist) {
        textMeasurer.measureHeight(manga.artist!!, typography.bodySmall, 2, detailsConstraints)
    } else {
        0
    }
    val statusHeight = textMeasurer.measureHeight("", typography.bodySmall, 2, detailsConstraints)
    val sourceHeight = textMeasurer.measureHeight("", typography.labelSmall, 1, constraints)

    val totalHeight = coverHeight + titleHeight + authorHeight + artistHeight + statusHeight + sourceHeight
    return with(density) { ((2 * smallPadding) + totalHeight + (5 * extraSmallPadding)).toDp() }
}

private fun TextMeasurer.measureHeight(
    text: String,
    style: TextStyle,
    maxLines: Int,
    constraints: Constraints,
): Int = measure(
    text = text,
    style = style,
    overflow = TextOverflow.Ellipsis,
    maxLines = maxLines,
    constraints = constraints,
)
    .size
    .height

private val MangaCardWidth = 150.dp
private val MangaDetailsIconWidth = 16.dp
