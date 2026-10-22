package com.nilemnod.mangareader.app.ui.base.delegate

import android.app.Activity
import com.nilemnod.mangareader.app.domain.ui.model.AppTheme
import com.nilemnod.mangareader.app.R
import com.nilemnod.mangareader.app.di.appGraph

interface ThemingDelegate {
    fun applyAppTheme(activity: Activity)

    companion object {
        fun getThemeResIds(appTheme: AppTheme, isAmoled: Boolean): List<Int> {
            return buildList(2) {
                add(themeResources.getOrDefault(appTheme, R.style.Theme_MangaReader))
                if (isAmoled) add(R.style.ThemeOverlay_MangaReader_Amoled)
            }
        }
    }
}

class ThemingDelegateImpl : ThemingDelegate {
    override fun applyAppTheme(activity: Activity) {
        val uiPreferences = activity.appGraph.uiPreferences
        ThemingDelegate.getThemeResIds(uiPreferences.appTheme.get(), uiPreferences.themeDarkAmoled.get())
            .forEach(activity::setTheme)
    }
}

private val themeResources: Map<AppTheme, Int> = mapOf(
    AppTheme.MONET to R.style.Theme_MangaReader_Monet,
    AppTheme.CATPPUCCIN to R.style.Theme_MangaReader_Catppuccin,
    AppTheme.TOKYONIGHT to R.style.Theme_MangaReader_TokyoNight,
    AppTheme.GREEN_APPLE to R.style.Theme_MangaReader_GreenApple,
    AppTheme.LAVENDER to R.style.Theme_MangaReader_Lavender,
    AppTheme.MIDNIGHT_DUSK to R.style.Theme_MangaReader_MidnightDusk,
    AppTheme.MONOCHROME to R.style.Theme_MangaReader_Monochrome,
    AppTheme.NORD to R.style.Theme_MangaReader_Nord,
    AppTheme.STRAWBERRY_DAIQUIRI to R.style.Theme_MangaReader_StrawberryDaiquiri,
    AppTheme.TAKO to R.style.Theme_MangaReader_Tako,
    AppTheme.TEALTURQUOISE to R.style.Theme_MangaReader_TealTurquoise,
    AppTheme.YINYANG to R.style.Theme_MangaReader_YinYang,
    AppTheme.YOTSUBA to R.style.Theme_MangaReader_Yotsuba,
    AppTheme.TIDAL_WAVE to R.style.Theme_MangaReader_TidalWave,
)
