package com.nilemnod.mangareader.core.ui.components.material

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.nilemnod.mangareader.core.ui.components.Pill
@Composable
fun TabText(text: String, badgeCount: Int? = null) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (badgeCount != null) {
            Pill(
                text = "$badgeCount",
                color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                fontSize = 11.sp,
            )
        }
    }
}
