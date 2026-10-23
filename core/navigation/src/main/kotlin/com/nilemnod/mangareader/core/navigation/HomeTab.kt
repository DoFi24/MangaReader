package com.nilemnod.mangareader.core.navigation

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

sealed interface HomeTab {
    data class Library(val mangaIdToOpen: Long? = null) : HomeTab
    data object Updates : HomeTab
    data object History : HomeTab
    data class Browse(val toExtensions: Boolean = false) : HomeTab
    data class More(val toDownloads: Boolean) : HomeTab
}

object HomeTabRouter {

    private val requests = Channel<HomeTab>()

    val events: Flow<HomeTab> = requests.receiveAsFlow()

    suspend fun open(tab: HomeTab) {
        requests.send(tab)
    }
}
