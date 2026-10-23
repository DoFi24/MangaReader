package com.nilemnod.mangareader.data.database

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import com.nilemnod.mangareader.domain.database.repository.DatabaseRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class DatabaseRepositoryImpl(
    // Lazy since the driver reports its migration state here
    private val driver: Lazy<SqlDriver>,
) : DatabaseRepository {

    override val isMigrating = MutableStateFlow(false)

    override suspend fun awaitOpen() {
        withContext(Dispatchers.IO) {
            driver.value.executeQuery(null, "SELECT 1", { QueryResult.Value(Unit) }, 0).await()
        }
    }
}
