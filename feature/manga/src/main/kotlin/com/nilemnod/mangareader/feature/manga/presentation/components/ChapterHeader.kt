package com.nilemnod.mangareader.feature.manga.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nilemnod.mangareader.core.ui.components.material.padding
import com.nilemnod.mangareader.core.ui.i18n.pluralStringResource
import com.nilemnod.mangareader.core.ui.i18n.stringResource
import com.nilemnod.mangareader.i18n.MR
@Composable
fun ChapterHeader(
    enabled: Boolean,
    chapterCount: Int?,
    missingChapterCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                enabled = enabled,
                onClick = onClick,
            )
            .heightIn(min = 48.dp)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.padding.extraSmall),
    ) {
        Text(
            text = if (chapterCount == null) {
                stringResource(MR.strings.chapters)
            } else {
                pluralStringResource(MR.plurals.manga_num_chapters, count = chapterCount, chapterCount)
            },
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )

        MissingChaptersWarning(missingChapterCount)
    }
}

@Composable
private fun MissingChaptersWarning(count: Int) {
    if (count == 0) {
        return
    }

    Text(
        text = pluralStringResource(MR.plurals.missing_chapters, count = count, count),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.error,
    )
}
