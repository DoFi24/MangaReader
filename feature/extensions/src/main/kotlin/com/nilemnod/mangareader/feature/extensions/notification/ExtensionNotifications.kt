package com.nilemnod.mangareader.feature.extensions.notification

import android.app.Notification

interface ExtensionNotifications {

    fun promptUpdates(names: List<String>)

    fun dismissUpdates()

    fun installServiceNotification(): InstallServiceNotification
}

class InstallServiceNotification(
    val id: Int,
    val notification: Notification,
)
