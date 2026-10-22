package com.nilemnod.mangareader.app.data.backup.create.creators

import dev.zacsweers.metro.Inject
import com.nilemnod.mangareader.app.data.backup.models.BackupExtensionStore
import com.nilemnod.mangareader.app.data.backup.models.backupExtensionStoreMapper
import com.nilemnod.mangareader.domain.extension.interactor.GetExtensionStores

@Inject
class ExtensionStoresBackupCreator(
    private val getExtensionStores: GetExtensionStores,
) {

    suspend operator fun invoke(): List<BackupExtensionStore> {
        return getExtensionStores.get()
            .map(backupExtensionStoreMapper)
    }
}
