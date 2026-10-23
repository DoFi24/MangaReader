package com.nilemnod.mangareader.app.data.backup

import android.content.Context
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import com.hippo.unifile.UniFile
import com.nilemnod.mangareader.app.R
import com.nilemnod.mangareader.app.data.notification.NotificationReceiver
import com.nilemnod.mangareader.app.data.notification.Notifications
import com.nilemnod.mangareader.app.util.system.cancelNotification
import com.nilemnod.mangareader.app.util.system.notificationBuilder
import com.nilemnod.mangareader.app.util.system.notify
import com.nilemnod.mangareader.core.android.storage.getUriCompat
import com.nilemnod.mangareader.core.backup.BackupNotifier
import com.nilemnod.mangareader.core.common.i18n.pluralStringResource
import com.nilemnod.mangareader.core.common.i18n.stringResource
import com.nilemnod.mangareader.core.common.security.SecurityPreferences
import com.nilemnod.mangareader.core.common.storage.displayablePath
import com.nilemnod.mangareader.i18n.MR
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import java.io.File
import kotlin.time.Duration.Companion.milliseconds
import com.nilemnod.mangareader.core.ui.R as UiR
import com.nilemnod.mangareader.icons.materialsymbols.R as MaterialSymbolsR

@Inject
@ContributesBinding(AppScope::class)
class AndroidBackupNotifier(
    private val context: Context,
    private val preferences: SecurityPreferences,
) : BackupNotifier {

    override val backupProgressId: Int = Notifications.ID_BACKUP_PROGRESS

    override val restoreProgressId: Int = Notifications.ID_RESTORE_PROGRESS

    private val progressNotificationBuilder = context.notificationBuilder(
        Notifications.CHANNEL_BACKUP_RESTORE_PROGRESS,
    ) {
        setLargeIcon(BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher))
        setSmallIcon(UiR.drawable.ic_app)
        setAutoCancel(false)
        setOngoing(true)
        setOnlyAlertOnce(true)
    }

    private val completeNotificationBuilder = context.notificationBuilder(
        Notifications.CHANNEL_BACKUP_RESTORE_COMPLETE,
    ) {
        setLargeIcon(BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher))
        setSmallIcon(UiR.drawable.ic_app)
        setAutoCancel(false)
    }

    private fun NotificationCompat.Builder.show(id: Int) {
        context.notify(id, build())
    }

    override fun showBackupProgress(): NotificationCompat.Builder {
        val builder = with(progressNotificationBuilder) {
            setContentTitle(context.stringResource(MR.strings.creating_backup))

            setProgress(0, 0, true)
        }

        builder.show(Notifications.ID_BACKUP_PROGRESS)

        return builder
    }

    override fun dismissBackupProgress() {
        context.cancelNotification(Notifications.ID_BACKUP_PROGRESS)
    }

    override fun showBackupError(error: String?) {
        context.cancelNotification(Notifications.ID_BACKUP_PROGRESS)

        with(completeNotificationBuilder) {
            setContentTitle(context.stringResource(MR.strings.creating_backup_error))
            setContentText(error)

            show(Notifications.ID_BACKUP_COMPLETE)
        }
    }

    override fun showBackupComplete(file: UniFile) {
        context.cancelNotification(Notifications.ID_BACKUP_PROGRESS)

        with(completeNotificationBuilder) {
            setContentTitle(context.stringResource(MR.strings.backup_created))
            setContentText(file.displayablePath)

            clearActions()
            addAction(
                MaterialSymbolsR.drawable.rounded_share,
                context.stringResource(MR.strings.action_share),
                NotificationReceiver.shareBackupPendingActivity(context, file.uri),
            )

            show(Notifications.ID_BACKUP_COMPLETE)
        }
    }

    override fun showRestoreProgress(
        content: String,
        progress: Int,
        maxAmount: Int,
        sync: Boolean,
    ): NotificationCompat.Builder {
        val builder = with(progressNotificationBuilder) {
            val contentTitle = if (sync) {
                context.stringResource(MR.strings.syncing_library)
            } else {
                context.stringResource(MR.strings.restoring_backup)
            }
            setContentTitle(contentTitle)

            if (!preferences.hideNotificationContent.get()) {
                setContentText(content)
            }

            setProgress(maxAmount, progress, false)
            setOnlyAlertOnce(true)

            clearActions()
            addAction(
                MaterialSymbolsR.drawable.rounded_close,
                context.stringResource(MR.strings.action_cancel),
                NotificationReceiver.cancelRestorePendingBroadcast(context, Notifications.ID_RESTORE_PROGRESS),
            )
        }

        builder.show(Notifications.ID_RESTORE_PROGRESS)

        return builder
    }

    override fun dismissRestoreProgress() {
        context.cancelNotification(Notifications.ID_RESTORE_PROGRESS)
    }

    override fun showRestoreError(error: String?) {
        context.cancelNotification(Notifications.ID_RESTORE_PROGRESS)

        with(completeNotificationBuilder) {
            setContentTitle(context.stringResource(MR.strings.restoring_backup_error))
            setContentText(error)

            show(Notifications.ID_RESTORE_COMPLETE)
        }
    }

    override fun showRestoreComplete(
        time: Long,
        errorCount: Int,
        path: String?,
        file: String?,
        sync: Boolean,
    ) {
        val contentTitle = if (sync) {
            context.stringResource(MR.strings.library_sync_complete)
        } else {
            context.stringResource(MR.strings.restore_completed)
        }

        context.cancelNotification(Notifications.ID_RESTORE_PROGRESS)

        val timeString = context.stringResource(
            MR.strings.restore_duration,
            time.milliseconds.inWholeMinutes,
            time.milliseconds.inWholeSeconds - (time.milliseconds.inWholeMinutes * 60),
        )

        with(completeNotificationBuilder) {
            setContentTitle(contentTitle)
            setContentText(
                context.pluralStringResource(
                    MR.plurals.restore_completed_message,
                    errorCount,
                    timeString,
                    errorCount,
                ),
            )

            clearActions()
            if (errorCount > 0 && !path.isNullOrEmpty() && !file.isNullOrEmpty()) {
                val destFile = File(path, file)
                val uri = destFile.getUriCompat(context)

                val errorLogIntent = NotificationReceiver.openErrorLogPendingActivity(context, uri)
                setContentIntent(errorLogIntent)
                addAction(
                    MaterialSymbolsR.drawable.rounded_filled_folder,
                    context.stringResource(MR.strings.action_show_errors),
                    errorLogIntent,
                )
            }

            show(Notifications.ID_RESTORE_COMPLETE)
        }
    }
}
