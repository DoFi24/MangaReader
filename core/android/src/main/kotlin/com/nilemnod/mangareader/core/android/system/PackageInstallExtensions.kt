package com.nilemnod.mangareader.core.android.system

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.net.toUri

fun Context.launchRequestPackageInstallsPermission() {
    Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
        data = "package:$packageName".toUri()
        startActivity(this)
    }
}
