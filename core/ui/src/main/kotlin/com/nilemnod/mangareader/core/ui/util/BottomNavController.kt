package com.nilemnod.mangareader.core.ui.util

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

object BottomNavController {

    private val events = Channel<Boolean>()

    val visibility: Flow<Boolean> = events.receiveAsFlow()

    suspend fun show(show: Boolean) {
        events.send(show)
    }
}
