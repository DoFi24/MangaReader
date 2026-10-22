package com.nilemnod.mangareader.app.data.backup.create.creators

import dev.zacsweers.metro.Inject
import com.nilemnod.mangareader.app.data.backup.models.BackupSource
import eu.kanade.tachiyomi.source.Source
import com.nilemnod.mangareader.domain.source.service.SourceManager

@Inject
class SourcesBackupCreator(
    private val sourceManager: SourceManager,
) {

    suspend operator fun invoke(sourceIds: Set<Long>): List<BackupSource> {
        return sourceIds.map { sourceManager.getOrStub(it).toBackupSource() }
    }
}

private fun Source.toBackupSource() =
    BackupSource(
        name = this.name,
        sourceId = this.id,
    )
