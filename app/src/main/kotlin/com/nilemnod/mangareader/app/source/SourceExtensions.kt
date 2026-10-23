package com.nilemnod.mangareader.app.source

import android.content.Context
import com.nilemnod.mangareader.app.di.appGraph
import com.nilemnod.mangareader.domain.source.api.ContentSource
import com.nilemnod.mangareader.domain.source.service.getNameForMangaInfo
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

fun ContentSource.getNameForMangaInfo(): String =
    getNameForMangaInfo(Injekt.get<Context>().appGraph.sourcePreferences)
