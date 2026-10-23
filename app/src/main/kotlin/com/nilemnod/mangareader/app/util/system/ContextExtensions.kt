package com.nilemnod.mangareader.app.util.system

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.PowerManager
import androidx.core.content.getSystemService
import com.hippo.unifile.UniFile
import com.nilemnod.mangareader.core.android.system.isPackageInstalled
import rikka.shizuku.ShizukuProvider

val Context.powerManager: PowerManager
    get() = getSystemService()!!

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
