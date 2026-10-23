package com.nilemnod.mangareader.app

import android.annotation.SuppressLint
import android.app.Application
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.webkit.WebView
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.preference.PreferenceManager
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.memory.MemoryCache
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.allowRgb565
import coil3.request.crossfade
import coil3.util.DebugLogger
import com.nilemnod.mangareader.app.crash.CrashActivity
import com.nilemnod.mangareader.app.crash.GlobalExceptionHandler
import com.nilemnod.mangareader.app.data.coil.BufferedSourceFetcher
import com.nilemnod.mangareader.app.data.coil.CoverRequestInterceptor
import com.nilemnod.mangareader.app.data.coil.ExtensionIconFetcher
import com.nilemnod.mangareader.app.data.coil.ImageDecoder
import com.nilemnod.mangareader.app.data.coil.InstalledExtensionIconKeyer
import com.nilemnod.mangareader.app.data.coil.InstalledSourceIconKeyer
import com.nilemnod.mangareader.app.data.coil.MangaCoverFetcher
import com.nilemnod.mangareader.app.data.coil.MangaCoverKeyer
import com.nilemnod.mangareader.app.data.coil.MangaKeyer
import com.nilemnod.mangareader.app.data.notification.Notifications
import com.nilemnod.mangareader.app.di.AppGraph
import com.nilemnod.mangareader.app.di.injekt.MetroInjektRegistrar
import com.nilemnod.mangareader.app.domain.base.BasePreferences
import com.nilemnod.mangareader.app.extension.ExtensionManager
import com.nilemnod.mangareader.app.navigation.AppNavigation
import com.nilemnod.mangareader.app.ui.base.delegate.SecureActivityDelegate
import com.nilemnod.mangareader.app.util.system.cancelNotification
import com.nilemnod.mangareader.app.util.system.isDebugBuildType
import com.nilemnod.mangareader.app.util.system.isReleaseBuildType
import com.nilemnod.mangareader.app.util.system.notify
import com.nilemnod.mangareader.core.android.system.animatorDurationScale
import com.nilemnod.mangareader.core.common.AppBuild
import com.nilemnod.mangareader.core.common.i18n.stringResource
import com.nilemnod.mangareader.core.common.preference.Preference
import com.nilemnod.mangareader.core.common.preference.PreferenceStore
import com.nilemnod.mangareader.core.common.util.system.DeviceUtil
import com.nilemnod.mangareader.core.common.util.system.WebViewUtil
import com.nilemnod.mangareader.core.common.util.system.logcat
import com.nilemnod.mangareader.core.metro.AppCoroutineScope
import com.nilemnod.mangareader.core.metro.GraphProvider
import com.nilemnod.mangareader.core.migration.Migration
import com.nilemnod.mangareader.core.migration.Migrator
import com.nilemnod.mangareader.core.network.NetworkHelper
import com.nilemnod.mangareader.core.network.NetworkPreferences
import com.nilemnod.mangareader.core.storage.CoverCache
import com.nilemnod.mangareader.core.ui.preference.UiPreferences
import com.nilemnod.mangareader.core.ui.preference.model.setAppCompatDelegateThemeMode
import com.nilemnod.mangareader.domain.source.service.SourceManager
import com.nilemnod.mangareader.feature.widget.WidgetManager
import com.nilemnod.mangareader.i18n.MR
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.createGraphFactory
import eu.kanade.tachiyomi.AppInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import logcat.AndroidLogcatLogger
import logcat.LogPriority
import logcat.LogcatLogger
import org.conscrypt.Conscrypt
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.InjektScope
import java.security.Security
import kotlin.concurrent.thread
import com.nilemnod.mangareader.icons.materialsymbols.R as MaterialSymbolsR

class App : Application(), DefaultLifecycleObserver, SingletonImageLoader.Factory, GraphProvider<AppGraph> {

    override val graph: AppGraph by lazy {
        createGraphFactory<AppGraph.Factory>().create(context = this, isDebugBuild = isDebugBuildType)
    }

    @Inject private lateinit var preferenceStore: PreferenceStore

