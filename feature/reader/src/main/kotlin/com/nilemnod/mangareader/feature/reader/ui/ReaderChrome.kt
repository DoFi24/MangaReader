package com.nilemnod.mangareader.feature.reader.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.nilemnod.mangareader.core.ui.i18n.stringResource
import com.nilemnod.mangareader.core.ui.theme.TabularNumbers
import com.nilemnod.mangareader.i18n.MR
import com.nilemnod.mangareader.icons.custommaterialsymbols.CustomMaterialSymbols
import com.nilemnod.mangareader.icons.custommaterialsymbols.edge.Back
import com.nilemnod.mangareader.icons.custommaterialsymbols.edge.NextChapter
import com.nilemnod.mangareader.icons.custommaterialsymbols.edge.PreviousChapter
import com.nilemnod.mangareader.icons.custommaterialsymbols.edge.Settings
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReaderTopBar(
    title: String,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = title,
                maxLines = 1,
                style = MaterialTheme.typography.titleMedium,
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = CustomMaterialSymbols.Edge.Back,
                    contentDescription = null,
                )
            }
        },
        actions = {
            IconButton(onClick = onOpenSettings) {
                Icon(
                    imageVector = CustomMaterialSymbols.Edge.Settings,
                    contentDescription = stringResource(MR.strings.action_settings),
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
    )
}

@Composable
internal fun ReaderBottomBar(
    currentPage: Int,
    pageCount: Int,
    rightToLeft: Boolean,
    hasPrevious: Boolean,
    hasNext: Boolean,
    onSeek: (Int) -> Unit,
    onPreviousChapter: () -> Unit,
    onNextChapter: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val direction = if (rightToLeft) LayoutDirection.Rtl else LayoutDirection.Ltr
    Surface(
        modifier = modifier
            .navigationBarsPadding()
            .padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides direction) {
            Row(
                modifier = Modifier
                    .height(56.dp)
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                IconButton(onClick = onPreviousChapter, enabled = hasPrevious) {
                    Icon(
                        imageVector = CustomMaterialSymbols.Edge.PreviousChapter,
                        contentDescription = stringResource(MR.strings.action_previous_chapter),
                    )
                }
                Text(
                    text = "${currentPage + 1}",
                    style = MaterialTheme.typography.labelLarge.merge(TabularNumbers),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Slider(
                    value = currentPage.toFloat(),
                    onValueChange = { onSeek(it.toInt()) },
                    valueRange = 0f..(pageCount - 1).coerceAtLeast(1).toFloat(),
                    enabled = pageCount > 1,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "$pageCount",
                    style = MaterialTheme.typography.bodyMedium.merge(TabularNumbers),
                )
                IconButton(onClick = onNextChapter, enabled = hasNext) {
                    Icon(
                        imageVector = CustomMaterialSymbols.Edge.NextChapter,
                        contentDescription = stringResource(MR.strings.action_next_chapter),
                    )
                }
            }
        }
    }
}

@Composable
internal fun PageNumberBadge(
    currentPage: Int,
    pageCount: Int,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Text(
            text = "${currentPage + 1} / $pageCount",
            style = MaterialTheme.typography.labelLarge.merge(TabularNumbers),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
        )
    }
}
