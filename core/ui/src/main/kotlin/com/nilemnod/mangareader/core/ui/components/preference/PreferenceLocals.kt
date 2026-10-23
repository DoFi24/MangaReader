package com.nilemnod.mangareader.core.ui.components.preference

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.structuralEqualityPolicy
import androidx.compose.ui.unit.dp

val LocalPreferenceHighlighted = compositionLocalOf(structuralEqualityPolicy()) { false }
val LocalPreferenceMinHeight = compositionLocalOf(structuralEqualityPolicy()) { 56.dp }
