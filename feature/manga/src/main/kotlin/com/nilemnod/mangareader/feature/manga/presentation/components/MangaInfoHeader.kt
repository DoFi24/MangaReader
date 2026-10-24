package com.nilemnod.mangareader.feature.manga.presentation.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.mikepenz.markdown.model.markdownAnnotator
import com.mikepenz.markdown.model.markdownAnnotatorConfig
import com.mikepenz.markdown.utils.getUnescapedTextInNode
import com.nilemnod.mangareader.core.common.util.system.copyToClipboard
import com.nilemnod.mangareader.core.mangaui.DISALLOWED_MARKDOWN_TYPES
import com.nilemnod.mangareader.core.mangaui.DotSeparatorText
import com.nilemnod.mangareader.core.mangaui.MARKDOWN_INLINE_IMAGE_TAG
import com.nilemnod.mangareader.core.mangaui.MangaCover
import com.nilemnod.mangareader.core.mangaui.MarkdownRender
import com.nilemnod.mangareader.core.mangaui.getMarkdownLinkStyle
import com.nilemnod.mangareader.core.ui.components.DropdownMenu
import com.nilemnod.mangareader.core.ui.components.material.TextButton
import com.nilemnod.mangareader.core.ui.components.material.padding
import com.nilemnod.mangareader.core.ui.i18n.pluralStringResource
import com.nilemnod.mangareader.core.ui.i18n.stringResource
import com.nilemnod.mangareader.core.ui.preference.appUiPreferences
import com.nilemnod.mangareader.core.ui.theme.Motion
import com.nilemnod.mangareader.core.ui.util.clickableNoIndication
import com.nilemnod.mangareader.domain.manga.model.Manga
import com.nilemnod.mangareader.domain.source.api.ContentSource
import com.nilemnod.mangareader.domain.source.api.MangaStatus
import com.nilemnod.mangareader.i18n.MR
import com.nilemnod.mangareader.icons.custommaterialsymbols.CustomMaterialSymbols
import com.nilemnod.mangareader.icons.custommaterialsymbols.edge.About
import com.nilemnod.mangareader.icons.custommaterialsymbols.edge.Cancelled
import com.nilemnod.mangareader.icons.custommaterialsymbols.edge.Completed
import com.nilemnod.mangareader.icons.custommaterialsymbols.edge.Favorite
import com.nilemnod.mangareader.icons.custommaterialsymbols.edge.Note
import com.nilemnod.mangareader.icons.custommaterialsymbols.edge.OnHold
import com.nilemnod.mangareader.icons.custommaterialsymbols.edge.Ongoing
import com.nilemnod.mangareader.icons.custommaterialsymbols.edge.Security
import com.nilemnod.mangareader.icons.custommaterialsymbols.edge.Updates
import com.nilemnod.mangareader.icons.custommaterialsymbols.edge.Web
import com.nilemnod.mangareader.icons.custommaterialsymbols.edgefilled.Favorite
import com.nilemnod.mangareader.icons.custommaterialsymbols.edgefilled.Updates
import com.nilemnod.mangareader.icons.custommaterialsymbols.edgefilled.Warning
import com.nilemnod.mangareader.icons.materialsymbols.MaterialSymbols
import com.nilemnod.mangareader.icons.materialsymbols.rounded.KeyboardArrowDown
import com.nilemnod.mangareader.icons.materialsymbols.rounded.KeyboardArrowUp
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.ast.findChildOfType
import kotlin.math.roundToInt
import kotlin.time.Clock
import kotlin.time.Instant

