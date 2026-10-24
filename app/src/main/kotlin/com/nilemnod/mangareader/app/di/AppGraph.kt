package com.nilemnod.mangareader.app.di

import android.content.Context
import com.nilemnod.mangareader.app.App
import com.nilemnod.mangareader.app.data.download.DownloadWorker
import com.nilemnod.mangareader.app.data.library.LibraryUpdateWorker
import com.nilemnod.mangareader.app.data.library.MetadataUpdateWorker
import com.nilemnod.mangareader.app.data.notification.NotificationReceiver
import com.nilemnod.mangareader.app.ui.base.delegate.SecureActivityDelegateImpl
import com.nilemnod.mangareader.app.ui.main.MainActivity
import com.nilemnod.mangareader.app.ui.webview.WebViewActivity
import com.nilemnod.mangareader.app.util.CrashLogUtil
import com.nilemnod.mangareader.core.common.security.SecurityPreferences
import com.nilemnod.mangareader.core.metro.IsDebugBuild
import com.nilemnod.mangareader.core.network.JavaScriptEngine
import com.nilemnod.mangareader.core.network.NetworkHelper
import com.nilemnod.mangareader.core.reader.ReaderPreferences
import com.nilemnod.mangareader.core.ui.preference.UiPreferences
import com.nilemnod.mangareader.core.ui.preference.UiPreferencesProvider
import com.nilemnod.mangareader.domain.library.service.LibraryPreferences
import com.nilemnod.mangareader.domain.source.service.SourcePreferences
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.ViewModelGraph
import kotlinx.serialization.json.Json
import kotlinx.serialization.protobuf.ProtoBuf
import nl.adaptivity.xmlutil.serialization.XML

@DependencyGraph(
    scope = AppScope::class,
    bindingContainers = [AppBindings::class],
)
interface AppGraph : ViewModelGraph, UiPreferencesProvider {
    fun inject(app: App)
    fun inject(mainActivity: MainActivity)
    fun inject(webViewActivity: WebViewActivity)
    fun inject(libraryUpdateWorker: LibraryUpdateWorker)
    fun inject(metadataUpdateWorker: MetadataUpdateWorker)
    fun inject(downloadWorker: DownloadWorker)
    fun inject(notificationReceiver: NotificationReceiver)
    fun inject(notificationReceiver: SecureActivityDelegateImpl)

    val viewModelFactory: MetroViewModelFactory

    override val uiPreferences: UiPreferences
    val readerPreferences: ReaderPreferences
    val libraryPreferences: LibraryPreferences
    val sourcePreferences: SourcePreferences
    val securityPreferences: SecurityPreferences

    val crashLogUtil: CrashLogUtil

    val json: Json
    val protoBuf: ProtoBuf
    val xml: XML
    val networkHelper: NetworkHelper
    val javaScriptEngine: JavaScriptEngine

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(@Provides context: Context, @Provides @IsDebugBuild isDebugBuild: Boolean): AppGraph
    }
}
