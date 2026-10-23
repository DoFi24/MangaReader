package com.nilemnod.mangareader.core.android.storage

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.nilemnod.mangareader.core.common.AppBuild
import java.io.File

fun File.getUriCompat(context: Context): Uri {
    return FileProvider.getUriForFile(context, AppBuild.applicationId + ".provider", this)
}

fun Context.createFileInCacheDir(name: String): File {
    val file = File(externalCacheDir, name)
    if (file.exists()) {
        file.delete()
    }
    file.createNewFile()
    return file
}
