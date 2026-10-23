package com.nilemnod.mangareader.core.mangaui

import android.content.Context
import androidx.compose.runtime.Composable
import com.nilemnod.mangareader.core.common.i18n.stringResource
import com.nilemnod.mangareader.core.ui.i18n.stringResource
import com.nilemnod.mangareader.domain.category.model.Category
import com.nilemnod.mangareader.i18n.MR
val Category.visualName: String
    @Composable
    get() = when {
        isSystemCategory -> stringResource(MR.strings.label_default)
        else -> name
    }

fun Category.visualName(context: Context): String =
    when {
        isSystemCategory -> context.stringResource(MR.strings.label_default)
        else -> name
    }
