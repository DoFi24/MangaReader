package com.nilemnod.mangareader.core.ui.activity

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo

@ContributesTo(AppScope::class)
interface ActivityGraph {
    val appActivityDelegate: AppActivityDelegate
}
