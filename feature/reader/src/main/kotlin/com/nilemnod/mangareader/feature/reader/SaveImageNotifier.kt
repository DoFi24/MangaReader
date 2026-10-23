package com.nilemnod.mangareader.feature.reader

import android.net.Uri

interface SaveImageNotifier {

    fun onComplete(uri: Uri)

    fun onClear()

    fun onError(error: String?)
}
