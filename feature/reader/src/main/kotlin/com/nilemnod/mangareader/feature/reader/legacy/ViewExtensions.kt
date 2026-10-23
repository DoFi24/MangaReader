package com.nilemnod.mangareader.feature.reader.legacy

import android.content.res.Resources
import android.graphics.Rect
import android.view.View
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.nilemnod.mangareader.core.ui.theme.MangaReaderTheme

fun ComposeView.setComposeContent(
    content: @Composable () -> Unit,
) {
    setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
    setContent {
        MangaReaderTheme {
            CompositionLocalProvider(
                LocalTextStyle provides MaterialTheme.typography.bodySmall,
                LocalContentColor provides MaterialTheme.colorScheme.onBackground,
            ) {
                content()
            }
        }
    }
}

fun View?.isVisibleOnScreen(): Boolean {
    if (this == null) {
        return false
    }
    if (!this.isShown) {
        return false
    }
    val actualPosition = Rect()
    this.getGlobalVisibleRect(actualPosition)
    val screen =
        Rect(0, 0, Resources.getSystem().displayMetrics.widthPixels, Resources.getSystem().displayMetrics.heightPixels)
    return actualPosition.intersect(screen)
}
