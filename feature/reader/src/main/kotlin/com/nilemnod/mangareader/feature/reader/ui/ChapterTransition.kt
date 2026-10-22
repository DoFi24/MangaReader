package com.nilemnod.mangareader.feature.reader.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nilemnod.mangareader.core.ui.i18n.stringResource
import com.nilemnod.mangareader.i18n.MR
@Composable
internal fun ChapterTransition(
    finishedTitle: String,
    nextTitle: String?,
    onOpenNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(MR.strings.transition_finished),
            style = MaterialTheme.typography.labelLarge,
        )
        Text(
            text = finishedTitle,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(24.dp))
        if (nextTitle != null) {
            Text(
                text = stringResource(MR.strings.transition_next),
                style = MaterialTheme.typography.labelLarge,
            )
            Text(
                text = nextTitle,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onOpenNext) {
                Text(stringResource(MR.strings.action_next_chapter))
            }
        } else {
            Text(
                text = stringResource(MR.strings.transition_no_next),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
internal fun ChapterDivider(
    finishedTitle: String,
    nextTitle: String?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = stringResource(MR.strings.transition_finished),
            style = MaterialTheme.typography.labelLarge,
        )
        Text(
            text = finishedTitle,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        if (nextTitle != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(MR.strings.transition_next),
                style = MaterialTheme.typography.labelLarge,
            )
            Text(
                text = nextTitle,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
            )
        }
    }
}
