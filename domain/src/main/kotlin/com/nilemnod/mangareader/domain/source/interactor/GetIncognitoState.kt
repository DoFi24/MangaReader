package com.nilemnod.mangareader.domain.source.interactor

import com.nilemnod.mangareader.domain.base.service.ModePreferences
import com.nilemnod.mangareader.domain.source.service.SourceManager
import com.nilemnod.mangareader.domain.source.service.SourcePreferences
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@Inject
class GetIncognitoState(
    private val modePreferences: ModePreferences,
    private val sourcePreferences: SourcePreferences,
    private val sourceManager: SourceManager,
) {
    suspend fun await(sourceId: Long?): Boolean {
        if (modePreferences.incognitoMode.get()) return true
        if (sourceId == null) return false
        val origin = sourceManager.get(sourceId)?.origin ?: return false

        return origin in sourcePreferences.incognitoExtensions.get()
    }

    fun subscribe(sourceId: Long?): Flow<Boolean> {
        if (sourceId == null) return modePreferences.incognitoMode.changes()

        return combine(
            modePreferences.incognitoMode.changes(),
            sourcePreferences.incognitoExtensions.changes(),
            sourceManager.sources.map { sources -> sources.find { it.id == sourceId }?.origin },
        ) { incognito, incognitoExtensions, origin ->
            incognito || (origin in incognitoExtensions)
        }
            .distinctUntilChanged()
    }
}
