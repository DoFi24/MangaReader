package com.nilemnod.mangareader.core.ui.activity

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.nilemnod.mangareader.core.metro.metroGraph

open class HostActivity : AppCompatActivity() {

    private val appActivityDelegate: AppActivityDelegate
        get() = metroGraph<ActivityGraph>().appActivityDelegate

    override fun attachBaseContext(newBase: Context) {
        val delegate = newBase.metroGraph<ActivityGraph>().appActivityDelegate
        super.attachBaseContext(delegate.wrapBaseContext(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        appActivityDelegate.applyTheme(this)
        super.onCreate(savedInstanceState)
    }

    fun registerSecureActivity(activity: AppCompatActivity) {
        appActivityDelegate.registerSecureActivity(activity)
    }
}
