package com.nilemnod.mangareader.domain.extension.service

import com.nilemnod.mangareader.domain.extension.model.Extension
import com.nilemnod.mangareader.domain.extension.model.InstallStep
import kotlinx.coroutines.flow.Flow

interface ExtensionService {

    val loadedExtensionsFlow: Flow<List<Extension.Loaded>>

    val notLoadedExtensionsFlow: Flow<List<Extension.NotLoaded>>

    val availableExtensionsFlow: Flow<List<Extension.Available>>

    val installerRequiresSystemPermission: Flow<Boolean>

    suspend fun findAvailableExtensions()

    fun installExtension(extension: Extension.Available): Flow<InstallStep>

    fun updateExtension(extension: Extension.Installed): Flow<InstallStep>

    fun cancelInstallUpdateExtension(extension: Extension)

    fun uninstallExtension(extension: Extension.Installed)

    fun trust(extension: Extension.NotLoaded)
}
