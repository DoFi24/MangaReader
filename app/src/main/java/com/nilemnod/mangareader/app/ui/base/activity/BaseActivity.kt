package com.nilemnod.mangareader.app.ui.base.activity

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.nilemnod.mangareader.app.ui.base.delegate.SecureActivityDelegate
import com.nilemnod.mangareader.app.ui.base.delegate.SecureActivityDelegateImpl
import com.nilemnod.mangareader.app.ui.base.delegate.ThemingDelegate
import com.nilemnod.mangareader.app.ui.base.delegate.ThemingDelegateImpl
import com.nilemnod.mangareader.app.util.system.prepareTabletUiContext
import com.nilemnod.mangareader.app.di.appGraph

open class BaseActivity :
    AppCompatActivity(),
    SecureActivityDelegate by SecureActivityDelegateImpl(),
    ThemingDelegate by ThemingDelegateImpl() {

    override fun attachBaseContext(newBase: Context) {
        val uiPreferences = newBase.appGraph.uiPreferences
        super.attachBaseContext(newBase.prepareTabletUiContext(uiPreferences))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        applyAppTheme(this)
        super.onCreate(savedInstanceState)
    }
}
