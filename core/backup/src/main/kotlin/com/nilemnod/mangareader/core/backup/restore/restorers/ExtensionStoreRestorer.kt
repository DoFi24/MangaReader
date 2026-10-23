package com.nilemnod.mangareader.core.backup.restore.restorers

import com.nilemnod.mangareader.core.backup.models.BackupExtensionStore
import com.nilemnod.mangareader.domain.extension.model.ExtensionStore
import com.nilemnod.mangareader.domain.extension.repository.ExtensionStoreRepository
import dev.zacsweers.metro.Inject

@Inject
class ExtensionStoreRestorer(
    private val extensionStoreRepository: ExtensionStoreRepository,
) {

    suspend operator fun invoke(
        backupStore: BackupExtensionStore,
    ) {
        extensionStoreRepository.upsert(
            ExtensionStore(
                indexUrl = backupStore.indexUrl,
                name = backupStore.name,
                badgeLabel = backupStore.badgeLabel ?: backupStore.name,
                signingKey = backupStore.signingKey,
                contact = ExtensionStore.Contact(
                    website = backupStore.contactWebsite,
                    discord = backupStore.contactDiscord,
                ),
                isLegacy = backupStore.isLegacy ?: true,
                extensionListUrl = backupStore.extensionListUrl,
            ),
        )
    }
}
