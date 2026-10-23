package com.nilemnod.mangareader.core.ui.activity

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity

interface AppActivityDelegate {

    fun wrapBaseContext(base: Context): Context

    fun applyTheme(activity: Activity)

    fun registerSecureActivity(activity: AppCompatActivity)

    fun createThemedContext(base: Context, configuration: Configuration): Context
}
