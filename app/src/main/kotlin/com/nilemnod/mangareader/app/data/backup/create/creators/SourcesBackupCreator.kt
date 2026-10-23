package com.nilemnod.mangareader.app.data.backup.create.creators

import com.nilemnod.mangareader.app.data.backup.models.BackupSource
import com.nilemnod.mangareader.domain.source.api.ContentSource
import com.nilemnod.mangareader.domain.source.service.SourceManager
import dev.zacsweers.metro.Inject

@Inject
class SourcesBackupCreator(
    private val sourceManager: SourceManager,
) {

    suspend operator fun invoke(sourceIds: Set<Long>): List<BackupSource> {
        return sourceIds.map { sourceManager.contentOrStub(it).toBackupSource() }
    }
}

private fun ContentSource.toBackupSource() =
    BackupSource(
        name = this.name,
        sourceId = this.id,
    )
