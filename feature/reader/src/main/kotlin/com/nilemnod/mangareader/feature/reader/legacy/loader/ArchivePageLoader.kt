package com.nilemnod.mangareader.feature.reader.legacy.loader

import com.nilemnod.mangareader.core.archive.ArchiveReader
import com.nilemnod.mangareader.core.common.util.lang.compareToCaseInsensitiveNaturalOrder
import com.nilemnod.mangareader.core.common.util.system.ImageUtil
import com.nilemnod.mangareader.domain.source.api.Page
import com.nilemnod.mangareader.feature.reader.legacy.model.ReaderPage

/**
 * Loader used to load a chapter from an archive file.
 */
internal class ArchivePageLoader(private val reader: ArchiveReader) : PageLoader() {
    override var isLocal: Boolean = true

    override suspend fun getPages(): List<ReaderPage> = reader.useEntries { entries ->
        entries
            .filter { it.isFile && ImageUtil.isImage(it.name) { reader.getInputStream(it.name)!! } }
            .sortedWith { f1, f2 -> f1.name.compareToCaseInsensitiveNaturalOrder(f2.name) }
            .mapIndexed { i, entry ->
                ReaderPage(i).apply {
                    stream = { reader.getInputStream(entry.name)!! }
                    status = Page.State.Ready
                }
            }
            .toList()
    }

    override suspend fun loadPage(page: ReaderPage) {
        check(!isRecycled)
    }

    override fun recycle() {
        super.recycle()
        reader.close()
    }
}
