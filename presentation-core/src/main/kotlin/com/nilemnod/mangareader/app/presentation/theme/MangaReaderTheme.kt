package com.nilemnod.mangareader.app.presentation.theme

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.nilemnod.mangareader.presentation.core.util.collectAsState
import androidx.compose.runtime.getValue
import com.nilemnod.mangareader.app.domain.ui.model.AppTheme
import com.nilemnod.mangareader.app.presentation.theme.colorscheme.BaseColorScheme
import com.nilemnod.mangareader.app.presentation.theme.colorscheme.CatppuccinColorScheme
import com.nilemnod.mangareader.app.presentation.theme.colorscheme.GreenAppleColorScheme
import com.nilemnod.mangareader.app.presentation.theme.colorscheme.LavenderColorScheme
import com.nilemnod.mangareader.app.presentation.theme.colorscheme.MidnightDuskColorScheme
import com.nilemnod.mangareader.app.presentation.theme.colorscheme.MonetColorScheme
import com.nilemnod.mangareader.app.presentation.theme.colorscheme.MonochromeColorScheme
import com.nilemnod.mangareader.app.presentation.theme.colorscheme.NordColorScheme
import com.nilemnod.mangareader.app.presentation.theme.colorscheme.StrawberryColorScheme
import com.nilemnod.mangareader.app.presentation.theme.colorscheme.MangaReaderColorScheme
import com.nilemnod.mangareader.app.presentation.theme.colorscheme.TakoColorScheme
import com.nilemnod.mangareader.app.presentation.theme.colorscheme.TealTurqoiseColorScheme
import com.nilemnod.mangareader.app.presentation.theme.colorscheme.TidalWaveColorScheme
import com.nilemnod.mangareader.app.presentation.theme.colorscheme.TokyoNightColorScheme
import com.nilemnod.mangareader.app.presentation.theme.colorscheme.YinYangColorScheme
import com.nilemnod.mangareader.app.presentation.theme.colorscheme.YotsubaColorScheme
import com.nilemnod.mangareader.app.domain.ui.appUiPreferences

@Composable
fun MangaReaderTheme(
    appTheme: AppTheme? = null,
    amoled: Boolean? = null,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val uiPreferences = remember { context.appUiPreferences }
    val fontScalePercent by uiPreferences.fontScalePercent.collectAsState()
    val density = LocalDensity.current
    val scaledDensity = remember(density, fontScalePercent) {
        Density(density.density, density.fontScale * fontScalePercent / PERCENT)
    }
    CompositionLocalProvider(LocalDensity provides scaledDensity) {
        BaseMangaReaderTheme(
            appTheme = appTheme ?: uiPreferences.appTheme.get(),
            isAmoled = amoled ?: uiPreferences.themeDarkAmoled.get(),
            content = content,
        )
    }
}

private const val PERCENT = 100f

@Composable
fun MangaReaderPreviewTheme(
    appTheme: AppTheme = AppTheme.DEFAULT,
    isAmoled: Boolean = false,
    content: @Composable () -> Unit,
) = BaseMangaReaderTheme(appTheme, isAmoled, content)

@Composable
private fun BaseMangaReaderTheme(
    appTheme: AppTheme,
    isAmoled: Boolean,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    MaterialExpressiveTheme(
        colorScheme = remember(appTheme, isDark, isAmoled) {
            getThemeColorScheme(
                context = context,
                appTheme = appTheme,
                isDark = isDark,
                isAmoled = isAmoled,
            )
        },
        content = content,
    )
}

private fun getThemeColorScheme(
    context: Context,
    appTheme: AppTheme,
    isDark: Boolean,
    isAmoled: Boolean,
): ColorScheme {
    val colorScheme = if (appTheme == AppTheme.MONET) {
        MonetColorScheme(context)
    } else {
        colorSchemes.getOrDefault(appTheme, MangaReaderColorScheme)
    }
    return colorScheme.getColorScheme(
        isDark = isDark,
        isAmoled = isAmoled,
        overrideDarkSurfaceContainers = appTheme != AppTheme.MONET,
    )
}

private val colorSchemes: Map<AppTheme, BaseColorScheme> = mapOf(
    AppTheme.DEFAULT to MangaReaderColorScheme,
    AppTheme.CATPPUCCIN to CatppuccinColorScheme,
    AppTheme.TOKYONIGHT to TokyoNightColorScheme,
    AppTheme.GREEN_APPLE to GreenAppleColorScheme,
    AppTheme.LAVENDER to LavenderColorScheme,
    AppTheme.MIDNIGHT_DUSK to MidnightDuskColorScheme,
    AppTheme.MONOCHROME to MonochromeColorScheme,
    AppTheme.NORD to NordColorScheme,
    AppTheme.STRAWBERRY_DAIQUIRI to StrawberryColorScheme,
    AppTheme.TAKO to TakoColorScheme,
    AppTheme.TEALTURQUOISE to TealTurqoiseColorScheme,
    AppTheme.TIDAL_WAVE to TidalWaveColorScheme,
    AppTheme.YINYANG to YinYangColorScheme,
    AppTheme.YOTSUBA to YotsubaColorScheme,
)
