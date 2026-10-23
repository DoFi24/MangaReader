package com.nilemnod.mangareader.app.ui.base.activity

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import com.nilemnod.mangareader.app.R
import com.nilemnod.mangareader.app.ui.base.delegate.SecureActivityDelegateImpl
import com.nilemnod.mangareader.app.ui.base.delegate.ThemingDelegate
import com.nilemnod.mangareader.app.ui.base.delegate.ThemingDelegateImpl
import com.nilemnod.mangareader.core.ui.activity.AppActivityDelegate
import com.nilemnod.mangareader.core.ui.preference.UiPreferences
import com.nilemnod.mangareader.core.ui.util.system.prepareTabletUiContext
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

@Inject
@ContributesBinding(AppScope::class)
class AndroidAppActivityDelegate(
    private val uiPreferences: UiPreferences,
) : AppActivityDelegate {

    override fun wrapBaseContext(base: Context): Context = base.prepareTabletUiContext(uiPreferences)

    override fun applyTheme(activity: Activity) {
        ThemingDelegateImpl().applyAppTheme(activity)
    }

    override fun registerSecureActivity(activity: AppCompatActivity) {
        SecureActivityDelegateImpl().registerSecureActivity(activity)
    }

    override fun createThemedContext(base: Context, configuration: Configuration): Context {
        val wrappedContext = ContextThemeWrapper(base, R.style.Theme_MangaReader)
        wrappedContext.applyOverrideConfiguration(configuration)
        ThemingDelegate.getThemeResIds(uiPreferences.appTheme.get(), uiPreferences.themeDarkAmoled.get())
            .forEach { wrappedContext.theme.applyStyle(it, true) }
        return wrappedContext
    }
}
