package com.nilemnod.mangareader.feature.reader.legacy

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import com.nilemnod.mangareader.core.metro.metroGraph
import com.nilemnod.mangareader.core.ui.activity.ActivityGraph
import com.nilemnod.mangareader.core.ui.preference.model.ThemeMode
import com.nilemnod.mangareader.core.ui.util.system.isNightMode
import com.nilemnod.mangareader.feature.reader.legacy.di.ReaderGraph

private val ReaderGrayBackgroundColor = Color.rgb(0x20, 0x21, 0x25)

fun Context.readerBackgroundColor(readerTheme: Int): Int = when (readerTheme) {
    0 -> Color.WHITE
    2 -> ReaderGrayBackgroundColor
    3 -> if (isNightMode()) ReaderGrayBackgroundColor else Color.WHITE
    else -> Color.BLACK
}

fun Context.createReaderThemeContext(): Context {
    val graph = metroGraph<ReaderGraph>()
    val themeMode = graph.uiPreferences.themeMode.get()
    val isDarkBackground = when (graph.readerPreferences.readerTheme.get()) {
        1, 2 -> true
        3 -> when (themeMode) {
            ThemeMode.SYSTEM -> applicationContext.isNightMode()
            else -> themeMode == ThemeMode.DARK
        }
        else -> false
    }
    val expected = if (isDarkBackground) Configuration.UI_MODE_NIGHT_YES else Configuration.UI_MODE_NIGHT_NO
    if (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK != expected) {
        val overrideConf = Configuration()
        overrideConf.setTo(resources.configuration)
        overrideConf.uiMode = (overrideConf.uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or expected
        return metroGraph<ActivityGraph>().appActivityDelegate.createThemedContext(this, overrideConf)
    }
    return this
}
