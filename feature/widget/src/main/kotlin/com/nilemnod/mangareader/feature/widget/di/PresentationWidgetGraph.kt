package com.nilemnod.mangareader.feature.widget.di

import com.nilemnod.mangareader.feature.widget.BaseUpdatesGridGlanceWidget
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo

@ContributesTo(AppScope::class)
interface PresentationWidgetGraph {
    fun inject(widget: BaseUpdatesGridGlanceWidget)
}