@Composable
fun MangaInfoBox(
    isTabletUi: Boolean,
    appBarPadding: Dp,
    manga: Manga,
    source: ContentSource,
    sourceName: String,
    isStubSource: Boolean,
    onCoverClick: () -> Unit,
    doSearch: (query: String, global: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
            if (!isTabletUi) {
                MangaAndSourceTitlesSmall(
                    appBarPadding = appBarPadding,
                    manga = manga,
                    source = source,
                    sourceName = sourceName,
                    isStubSource = isStubSource,
                    onCoverClick = onCoverClick,
                    doSearch = doSearch,
                )
            } else {
                MangaAndSourceTitlesLarge(
                    appBarPadding = appBarPadding,
                    manga = manga,
                    source = source,
                    sourceName = sourceName,
                    isStubSource = isStubSource,
                    onCoverClick = onCoverClick,
                    doSearch = doSearch,
                )
            }
        }
    }
}

@Composable
fun MangaActionRow(
    favorite: Boolean,
    nextUpdate: Instant?,
    isUserIntervalMode: Boolean,
    onAddToLibraryClicked: () -> Unit,
    onWebViewClicked: (() -> Unit)?,
    onWebViewLongClicked: (() -> Unit)?,
    onEditIntervalClicked: (() -> Unit)?,
    onEditCategory: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val defaultActionButtonColor = MaterialTheme.colorScheme.onSurfaceVariant

    val nextUpdateDays = remember(nextUpdate) {
        return@remember if (nextUpdate != null) {
            val now = Clock.System.now()
            now.daysUntil(nextUpdate, TimeZone.currentSystemDefault()).coerceAtLeast(0)
        } else {
            null
        }
    }

    Row(
        modifier = modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        MangaActionButton(
            title = if (favorite) {
                stringResource(MR.strings.in_library)
            } else {
                stringResource(MR.strings.add_to_library)
            },
            icon = if (favorite) CustomMaterialSymbols.EdgeFilled.Favorite else CustomMaterialSymbols.Edge.Favorite,
            color = if (favorite) MaterialTheme.colorScheme.primary else defaultActionButtonColor,
            onClick = onAddToLibraryClicked,
            onLongClick = onEditCategory,
        )
        MangaActionButton(
            title = when (nextUpdateDays) {
                null -> stringResource(MR.strings.not_applicable)
                0 -> stringResource(MR.strings.manga_interval_expected_update_soon)
                else -> pluralStringResource(
                    MR.plurals.day,
                    count = nextUpdateDays,
                    nextUpdateDays,
                )
            },
            icon = if (isUserIntervalMode) {
                CustomMaterialSymbols.EdgeFilled.Updates
            } else {
                CustomMaterialSymbols.Edge.Updates
            },
            color = if (isUserIntervalMode) MaterialTheme.colorScheme.primary else defaultActionButtonColor,
            onClick = { onEditIntervalClicked?.invoke() },
        )
        if (onWebViewClicked != null) {
            MangaActionButton(
                title = stringResource(MR.strings.action_web_view),
                icon = CustomMaterialSymbols.Edge.Web,
                color = defaultActionButtonColor,
                onClick = onWebViewClicked,
                onLongClick = onWebViewLongClicked,
            )
        }
    }
}

