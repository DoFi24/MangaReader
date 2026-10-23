package com.nilemnod.mangareader.feature.reader.legacy.model

import com.nilemnod.mangareader.core.download.DownloadStream
import com.nilemnod.mangareader.domain.source.api.Page
import java.io.InputStream

open class ReaderPage(
    index: Int,
    url: String = "",
    imageUrl: String? = null,
    var stream: (() -> InputStream)? = null,
) : Page(index, url, imageUrl, null) {

    open lateinit var chapter: ReaderChapter

    /** Set by a viewer before the download; filled during [Page.State.DownloadImage]. */
    @Volatile
    var downloadStream: DownloadStream? = null
}
