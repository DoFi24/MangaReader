package com.nilemnod.mangareader.core.backup.create.creators

import android.content.Context
import com.nilemnod.mangareader.core.backup.models.BackupPreference
import com.nilemnod.mangareader.core.backup.models.BackupSourcePreferences
import com.nilemnod.mangareader.core.backup.models.BooleanPreferenceValue
import com.nilemnod.mangareader.core.backup.models.FloatPreferenceValue
import com.nilemnod.mangareader.core.backup.models.IntPreferenceValue
import com.nilemnod.mangareader.core.backup.models.LongPreferenceValue
import com.nilemnod.mangareader.core.backup.models.StringPreferenceValue
import com.nilemnod.mangareader.core.backup.models.StringSetPreferenceValue
import com.nilemnod.mangareader.core.common.preference.Preference
import com.nilemnod.mangareader.core.common.preference.PreferenceStore
import com.nilemnod.mangareader.domain.source.service.SourceManager
import dev.zacsweers.metro.Inject

@Inject
class PreferenceBackupCreator(
    private val context: Context,
    private val sourceManager: SourceManager,
    private val preferenceStore: PreferenceStore,
) {

    fun createApp(includePrivatePreferences: Boolean): List<BackupPreference> {
        return preferenceStore.getAll().toBackupPreferences()
            .withPrivatePreferences(includePrivatePreferences)
    }

    suspend fun createSource(includePrivatePreferences: Boolean): List<BackupSourcePreferences> {
        return sourceManager.getAll()
            .mapNotNull { it.settings }
            .map {
                BackupSourcePreferences(
                    it.preferencesName,
                    context.getSharedPreferences(it.preferencesName, Context.MODE_PRIVATE).all.toBackupPreferences()
                        .withPrivatePreferences(includePrivatePreferences),
                )
            }
            .filter { it.prefs.isNotEmpty() }
    }

    @Suppress("UNCHECKED_CAST")
    private fun Map<String, *>.toBackupPreferences(): List<BackupPreference> {
        return this
            .filterKeys { !Preference.isAppState(it) }
            .mapNotNull { (key, value) ->
                when (value) {
                    is Int -> BackupPreference(key, IntPreferenceValue(value))
                    is Long -> BackupPreference(key, LongPreferenceValue(value))
                    is Float -> BackupPreference(key, FloatPreferenceValue(value))
                    is String -> BackupPreference(key, StringPreferenceValue(value))
                    is Boolean -> BackupPreference(key, BooleanPreferenceValue(value))
                    is Set<*> -> (value as? Set<String>)?.let {
                        BackupPreference(key, StringSetPreferenceValue(it))
                    }
                    else -> null
                }
            }
    }

    private fun List<BackupPreference>.withPrivatePreferences(include: Boolean) =
        if (include) {
            this
        } else {
            this.filter { !Preference.isPrivate(it.key) }
        }
}
