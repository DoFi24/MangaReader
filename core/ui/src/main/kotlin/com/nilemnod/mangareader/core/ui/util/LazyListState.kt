package com.nilemnod.mangareader.core.ui.util

import androidx.compose.foundation.lazy.LazyListState

fun LazyListState.shouldExpandFAB(): Boolean = lastScrolledBackward || !canScrollForward || !canScrollBackward
