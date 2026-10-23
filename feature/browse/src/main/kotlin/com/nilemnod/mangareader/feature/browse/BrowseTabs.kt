package com.nilemnod.mangareader.feature.browse

import androidx.compose.runtime.Composable
import com.nilemnod.mangareader.core.ui.components.TabContent
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import java.util.concurrent.CopyOnWriteArrayList

class BrowseTabSlot(
    val content: TabContent,
    val searchQuery: String? = null,
    val onChangeSearchQuery: (String?) -> Unit = {},
)

interface BrowseTabContribution {

    @Composable
    fun slot(): BrowseTabSlot
}

object BrowseTabs {

    private val contributions = CopyOnWriteArrayList<BrowseTabContribution>()

    private val showRequests = Channel<BrowseTabContribution>(1, BufferOverflow.DROP_OLDEST)

    internal val showEvents: Flow<BrowseTabContribution> = showRequests.receiveAsFlow()

    internal fun all(): List<BrowseTabContribution> = contributions.toList()

    fun register(contribution: BrowseTabContribution) {
        contributions += contribution
    }

    fun show(contribution: BrowseTabContribution) {
        showRequests.trySend(contribution)
    }
}