@Composable
fun ExpandableMangaDescription(
    defaultExpandState: Boolean,
    description: String?,
    tagsProvider: () -> List<String>?,
    notes: String,
    onTagSearch: (String) -> Unit,
    onCopyTagToClipboard: (tag: String) -> Unit,
    onEditNotes: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        val (expanded, onExpanded) = rememberSaveable {
            mutableStateOf(defaultExpandState)
        }
        val desc =
            description.takeIf { !it.isNullOrBlank() } ?: stringResource(MR.strings.description_placeholder)

        MangaSummary(
            description = desc,
            expanded = expanded,
            onToggle = { onExpanded(!expanded) },
            modifier = Modifier
                .padding(top = 16.dp)
                .padding(horizontal = 16.dp)
                .clickableNoIndication { onExpanded(!expanded) },
        )
        val tags = tagsProvider()
        if (!tags.isNullOrEmpty()) {
            Box(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .animateContentSize(animationSpec = tween(Motion.MEDIUM, easing = Motion.Standard))
                    .fillMaxWidth(),
            ) {
                var showMenu by remember { mutableStateOf(false) }
                var tagSelected by remember { mutableStateOf("") }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                ) {
                    DropdownMenuItem(
                        text = { Text(text = stringResource(MR.strings.action_search)) },
                        onClick = {
                            onTagSearch(tagSelected)
                            showMenu = false
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(text = stringResource(MR.strings.action_copy_to_clipboard)) },
                        onClick = {
                            onCopyTagToClipboard(tagSelected)
                            showMenu = false
                        },
                    )
                }
                if (expanded) {
                    FlowRow(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        tags.forEach {
                            TagsChip(
                                modifier = DefaultTagChipModifier,
                                text = it,
                                onClick = {
                                    tagSelected = it
                                    showMenu = true
                                },
                            )
                        }
                    }
                } else {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(items = tags) {
                            TagsChip(
                                modifier = DefaultTagChipModifier,
                                text = it,
                                onClick = {
                                    tagSelected = it
                                    showMenu = true
                                },
                            )
                        }
                    }
                }
            }
        }
        MangaNoteCard(
            notes = notes,
            onClick = onEditNotes,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp, bottom = 8.dp),
        )
    }
}

