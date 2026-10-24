package com.nilemnod.mangareader.feature.reader.legacy.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.nilemnod.mangareader.core.ui.i18n.pluralStringResource
import com.nilemnod.mangareader.core.ui.i18n.stringResource
import com.nilemnod.mangareader.core.ui.theme.MangaReaderPreviewTheme
import com.nilemnod.mangareader.domain.chapter.model.Chapter
import com.nilemnod.mangareader.domain.chapter.service.calculateChapterGap
import com.nilemnod.mangareader.feature.reader.legacy.model.ChapterTransition
import com.nilemnod.mangareader.feature.reader.legacy.model.ReaderChapter
import com.nilemnod.mangareader.i18n.MR
import com.nilemnod.mangareader.icons.custommaterialsymbols.CustomMaterialSymbols
import com.nilemnod.mangareader.icons.custommaterialsymbols.edge.About
import com.nilemnod.mangareader.icons.custommaterialsymbols.edgefilled.Downloaded
import com.nilemnod.mangareader.icons.custommaterialsymbols.edgefilled.Warning
@Composable
fun ChapterTransition(
    transition: ChapterTransition,
    currChapterDownloaded: Boolean,
    goingToChapterDownloaded: Boolean,
) {
    val currChapter = transition.from.chapter
    val goingToChapter = transition.to?.chapter

    ProvideTextStyle(MaterialTheme.typography.bodyMedium) {
        when (transition) {
            is ChapterTransition.Prev -> {
                TransitionText(
                    topLabel = stringResource(MR.strings.transition_previous),
                    topChapter = goingToChapter,
                    topChapterDownloaded = goingToChapterDownloaded,
                    topHighlighted = true,
                    bottomLabel = stringResource(MR.strings.transition_current),
                    bottomChapter = currChapter,
                    bottomChapterDownloaded = currChapterDownloaded,
                    fallbackLabel = stringResource(MR.strings.transition_no_previous),
                    chapterGap = calculateChapterGap(currChapter, goingToChapter),
                )
            }
            is ChapterTransition.Next -> {
                TransitionText(
                    topLabel = stringResource(MR.strings.transition_finished),
                    topChapter = currChapter,
                    topChapterDownloaded = currChapterDownloaded,
                    topHighlighted = false,
                    bottomLabel = stringResource(MR.strings.transition_next),
                    bottomChapter = goingToChapter,
                    bottomChapterDownloaded = goingToChapterDownloaded,
                    fallbackLabel = stringResource(MR.strings.transition_no_next),
                    chapterGap = calculateChapterGap(goingToChapter, currChapter),
                )
            }
        }
    }
}

