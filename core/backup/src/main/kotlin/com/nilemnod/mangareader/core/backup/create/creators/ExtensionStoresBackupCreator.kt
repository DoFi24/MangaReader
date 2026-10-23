package com.nilemnod.mangareader.core.backup.create.creators

import com.nilemnod.mangareader.core.backup.models.BackupExtensionStore
import com.nilemnod.mangareader.core.backup.models.backupExtensionStoreMapper
import com.nilemnod.mangareader.domain.extension.interactor.GetExtensionStores
import dev.zacsweers.metro.Inject

@Inject
class ExtensionStoresBackupCreator(
    private val getExtensionStores: GetExtensionStores,
) {

    suspend operator fun invoke(): List<BackupExtensionStore> {
        return getExtensionStores.get()
            .map(backupExtensionStoreMapper)
    }
}
