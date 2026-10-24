package com.nilemnod.mangareader.feature.extensions.installer

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import com.hippo.unifile.UniFile
import com.nilemnod.mangareader.core.android.system.isPackageInstalled
import rikka.shizuku.ShizukuProvider

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
