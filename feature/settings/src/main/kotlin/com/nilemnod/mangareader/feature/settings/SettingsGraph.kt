package com.nilemnod.mangareader.feature.settings

import android.content.Context
import com.nilemnod.mangareader.core.common.security.SecurityPreferences
import com.nilemnod.mangareader.core.metro.metroGraph
import com.nilemnod.mangareader.domain.backup.service.BackupPreferences
import com.nilemnod.mangareader.domain.base.service.AppStatePreferences
import com.nilemnod.mangareader.domain.base.service.ModePreferences
import com.nilemnod.mangareader.domain.category.interactor.GetCategories
import com.nilemnod.mangareader.domain.category.interactor.ResetCategoryFlags
import com.nilemnod.mangareader.domain.download.service.DownloadPreferences
import com.nilemnod.mangareader.domain.extension.interactor.GetExtensionStoreCountAsFlow
import com.nilemnod.mangareader.domain.library.service.LibraryPreferences
import com.nilemnod.mangareader.domain.library.service.LibraryUpdateLauncher
import com.nilemnod.mangareader.domain.source.service.SourcePreferences
import com.nilemnod.mangareader.domain.storage.service.StoragePreferences
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
}

val Context.settingsGraph: SettingsGraph
    get() = metroGraph<SettingsGraph>()