    @Inject private lateinit var basePreferences: BasePreferences

    @Inject private lateinit var networkPreferences: NetworkPreferences

    @Inject private lateinit var uiPreferences: UiPreferences

    // Lazy: only the image loader uses these, and process starts without UI (workers, receivers) never build it
    @Inject private lateinit var coverCache: Lazy<CoverCache>

    @Inject private lateinit var networkHelper: Lazy<NetworkHelper>

    @Inject private lateinit var sourceManager: Lazy<SourceManager>

    @Inject private lateinit var extensionManager: Lazy<ExtensionManager>

    @Inject private lateinit var widgetManager: WidgetManager

    @Inject private lateinit var migrations: Set<Migration>

    @Inject @AppCoroutineScope
    private lateinit var scope: CoroutineScope

    private val disableIncognitoReceiver = DisableIncognitoReceiver()

    @SuppressLint("LaunchActivityFromNotification")
    override fun onCreate() {
        super<Application>.onCreate()

        AppInfo.init(BuildConfig.VERSION_CODE, BuildConfig.VERSION_NAME)
        AppBuild.init(isReleaseBuildType)

        // Must run before the graph is built, since injecting dependencies initializes WebView and the
        // suffix can't be set once a provider exists in the process. Secondary processes die otherwise.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val process = getProcessName()
            if (packageName != process) WebView.setDataDirectorySuffix(process)
        }

        // The graph and the code after it read the default preferences straight away. Loading them takes a disk
        // read, so start it on another thread while the graph is built; the first read then waits less or not at all
        thread(name = "PreferencesPreload") { PreferenceManager.getDefaultSharedPreferences(this) }
        // The first use of Dispatchers.Main finds its factory with a ServiceLoader, which reads the APK
        thread(name = "MainDispatcherPreload") { Dispatchers.Main }

        Injekt = InjektScope(MetroInjektRegistrar(application = this, graphProvider = this))
        graph.inject(this)

        AppNavigation.register()

        GlobalExceptionHandler.initialize(applicationContext, CrashActivity::class.java)

