package com.nilemnod.mangareader.source.mangadex

import com.nilemnod.mangareader.core.network.NetworkHelper
import com.nilemnod.mangareader.domain.source.api.ContentSource
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Provides

@ContributesTo(AppScope::class)
interface MangaDexProvider {

    @Provides
    @IntoSet
    fun mangaDexEnglish(network: NetworkHelper): ContentSource = MangaDexSource(network, "en")

    @Provides
    @IntoSet
    fun mangaDexRussian(network: NetworkHelper): ContentSource = MangaDexSource(network, "ru")
}
