package com.nilemnod.mangareader.core.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.nilemnod.mangareader.core.ui.util.system.isTabletUi

@Composable
@ReadOnlyComposable
fun isTabletUi(): Boolean {
    return LocalConfiguration.current.isTabletUi()
}

@Composable
@ReadOnlyComposable
fun isMediumWidthWindow(): Boolean {
    val configuration = LocalConfiguration.current
    return configuration.screenWidthDp > MediumWidthWindowSize.value
}

@Composable
@ReadOnlyComposable
fun isExpandedWidthWindow(): Boolean {
    val configuration = LocalConfiguration.current
    return configuration.screenWidthDp > ExpandedWidthWindowSize.value
}

val MediumWidthWindowSize = 600.dp
val ExpandedWidthWindowSize = 840.dp
