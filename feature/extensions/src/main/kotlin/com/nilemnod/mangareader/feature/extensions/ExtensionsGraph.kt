package com.nilemnod.mangareader.feature.extensions

import android.content.Context
import com.nilemnod.mangareader.core.metro.metroGraph
import com.nilemnod.mangareader.domain.extension.repository.ExtensionStoreRepository
import com.nilemnod.mangareader.domain.extension.service.ExtensionSourceLoader
import com.nilemnod.mangareader.domain.source.service.SourcePreferences
import com.nilemnod.mangareader.feature.extensions.notification.ExtensionNotifications
import com.nilemnod.mangareader.feature.extensions.trust.TrustExtension
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo

@ContributesTo(AppScope::class)
interface ExtensionsGraph {
    val extensionManager: ExtensionManager
    val trustExtension: TrustExtension
    val sourcePreferences: SourcePreferences
    val extensionStoreRepository: ExtensionStoreRepository
    val extensionSourceLoader: ExtensionSourceLoader
    val extensionNotifications: ExtensionNotifications
}

val Context.extensionsGraph: ExtensionsGraph
    get() = metroGraph<ExtensionsGraph>()
