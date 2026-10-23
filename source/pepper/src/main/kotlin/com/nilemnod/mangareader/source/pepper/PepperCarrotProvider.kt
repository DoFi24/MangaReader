package com.nilemnod.mangareader.source.pepper

import com.nilemnod.mangareader.core.network.NetworkHelper
import com.nilemnod.mangareader.domain.source.api.ContentSource
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Provides
import kotlinx.serialization.json.Json

@ContributesTo(AppScope::class)
interface PepperCarrotProvider {

    @Provides
    @IntoSet
    fun pepperCarrotEnglish(network: NetworkHelper, json: Json): ContentSource =
        PepperCarrotSource(network, json, "en")

    @Provides
    @IntoSet
    fun pepperCarrotRussian(network: NetworkHelper, json: Json): ContentSource =
        PepperCarrotSource(network, json, "ru")
}
