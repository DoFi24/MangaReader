package com.nilemnod.mangareader.core.common.util.system

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import androidx.core.content.getSystemService
import com.nilemnod.mangareader.core.common.i18n.stringResource
import com.nilemnod.mangareader.core.common.util.lang.truncateCenter
import com.nilemnod.mangareader.i18n.MR
import logcat.LogPriority

/**
 * Copies a string to clipboard
 *
 * @param label Label to show to the user describing the content
 * @param content the actual text to copy to the board
 */
fun Context.copyToClipboard(label: String, content: String) {
    if (content.isBlank()) return

    try {
        val clipboard = getSystemService<ClipboardManager>()!!
        clipboard.setPrimaryClip(ClipData.newPlainText(label, content))

        // Android 13 and higher shows a visual confirmation of copied contents
        // https://developer.android.com/about/versions/13/features/copy-paste
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            toast(stringResource(MR.strings.copied_to_clipboard, content.truncateCenter(50)))
        }
    } catch (e: Throwable) {
        logcat(LogPriority.ERROR, e)
        toast(MR.strings.clipboard_copy_error)
    }
}