        // TLS 1.3 support for Android < 10
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            Security.insertProviderAt(Conscrypt.newProvider(), 1)
        }

        setupNotificationChannels()

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        // Show notification to disable Incognito Mode when it's enabled
        basePreferences.incognitoMode.changes()
            .onEach { enabled ->
                if (enabled) {
                    disableIncognitoReceiver.register()
                    notify(
                        Notifications.ID_INCOGNITO_MODE,
                        Notifications.CHANNEL_INCOGNITO_MODE,
                    ) {
                        setContentTitle(stringResource(MR.strings.pref_incognito_mode))
                        setContentText(stringResource(MR.strings.notification_incognito_text))
                        setSmallIcon(MaterialSymbolsR.drawable.rounded_eyeglasses_2)
                        setOngoing(true)

                        val pendingIntent = PendingIntent.getBroadcast(
                            this@App,
                            0,
                            Intent(ACTION_DISABLE_INCOGNITO_MODE).setPackage(BuildConfig.APPLICATION_ID),
                            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE,
                        )
                        setContentIntent(pendingIntent)
                    }
                } else {
                    disableIncognitoReceiver.unregister()
                    cancelNotification(Notifications.ID_INCOGNITO_MODE)
                }
            }
            .launchIn(scope)

        setAppCompatDelegateThemeMode(uiPreferences.themeMode.get())

        // Updates widget update
        widgetManager.init(scope)

        if (!LogcatLogger.isInstalled) {
            val minLogPriority = when {
                networkPreferences.verboseLogging.get() -> LogPriority.VERBOSE
                BuildConfig.DEBUG -> LogPriority.DEBUG
                else -> LogPriority.INFO
            }
            LogcatLogger.install()
            LogcatLogger.loggers += AndroidLogcatLogger(minLogPriority)
        }

        initializeMigrator()
    }

    private fun initializeMigrator() {
        val preference = preferenceStore.getInt(Preference.appStateKey("last_version_code"), 0)
        logcat {
            "Migration from ${preference.get()} to ${BuildConfig.VERSION_CODE} with ${migrations.size} migration(s)"
        }
        Migrator.initialize(
            old = preference.get(),
            new = BuildConfig.VERSION_CODE,
            migrations = migrations.toList(),
            onMigrationComplete = {
                logcat { "Updating last version to ${BuildConfig.VERSION_CODE}" }
                preference.set(BuildConfig.VERSION_CODE)
            },
        )
    }

    override fun newImageLoader(context: Context): ImageLoader {
        return ImageLoader.Builder(this).apply {
            val callFactoryLazy = lazy { networkHelper.value.client }
            val coverCache = coverCache.value
            val sourceManager = sourceManager.value
            components {
                // NetworkFetcher.Factory
                add(OkHttpNetworkFetcherFactory(callFactoryLazy::value))
                // Decoder.Factory
                add(ImageDecoder.Factory())
                // Fetcher.Factory
                add(BufferedSourceFetcher.Factory())
                add(MangaCoverFetcher.MangaCoverFactory(callFactoryLazy, coverCache, sourceManager))
                add(MangaCoverFetcher.MangaFactory(callFactoryLazy, coverCache, sourceManager))
                add(ExtensionIconFetcher.ExtensionFactory(context))
                add(ExtensionIconFetcher.SourceFactory(context, extensionManager))
                // Keyer
                add(MangaCoverKeyer(coverCache))
                add(MangaKeyer(coverCache))
                add(InstalledExtensionIconKeyer())
                add(InstalledSourceIconKeyer())
                // Interceptor
                add(CoverRequestInterceptor())
            }

            memoryCache(
                MemoryCache.Builder()
                    .maxSizePercent(context)
                    .build(),
            )

            crossfade((300 * this@App.animatorDurationScale).toInt())
            allowRgb565(DeviceUtil.isLowRamDevice(this@App))
            if (networkPreferences.verboseLogging.get()) logger(DebugLogger())

            // Coil spawns a new thread for every image load by default
            fetcherCoroutineContext(Dispatchers.IO.limitedParallelism(8))
            decoderCoroutineContext(Dispatchers.IO.limitedParallelism(3))
        }
            .build()
    }

    override fun onStart(owner: LifecycleOwner) {
        SecureActivityDelegate.onApplicationStart(this)
    }

    override fun onStop(owner: LifecycleOwner) {
        SecureActivityDelegate.onApplicationStopped(this)
    }

    override fun getPackageName(): String {
        try {
            // Override the value passed as X-Requested-With in WebView requests
            val stackTrace = Thread.currentThread().stackTrace
            val isChromiumCall = stackTrace.any { trace ->
                trace.className.lowercase() in setOf("org.chromium.base.buildinfo", "org.chromium.base.apkinfo") &&
                    trace.methodName.lowercase() in setOf("getall", "getpackagename", "<init>")
            }

            if (isChromiumCall) return WebViewUtil.spoofedPackageName(applicationContext)
        } catch (_: Exception) {
        }

        return super.getPackageName()
    }

    private fun setupNotificationChannels() {
        try {
            Notifications.createChannels(this)
        } catch (e: Exception) {
            logcat(LogPriority.ERROR, e) { "Failed to modify notification channels" }
        }
    }

    private inner class DisableIncognitoReceiver : BroadcastReceiver() {
        private var registered = false

        override fun onReceive(context: Context, intent: Intent) {
            basePreferences.incognitoMode.set(false)
        }

        fun register() {
            if (!registered) {
                ContextCompat.registerReceiver(
                    this@App,
                    this,
                    IntentFilter(ACTION_DISABLE_INCOGNITO_MODE),
                    ContextCompat.RECEIVER_NOT_EXPORTED,
                )
                registered = true
            }
        }

        fun unregister() {
            if (registered) {
                unregisterReceiver(this)
                registered = false
            }
        }
    }
}

private const val ACTION_DISABLE_INCOGNITO_MODE = "tachi.action.DISABLE_INCOGNITO_MODE"
