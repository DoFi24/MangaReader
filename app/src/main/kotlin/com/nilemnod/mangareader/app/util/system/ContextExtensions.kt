package com.nilemnod.mangareader.app.util.system

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import com.hippo.unifile.UniFile
import com.nilemnod.mangareader.app.R
import com.nilemnod.mangareader.app.di.appGraph
import com.nilemnod.mangareader.app.ui.base.delegate.ThemingDelegate
import com.nilemnod.mangareader.core.android.system.isPackageInstalled
import com.nilemnod.mangareader.core.common.i18n.stringResource
import com.nilemnod.mangareader.core.common.util.lang.truncateCenter
import com.nilemnod.mangareader.core.common.util.system.DeviceUtil
import com.nilemnod.mangareader.core.common.util.system.logcat
import com.nilemnod.mangareader.core.common.util.system.toast
import com.nilemnod.mangareader.core.ui.preference.model.ThemeMode
import com.nilemnod.mangareader.core.ui.util.system.isNightMode
import com.nilemnod.mangareader.i18n.MR
import logcat.LogPriority
import rikka.shizuku.ShizukuProvider
import java.io.File

val Context.powerManager: PowerManager
    get() = getSystemService()!!

fun Context.createFileInCacheDir(name: String): File {
    val file = File(externalCacheDir, name)
    if (file.exists()) {
        file.delete()
    }
    file.createNewFile()
    return file
}

private val ReaderGrayBackgroundColor = Color.rgb(0x20, 0x21, 0x25)

/**
 * Background color of the reader for the given [readerTheme] preference value.
 *
 * Automatic (3) resolves against the current night mode, matching the activity background.
 */
fun Context.readerBackgroundColor(readerTheme: Int): Int = when (readerTheme) {
    0 -> Color.WHITE // White
    2 -> ReaderGrayBackgroundColor // Gray
    3 -> if (isNightMode()) ReaderGrayBackgroundColor else Color.WHITE // Automatic
    else -> Color.BLACK // Black
}

/**
 * Creates night mode Context depending on reader theme/background
 *
 * Context wrapping method obtained from AppCompatDelegateImpl
 * https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:appcompat/appcompat/src/main/java/androidx/appcompat/app/AppCompatDelegateImpl.java;l=348;drc=e28752c96fc3fb4d3354781469a1af3dbded4898
 */
fun Context.createReaderThemeContext(): Context {
    val graph = appGraph
    val preferences = graph.uiPreferences
    val readerPreferences = graph.readerPreferences
    val themeMode = preferences.themeMode.get()
    val isDarkBackground = when (readerPreferences.readerTheme.get()) {
        1, 2 -> true // Black, Gray
        3 -> when (themeMode) { // Automatic bg uses activity background by default
            ThemeMode.SYSTEM -> applicationContext.isNightMode()
            else -> themeMode == ThemeMode.DARK
        }
        else -> false // White
    }
    val expected = if (isDarkBackground) Configuration.UI_MODE_NIGHT_YES else Configuration.UI_MODE_NIGHT_NO
    if (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK != expected) {
        val overrideConf = Configuration()
        overrideConf.setTo(resources.configuration)
        overrideConf.uiMode = (overrideConf.uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or expected

        val wrappedContext = ContextThemeWrapper(this, R.style.Theme_MangaReader)
        wrappedContext.applyOverrideConfiguration(overrideConf)
        ThemingDelegate.getThemeResIds(preferences.appTheme.get(), preferences.themeDarkAmoled.get())
            .forEach { wrappedContext.theme.applyStyle(it, true) }
        return wrappedContext
    }
    return this
}

/**
 * Gets document size of provided [Uri]
 *
 * @return document size of [uri] or null if size can't be obtained
 */
fun Context.getUriSize(uri: Uri): Long? {
    return UniFile.fromUri(this, uri)?.length()?.takeIf { it >= 0 }
}

val Context.hasMiuiPackageInstaller get() = isPackageInstalled("com.miui.packageinstaller")

val Context.isShizukuInstalled: Boolean
    get() = try {
        packageManager.getPermissionInfo(ShizukuProvider.PERMISSION, 0)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }
