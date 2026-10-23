package com.nilemnod.mangareader.app.ui.reader.loader

import com.hippo.unifile.UniFile
import com.nilemnod.mangareader.app.ui.reader.model.ReaderPage
import com.nilemnod.mangareader.core.common.util.lang.compareToCaseInsensitiveNaturalOrder
import com.nilemnod.mangareader.core.common.util.system.ImageUtil
import com.nilemnod.mangareader.domain.source.api.Page

/**
 * Loader used to load a chapter from a directory given on [file].
 */
internal class DirectoryPageLoader(val file: UniFile) : PageLoader() {

    override var isLocal: Boolean = true

    override suspend fun getPages(): List<ReaderPage> {
        return file.listFiles()
            ?.filter { !it.isDirectory && ImageUtil.isImage(it.name) { it.openInputStream() } }
            ?.sortedWith { f1, f2 -> f1.name.orEmpty().compareToCaseInsensitiveNaturalOrder(f2.name.orEmpty()) }
            ?.mapIndexed { i, file ->
                val streamFn = { file.openInputStream() }
                ReaderPage(i).apply {
                    stream = streamFn
                    status = Page.State.Ready
                }
            }
            .orEmpty()
    }
}
