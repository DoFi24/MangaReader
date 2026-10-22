package com.nilemnod.mangareader.feature.reader.image

import android.content.Context
import coil3.ImageLoader
import coil3.decode.DataSource
import coil3.decode.ImageSource
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.fetch.SourceFetchResult
import coil3.key.Keyer
import coil3.memory.MemoryCache
import coil3.request.Options
import com.nilemnod.mangareader.feature.reader.model.PageSource

internal class PageSourceFetcher(
    private val data: PageSource,
    private val options: Options,
) : Fetcher {

    override suspend fun fetch(): FetchResult {
        return SourceFetchResult(
            source = ImageSource(source = data.open(), fileSystem = options.fileSystem),
            mimeType = null,
            dataSource = DataSource.DISK,
        )
    }

    class Factory : Fetcher.Factory<PageSource> {
        override fun create(data: PageSource, options: Options, imageLoader: ImageLoader): Fetcher {
            return PageSourceFetcher(data, options)
        }
    }
}

internal class PageSourceKeyer : Keyer<PageSource> {
    override fun key(data: PageSource, options: Options): String = data.key
}

fun createReaderImageLoader(context: Context): ImageLoader {
    return ImageLoader.Builder(context)
        .components {
            add(PageSourceKeyer())
            add(PageSourceFetcher.Factory())
        }
        .memoryCache {
            MemoryCache.Builder()
                .maxSizePercent(context, 0.25)
                .build()
        }
        .build()
}
