package com.nilemnod.mangareader.core.ui.components.preference

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nilemnod.mangareader.core.ui.components.preference.PrefsHorizontalPadding
import com.nilemnod.mangareader.core.ui.components.preference.highlightBackground

@Composable
fun PreferenceGroupHeader(title: String, highlighted: Boolean = false) {
    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = Modifier
            .fillMaxWidth()
            .highlightBackground(highlighted)
            .heightIn(min = 48.dp)
            .padding(top = 16.dp, bottom = 8.dp),
    ) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = PrefsHorizontalPadding),
            style = MaterialTheme.typography.titleSmall,
        )
    }
}
