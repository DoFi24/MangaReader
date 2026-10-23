package com.nilemnod.mangareader.feature.settings.about

import android.content.Context
import android.text.format.DateFormat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.nilemnod.mangareader.core.android.lang.toDateTimestampString
import com.nilemnod.mangareader.core.common.AppBuild
import com.nilemnod.mangareader.core.common.util.system.copyToClipboard
import com.nilemnod.mangareader.core.common.util.system.logcat
import com.nilemnod.mangareader.core.common.util.system.toast
import com.nilemnod.mangareader.core.navigation.SharedScreen
import com.nilemnod.mangareader.core.navigation.screen
import com.nilemnod.mangareader.core.ui.components.AppBar
import com.nilemnod.mangareader.core.ui.components.ScrollbarLazyColumn
import com.nilemnod.mangareader.core.ui.components.material.Scaffold
import com.nilemnod.mangareader.core.ui.components.preference.TextPreferenceWidget
import com.nilemnod.mangareader.core.ui.i18n.stringResource
import com.nilemnod.mangareader.core.ui.preference.UiPreferences
import com.nilemnod.mangareader.core.ui.preference.appUiPreferences
import com.nilemnod.mangareader.core.ui.util.LocalBackPress
import com.nilemnod.mangareader.core.ui.util.Screen
import com.nilemnod.mangareader.core.ui.util.rememberIs24HourSystem
import com.nilemnod.mangareader.domain.release.interactor.GetApplicationRelease
import com.nilemnod.mangareader.feature.settings.more.LogoHeader
import com.nilemnod.mangareader.feature.settings.settingsGraph
import com.nilemnod.mangareader.feature.settings.update.NewUpdateScreen
import com.nilemnod.mangareader.i18n.MR
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import logcat.LogPriority
import java.util.Locale
import kotlin.time.Instant

object AboutScreen : Screen() {

    @Composable
    override fun Content() {
        val scope = rememberCoroutineScope()
        val context = LocalContext.current
        val uriHandler = LocalUriHandler.current
        val handleBack = LocalBackPress.current
        val navigator = LocalNavigator.currentOrThrow
        var isCheckingUpdates by remember { mutableStateOf(false) }
        val debugInfoProvider = remember { context.settingsGraph.debugInfoProvider }
        val is24HourSystem = rememberIs24HourSystem()

        Scaffold(
            topBar = { scrollBehavior ->
                AppBar(
                    title = stringResource(MR.strings.pref_category_about),
                    navigateUp = if (handleBack != null) handleBack::invoke else null,
                    scrollBehavior = scrollBehavior,
                )
            },
        ) { contentPadding ->
            ScrollbarLazyColumn(
                contentPadding = contentPadding,
            ) {
                item {
                    LogoHeader(
                        iconPadding = PaddingValues(vertical = 56.dp),
                    )
                }

                item {
                    TextPreferenceWidget(
                        title = stringResource(MR.strings.version),
                        // Keyed on the system 12/24h setting, which can change while the app is backgrounded.
                        // Date/time prefs only change in Appearance, which recreates this screen on return.
                        subtitle = remember(is24HourSystem) { getVersionName(context, withBuildDate = true) },
                        onPreferenceClick = {
                            val deviceInfo = debugInfoProvider.getDebugInfo()
                            context.copyToClipboard("Debug information", deviceInfo)
                        },
                    )
                }

                if (AppBuild.updaterEnabled) {
                    item {
                        TextPreferenceWidget(
                            title = stringResource(MR.strings.check_for_updates),
                            widget = {
                                AnimatedVisibility(visible = isCheckingUpdates) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(28.dp),
                                        strokeWidth = 3.dp,
                                    )
                                }
                            },
                            onPreferenceClick = {
                                if (!isCheckingUpdates) {
                                    scope.launch {
                                        isCheckingUpdates = true

                                        checkVersion(
                                            context = context,
                                            onAvailableUpdate = { result ->
                                                val updateScreen = NewUpdateScreen(
                                                    versionName = result.release.version,
                                                    changelogInfo = result.release.info,
                                                    releaseLink = result.release.releaseLink,
                                                    downloadLink = result.release.downloadLink,
                                                )
                                                navigator.push(updateScreen)
                                            },
                                            onFinish = {
                                                isCheckingUpdates = false
                                            },
                                        )
                                    }
                                }
                            },
                        )
                    }
                }

                if (!AppBuild.isDebug && AppBuild.updaterEnabled) {
                    item {
                        TextPreferenceWidget(
                            title = stringResource(MR.strings.whats_new),
                            onPreferenceClick = { uriHandler.openUri(AppBuild.releaseUrl) },
                        )
                    }
                }

                item {
                    TextPreferenceWidget(
                        title = stringResource(MR.strings.licenses),
                        onPreferenceClick = { navigator.push(SharedScreen.OpenSourceLicenses.screen()) },
                    )
                }
            }
        }
    }

    /**
     * Checks version and shows a user prompt if an update is available.
     */
    private suspend fun checkVersion(
        context: Context,
        onAvailableUpdate: (GetApplicationRelease.Result.NewUpdate) -> Unit,
        onFinish: () -> Unit,
    ) {
        val updateChecker = context.settingsGraph.appUpdateChecker
        withContext(Dispatchers.Main) {
            try {
                when (val result = updateChecker.checkForUpdate(forceCheck = true)) {
                    is GetApplicationRelease.Result.NewUpdate -> {
                        onAvailableUpdate(result)
                    }
                    is GetApplicationRelease.Result.NoNewUpdate -> {
                        context.toast(MR.strings.update_check_no_new_updates)
                    }
                    is GetApplicationRelease.Result.OsTooOld -> {
                        context.toast(MR.strings.update_check_eol)
                    }
                }
            } catch (e: Exception) {
                context.toast(e.message)
                logcat(LogPriority.ERROR, e)
            } finally {
                onFinish()
            }
        }
    }

    fun getVersionName(context: Context, withBuildDate: Boolean): String {
        return when {
            AppBuild.isDebug -> {
                "Debug ${AppBuild.info.commitSha}".let {
                    if (withBuildDate) {
                        "$it (${getFormattedBuildTime(context)})"
                    } else {
                        it
                    }
                }
            }
            AppBuild.isNightly -> {
                "Nightly r${AppBuild.info.commitCount}".let {
                    if (withBuildDate) {
                        "$it (${AppBuild.info.commitSha}, ${getFormattedBuildTime(context)})"
                    } else {
                        "$it (${AppBuild.info.commitSha})"
                    }
                }
            }
            else -> {
                val channel = if (AppBuild.isFoss) "FOSS" else "Stable"
                "$channel v${AppBuild.versionName}".let {
                    if (withBuildDate) {
                        "$it (${getFormattedBuildTime(context)})"
                    } else {
                        it
                    }
                }
            }
        }
    }

    fun getFormattedBuildTime(context: Context): String {
        return try {
            val uiPreferences = context.appUiPreferences
            Instant.parse(AppBuild.info.buildTime)
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .toDateTimestampString(
                    UiPreferences.dateTimeFormat(
                        uiPreferences.dateFormat.get(),
                        uiPreferences.timeFormat.get(),
                        Locale.getDefault(),
                        DateFormat.is24HourFormat(context),
                    ),
                )
        } catch (_: Exception) {
            AppBuild.info.buildTime
        }
    }
}
