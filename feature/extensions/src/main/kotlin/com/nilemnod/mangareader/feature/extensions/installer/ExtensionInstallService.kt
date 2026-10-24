package com.nilemnod.mangareader.feature.extensions.installer

import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import com.nilemnod.mangareader.core.android.system.getSerializableExtraCompat
import com.nilemnod.mangareader.core.common.util.system.logcat
import com.nilemnod.mangareader.feature.extensions.extensionsGraph
import com.nilemnod.mangareader.feature.extensions.installer.ExtensionInstaller.Companion.EXTRA_DOWNLOAD_ID
import com.nilemnod.mangareader.feature.extensions.preference.InstallerType
import logcat.LogPriority

class ExtensionInstallService : Service() {

    private var installer: Installer? = null

    override fun onCreate() {
        val foreground = extensionsGraph.extensionNotifications.installServiceNotification()
        startForeground(foreground.id, foreground.notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val uri = intent?.data
        val id = intent?.getLongExtra(EXTRA_DOWNLOAD_ID, -1)?.takeIf { it != -1L }
        val installerUsed = intent?.getSerializableExtraCompat<InstallerType>(EXTRA_INSTALLER)
        if (uri == null || id == null || installerUsed == null) {
            stopSelf()
            return START_NOT_STICKY
        }

        if (installer == null) {
            installer = when (installerUsed) {
                InstallerType.PACKAGEINSTALLER -> PackageInstallerInstaller(this)
                InstallerType.SHIZUKU -> ShizukuInstaller(this)
                else -> {
                    logcat(LogPriority.ERROR) { "Not implemented for installer $installerUsed" }
                    stopSelf()
                    return START_NOT_STICKY
                }
            }
        }
        installer!!.addToQueue(id, uri)
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        installer?.onDestroy()
        installer = null
    }

    override fun onBind(i: Intent?): IBinder? = null

    companion object {
        private const val EXTRA_INSTALLER = "EXTRA_INSTALLER"

        fun getIntent(
            context: Context,
            downloadId: Long,
            uri: Uri,
            installer: InstallerType,
        ): Intent {
            return Intent(context, ExtensionInstallService::class.java)
                .setDataAndType(uri, ExtensionInstaller.APK_MIME)
                .putExtra(EXTRA_DOWNLOAD_ID, downloadId)
                .putExtra(EXTRA_INSTALLER, installer)
        }
    }
}
