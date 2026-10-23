@file:Suppress("NOTHING_TO_INLINE")

package com.nilemnod.mangareader.app.util.view

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionContext
import androidx.compose.runtime.CompositionLocalProvider
import com.nilemnod.mangareader.app.di.appGraph
import com.nilemnod.mangareader.core.ui.theme.MangaReaderTheme
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory

inline fun ComponentActivity.setComposeContent(
    parent: CompositionContext? = null,
    crossinline content: @Composable () -> Unit,
) {
    setContent(parent) {
        MangaReaderTheme {
            CompositionLocalProvider(
                LocalTextStyle provides MaterialTheme.typography.bodySmall,
                LocalContentColor provides MaterialTheme.colorScheme.onBackground,
                LocalMetroViewModelFactory provides appGraph.viewModelFactory,
            ) {
                content()
            }
        }
    }
}