@Composable
private fun TransitionText(
    topLabel: String,
    topChapter: Chapter?,
    topChapterDownloaded: Boolean,
    topHighlighted: Boolean,
    bottomLabel: String,
    bottomChapter: Chapter?,
    bottomChapterDownloaded: Boolean,
    fallbackLabel: String,
    chapterGap: Int,
) {
    Column(
        modifier = Modifier
            .widthIn(max = 420.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (topChapter != null) {
            ChapterText(
                header = topLabel,
                name = topChapter.name,
                scanlator = topChapter.scanlator,
                downloaded = topChapterDownloaded,
                highlighted = topHighlighted,
            )
        } else {
            NoChapterNotification(
                text = fallbackLabel,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        if (bottomChapter != null) {
            if (chapterGap > 0) {
                ChapterGapWarning(
                    gapCount = chapterGap,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            ChapterText(
                header = bottomLabel,
                name = bottomChapter.name,
                scanlator = bottomChapter.scanlator,
                downloaded = bottomChapterDownloaded,
                highlighted = !topHighlighted,
            )
        } else {
            NoChapterNotification(
                text = fallbackLabel,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun NoChapterNotification(
    text: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardColor,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = CustomMaterialSymbols.Edge.About,
                tint = MaterialTheme.colorScheme.primary,
                contentDescription = null,
            )

            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun ChapterGapWarning(
    gapCount: Int,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardColor,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = CustomMaterialSymbols.EdgeFilled.Warning,
                tint = MaterialTheme.colorScheme.error,
                contentDescription = null,
            )

            Text(
                text = pluralStringResource(MR.plurals.missing_chapters_warning, count = gapCount, gapCount),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun ChapterText(
    header: String,
    name: String,
    scanlator: String?,
    downloaded: Boolean,
    highlighted: Boolean,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = if (highlighted) {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        } else {
            CardColor
        },
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (downloaded && !highlighted) {
                Icon(
                    imageVector = CustomMaterialSymbols.EdgeFilled.Downloaded,
                    contentDescription = stringResource(MR.strings.label_downloaded),
                    tint = MaterialTheme.colorScheme.tertiary,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = header.trimEnd(':', ' ').uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (highlighted) LocalContentColor.current else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = name,
                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis,
                    style = if (highlighted) {
                        MaterialTheme.typography.titleLarge
                    } else {
                        MaterialTheme.typography.bodyLarge
                    },
                )
                val details = listOfNotNull(
                    stringResource(MR.strings.label_downloaded).takeIf { downloaded && highlighted },
                    scanlator?.takeIf { it.isNotBlank() },
                ).joinToString(" · ")
                if (details.isNotEmpty()) {
                    Text(
                        text = details,
                        modifier = Modifier.padding(top = 2.dp),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (highlighted) {
                            LocalContentColor.current
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
            }
        }
    }
}

private val CardColor: CardColors
    @Composable
    get() = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        contentColor = MaterialTheme.colorScheme.onSurface,
    )

private fun previewChapter(name: String, scanlator: String, chapterNumber: Double) = Chapter.create().copy(
    id = 0L,
    mangaId = 0L,
    url = "",
    name = name,
    scanlator = scanlator,
    chapterNumber = chapterNumber,
)
private val FakeChapter = previewChapter(
    name = "Vol.1, Ch.1 - Fake Chapter Title",
    scanlator = "Scanlator Name",
    chapterNumber = 1.0,
)
private val FakeGapChapter = previewChapter(
    name = "Vol.5, Ch.44 - Fake Gap Chapter Title",
    scanlator = "Scanlator Name",
    chapterNumber = 44.0,
)
private val FakeChapterLongTitle = previewChapter(
    name = "Vol.1, Ch.0 - The Mundane Musings of a Metafictional Manga: A Chapter About a Chapter, Featuring" +
        " an Absurdly Long Title and a Surprisingly Normal Day in the Lives of Our Heroes, as They Grapple with the " +
        "Daily Challenges of Existence, from Paying Rent to Finding Love, All While Navigating the Strange World of " +
        "Fictional Realities and Reality-Bending Fiction, Where the Fourth Wall is Always in Danger of Being Broken " +
        "and the Line Between Author and Character is Forever Blurred.",
    scanlator = "Long Long Funny Scanlator Sniper Group Name Reborn",
    chapterNumber = 1.0,
)

@PreviewLightDark
@Composable
private fun TransitionTextPreview() {
    MangaReaderPreviewTheme {
        Surface(modifier = Modifier.padding(48.dp)) {
            ChapterTransition(
                transition = ChapterTransition.Next(ReaderChapter(FakeChapter), ReaderChapter(FakeChapter)),
                currChapterDownloaded = false,
                goingToChapterDownloaded = true,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun TransitionTextLongTitlePreview() {
    MangaReaderPreviewTheme {
        Surface(modifier = Modifier.padding(48.dp)) {
            ChapterTransition(
                transition = ChapterTransition.Next(ReaderChapter(FakeChapterLongTitle), ReaderChapter(FakeChapter)),
                currChapterDownloaded = true,
                goingToChapterDownloaded = true,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun TransitionTextWithGapPreview() {
    MangaReaderPreviewTheme {
        Surface(modifier = Modifier.padding(48.dp)) {
            ChapterTransition(
                transition = ChapterTransition.Next(ReaderChapter(FakeChapter), ReaderChapter(FakeGapChapter)),
                currChapterDownloaded = true,
                goingToChapterDownloaded = false,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun TransitionTextNoNextPreview() {
    MangaReaderPreviewTheme {
        Surface(modifier = Modifier.padding(48.dp)) {
            ChapterTransition(
                transition = ChapterTransition.Next(ReaderChapter(FakeChapter), null),
                currChapterDownloaded = true,
                goingToChapterDownloaded = false,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun TransitionTextNoPreviousPreview() {
    MangaReaderPreviewTheme {
        Surface(modifier = Modifier.padding(48.dp)) {
            ChapterTransition(
                transition = ChapterTransition.Prev(ReaderChapter(FakeChapter), null),
                currChapterDownloaded = true,
                goingToChapterDownloaded = false,
            )
        }
    }
}
