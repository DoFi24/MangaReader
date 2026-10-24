package com.nilemnod.mangareader.app.data.notification

import android.content.Context
import androidx.core.app.NotificationCompat
import com.nilemnod.mangareader.app.util.system.cancelNotification
import com.nilemnod.mangareader.app.util.system.notificationBuilder
import com.nilemnod.mangareader.app.util.system.notify
import com.nilemnod.mangareader.core.common.i18n.pluralStringResource
import com.nilemnod.mangareader.core.common.i18n.stringResource
import com.nilemnod.mangareader.core.common.security.SecurityPreferences
import com.nilemnod.mangareader.feature.extensions.notification.ExtensionNotifications
import com.nilemnod.mangareader.feature.extensions.notification.InstallServiceNotification
import com.nilemnod.mangareader.i18n.MR
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import com.nilemnod.mangareader.core.ui.R as UiR
import com.nilemnod.mangareader.icons.materialsymbols.R as MaterialSymbolsR

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class AndroidExtensionNotifications(
    private val context: Context,
    private val securityPreferences: SecurityPreferences,
) : ExtensionNotifications {

    override fun promptUpdates(names: List<String>) {
        context.notify(
            Notifications.ID_UPDATES_TO_EXTS,
            Notifications.CHANNEL_EXTENSIONS_UPDATE,
        ) {
            setContentTitle(
                context.pluralStringResource(
                    MR.plurals.update_check_notification_ext_updates,
                    names.size,
                    names.size,
                ),
            )
            if (!securityPreferences.hideNotificationContent.get()) {
                val extNames = names.joinToString(", ")
                setContentText(extNames)
                setStyle(NotificationCompat.BigTextStyle().bigText(extNames))
            }
            setSmallIcon(MaterialSymbolsR.drawable.rounded_filled_extension)
            setContentIntent(NotificationReceiver.openExtensionsPendingActivity(context))
            setAutoCancel(true)
        }
    }

    override fun dismissUpdates() {
        context.cancelNotification(Notifications.ID_UPDATES_TO_EXTS)
    }

    override fun installServiceNotification(): InstallServiceNotification {
        val notification = context.notificationBuilder(Notifications.CHANNEL_EXTENSIONS_UPDATE) {
            setSmallIcon(UiR.drawable.ic_app)
            setAutoCancel(false)
            setOngoing(true)
            setShowWhen(false)
            setContentTitle(context.stringResource(MR.strings.ext_install_service_notif))
            setProgress(100, 100, true)
        }.build()
        return InstallServiceNotification(Notifications.ID_EXTENSION_INSTALLER, notification)
    }
}
