package com.nilemnod.mangareader.app.data.notification

import android.content.Context
import com.nilemnod.mangareader.feature.reader.ReaderNotifications
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

@Inject
@ContributesBinding(AppScope::class)
class AndroidReaderNotifications(
    private val context: Context,
) : ReaderNotifications {

    override fun dismissNewChapters(mangaId: Long) {
        NotificationReceiver.dismissNotification(context, mangaId.hashCode(), Notifications.ID_NEW_CHAPTERS)
    }
}
