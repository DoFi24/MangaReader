package com.nilemnod.mangareader.feature.settings

import android.content.Context
import com.nilemnod.mangareader.core.common.security.SecurityPreferences
import com.nilemnod.mangareader.core.download.ChapterCache
import com.nilemnod.mangareader.core.download.DownloadCache
import com.nilemnod.mangareader.core.metro.metroGraph
import com.nilemnod.mangareader.core.network.NetworkHelper
import com.nilemnod.mangareader.core.network.NetworkPreferences
import com.nilemnod.mangareader.core.reader.ReaderPreferences
import com.nilemnod.mangareader.domain.backup.service.BackupPreferences
import com.nilemnod.mangareader.domain.base.service.AppStatePreferences
import com.nilemnod.mangareader.domain.base.service.ModePreferences
import com.nilemnod.mangareader.domain.category.interactor.GetCategories
import com.nilemnod.mangareader.domain.category.interactor.ResetCategoryFlags
import com.nilemnod.mangareader.domain.download.service.DownloadPreferences
import com.nilemnod.mangareader.domain.extension.interactor.GetExtensionStoreCountAsFlow
import com.nilemnod.mangareader.domain.library.service.LibraryPreferences
import com.nilemnod.mangareader.domain.library.service.LibraryUpdateLauncher
import com.nilemnod.mangareader.domain.manga.interactor.GetFavorites
import com.nilemnod.mangareader.domain.manga.interactor.ResetViewerFlags
import com.nilemnod.mangareader.domain.source.service.SourcePreferences
import com.nilemnod.mangareader.domain.storage.service.StoragePreferences
import com.nilemnod.mangareader.feature.settings.about.DebugInfoProvider
import com.nilemnod.mangareader.feature.settings.screen.advanced.AdvancedSettingsGroups
import com.nilemnod.mangareader.feature.settings.screen.advanced.MetadataUpdateLauncher
import com.nilemnod.mangareader.feature.settings.update.AppUpdateChecker
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo

@ContributesTo(AppScope::class)
interface SettingsGraph {
    val libraryPreferences: LibraryPreferences
    val sourcePreferences: SourcePreferences
    val storagePreferences: StoragePreferences
    val securityPreferences: SecurityPreferences
    val downloadPreferences: DownloadPreferences
    val backupPreferences: BackupPreferences
    val modePreferences: ModePreferences
    val appStatePreferences: AppStatePreferences
    val getCategories: GetCategories
    val resetCategoryFlags: ResetCategoryFlags
    val getExtensionStoreCountAsFlow: GetExtensionStoreCountAsFlow
    val libraryUpdateLauncher: LibraryUpdateLauncher
    val debugInfoProvider: DebugInfoProvider
    val appUpdateChecker: AppUpdateChecker
    val chapterCache: ChapterCache
    val getFavorites: GetFavorites
    val networkPreferences: NetworkPreferences
    val networkHelper: NetworkHelper
    val downloadCache: DownloadCache
    val resetViewerFlags: ResetViewerFlags
    val metadataUpdateLauncher: MetadataUpdateLauncher
    val advancedSettingsGroups: AdvancedSettingsGroups
    val readerPreferences: ReaderPreferences
}

val Context.settingsGraph: SettingsGraph
    get() = metroGraph<SettingsGraph>()