@Composable
private fun MangaNoteCard(
    notes: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Row(
            modifier = Modifier
                .heightIn(min = 56.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = CustomMaterialSymbols.Edge.Note,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (notes.isBlank()) {
                Text(
                    text = stringResource(MR.strings.add_note),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                MangaNotesDisplay(
                    content = notes,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun MangaAndSourceTitlesLarge(
    appBarPadding: Dp,
    manga: Manga,
    source: ContentSource,
    sourceName: String,
    isStubSource: Boolean,
    onCoverClick: () -> Unit,
    doSearch: (query: String, global: Boolean) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = appBarPadding + 16.dp, end = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        MangaCover.Book(
            modifier = Modifier.fillMaxWidth(0.65f),
            data = ImageRequest.Builder(LocalContext.current)
                .data(manga)
                .crossfade(true)
                .build(),
            contentDescription = stringResource(MR.strings.manga_cover),
            onClick = onCoverClick,
        )
        Spacer(modifier = Modifier.height(16.dp))
        MangaContentInfo(
            title = manga.title,
            author = manga.author,
            artist = manga.artist,
            status = manga.status,
            source = source,
            sourceName = sourceName,
            isStubSource = isStubSource,
            doSearch = doSearch,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun MangaAndSourceTitlesSmall(
    appBarPadding: Dp,
    manga: Manga,
    source: ContentSource,
    sourceName: String,
    isStubSource: Boolean,
    onCoverClick: () -> Unit,
    doSearch: (query: String, global: Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = appBarPadding + 16.dp, end = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MangaCover.Book(
            modifier = Modifier
                .width(112.dp)
                .align(Alignment.Top),
            data = ImageRequest.Builder(LocalContext.current)
                .data(manga)
                .crossfade(true)
                .build(),
            contentDescription = stringResource(MR.strings.manga_cover),
            onClick = onCoverClick,
        )
        Column(
            modifier = Modifier.align(Alignment.Top).padding(top = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MangaContentInfo(
                title = manga.title,
                author = manga.author,
                artist = manga.artist,
                status = manga.status,
                source = source,
                sourceName = sourceName,
                isStubSource = isStubSource,
                doSearch = doSearch,
            )
        }
    }
}

@Composable
private fun ColumnScope.MangaContentInfo(
    title: String,
    author: String?,
    artist: String?,
    status: Long,
    source: ContentSource,
    sourceName: String,
    isStubSource: Boolean,
    doSearch: (query: String, global: Boolean) -> Unit,
    textAlign: TextAlign? = LocalTextStyle.current.textAlign,
) {
    val context = LocalContext.current
    Text(
        text = title.ifBlank { stringResource(MR.strings.unknown_title) },
        style = MaterialTheme.typography.headlineSmall,
        maxLines = 3,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.clickableNoIndication(
            onLongClick = {
                if (title.isNotBlank()) {
                    context.copyToClipboard(
                        title,
                        title,
                    )
                }
            },
            onClick = { if (title.isNotBlank()) doSearch(title, true) },
        ),
        textAlign = textAlign,
    )

    ProvideTextStyle(
        MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
    ) {
        Text(
            text = author?.takeIf { it.isNotBlank() }
                ?: stringResource(MR.strings.unknown_author),
            modifier = Modifier
                .clickableNoIndication(
                    onLongClick = {
                        if (!author.isNullOrBlank()) {
                            context.copyToClipboard(
                                author,
                                author,
                            )
                        }
                    },
                    onClick = { if (!author.isNullOrBlank()) doSearch(author, true) },
                ),
            textAlign = textAlign,
        )

        if (!artist.isNullOrBlank() && author != artist) {
            Text(
                text = artist,
                modifier = Modifier
                    .clickableNoIndication(
                        onLongClick = { context.copyToClipboard(artist, artist) },
                        onClick = { doSearch(artist, true) },
                    ),
                textAlign = textAlign,
            )
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = when (status) {
                MangaStatus.ONGOING.code -> CustomMaterialSymbols.Edge.Ongoing
                MangaStatus.COMPLETED.code -> CustomMaterialSymbols.Edge.Completed
                MangaStatus.LICENSED.code -> CustomMaterialSymbols.Edge.Security
                MangaStatus.PUBLISHING_FINISHED.code -> CustomMaterialSymbols.Edge.Completed
                MangaStatus.CANCELLED.code -> CustomMaterialSymbols.Edge.Cancelled
                MangaStatus.ON_HIATUS.code -> CustomMaterialSymbols.Edge.OnHold
                else -> CustomMaterialSymbols.Edge.About
            },
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .padding(end = 8.dp)
                .size(20.dp),
        )
        ProvideTextStyle(
            MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
        ) {
            Text(
                text = when (status) {
                    MangaStatus.ONGOING.code -> stringResource(MR.strings.ongoing)
                    MangaStatus.COMPLETED.code -> stringResource(MR.strings.completed)
                    MangaStatus.LICENSED.code -> stringResource(MR.strings.licensed)
                    MangaStatus.PUBLISHING_FINISHED.code -> stringResource(MR.strings.publishing_finished)
                    MangaStatus.CANCELLED.code -> stringResource(MR.strings.cancelled)
                    MangaStatus.ON_HIATUS.code -> stringResource(MR.strings.on_hiatus)
                    else -> stringResource(MR.strings.unknown)
                },
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )
            DotSeparatorText()
            if (isStubSource) {
                Icon(
                    imageVector = CustomMaterialSymbols.EdgeFilled.Warning,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(end = 4.dp)
                        .size(16.dp),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
            Text(
                text = sourceName,
                modifier = Modifier.clickableNoIndication {
                    doSearch(source.query(), false)
                },
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )
        }
    }
}

private fun ContentSource.query(): String = """src:"$name" lang:$lang""".takeIf { name.isNotBlank() } ?: "srcid:$id"

@Composable
private fun descriptionAnnotator(loadImages: Boolean, linkStyle: SpanStyle) = remember(loadImages, linkStyle) {
    markdownAnnotator(
        annotate = { content, child ->
            if (!loadImages && child.type == MarkdownElementTypes.IMAGE) {
                val inlineLink = child.findChildOfType(MarkdownElementTypes.INLINE_LINK)

                val url = inlineLink?.findChildOfType(MarkdownElementTypes.LINK_DESTINATION)
                    ?.getUnescapedTextInNode(content)
                    ?: inlineLink?.findChildOfType(MarkdownElementTypes.AUTOLINK)
                        ?.findChildOfType(MarkdownTokenTypes.AUTOLINK)
                        ?.getUnescapedTextInNode(content)
                    ?: return@markdownAnnotator false

                val textNode = inlineLink?.findChildOfType(MarkdownElementTypes.LINK_TITLE)
                    ?: inlineLink?.findChildOfType(MarkdownElementTypes.LINK_TEXT)
                val altText = textNode?.findChildOfType(MarkdownTokenTypes.TEXT)
                    ?.getUnescapedTextInNode(content).orEmpty()

                withLink(LinkAnnotation.Url(url = url)) {
                    pushStyle(linkStyle)
                    appendInlineContent(MARKDOWN_INLINE_IMAGE_TAG)
                    append(altText)
                    pop()
                }

                return@markdownAnnotator true
            }

            if (child.type in DISALLOWED_MARKDOWN_TYPES) {
                append(content.substring(child.startOffset, child.endOffset))
                return@markdownAnnotator true
            }

            false
        },
        config = markdownAnnotatorConfig(
            eolAsNewLine = true,
        ),
    )
}

@Composable
private fun MangaSummary(
    description: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val preferences = remember { context.appUiPreferences }
    val loadImages = remember { preferences.imagesInDescription.get() }
    val animProgress by animateFloatAsState(
        targetValue = if (expanded) 1f else 0f,
        animationSpec = tween(Motion.MEDIUM, easing = Motion.Standard),
        label = "summary",
    )
    var infoHeight by remember { mutableIntStateOf(0) }
    Layout(
        modifier = modifier.clipToBounds(),
        contents = listOf(
            {
                Text(
                    text = "\n\n",
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            {
                Column(
                    modifier = Modifier.onSizeChanged { size ->
                        infoHeight = size.height
                    },
                ) {
                    SelectionContainer {
                        MarkdownRender(
                            content = description,
                            annotator = descriptionAnnotator(
                                loadImages = loadImages,
                                linkStyle = getMarkdownLinkStyle().toSpanStyle(),
                            ),
                            loadImages = loadImages,
                        )
                    }
                }
            },
            {
                Box(
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center,
                ) {
                    IconButton(
                        onClick = onToggle,
                        modifier = Modifier.size(width = 48.dp, height = 32.dp),
                    ) {
                        Icon(
                            imageVector = if (expanded) {
                                MaterialSymbols.Rounded.KeyboardArrowUp
                            } else {
                                MaterialSymbols.Rounded.KeyboardArrowDown
                            },
                            contentDescription = stringResource(
                                if (expanded) MR.strings.manga_info_collapse else MR.strings.manga_info_expand,
                            ),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            },
        ),
    ) { (shrunk, actual, scrim), constraints ->
        val shrunkHeight = shrunk.single()
            .measure(constraints)
            .height
        val heightDelta = infoHeight - shrunkHeight
        val scrimHeight = 32.dp.roundToPx()

        val actualPlaceable = actual.single()
            .measure(constraints)
        val scrimPlaceable = scrim.single()
            .measure(Constraints.fixed(width = constraints.maxWidth, height = scrimHeight))

        val currentHeight = shrunkHeight + scrimHeight + (heightDelta.coerceAtLeast(0) * animProgress).roundToInt()
        layout(constraints.maxWidth, currentHeight) {
            actualPlaceable.place(0, 0)

            val scrimY = currentHeight - scrimHeight
            scrimPlaceable.place(0, scrimY)
        }
    }
}

private val DefaultTagChipModifier = Modifier.padding(vertical = 4.dp).height(32.dp)

@Composable
private fun TagsChip(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
        SuggestionChip(
            modifier = modifier,
            onClick = onClick,
            shape = MaterialTheme.shapes.small,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            colors = SuggestionChipDefaults.suggestionChipColors(
                labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
            label = { Text(text = text, style = MaterialTheme.typography.labelLarge) },
        )
    }
}

@Composable
private fun RowScope.MangaActionButton(
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .weight(1f)
            .heightIn(min = 64.dp),
        onLongClick = onLongClick,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp),
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = title,
                color = color,
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center,
            )
        }
    }
}
