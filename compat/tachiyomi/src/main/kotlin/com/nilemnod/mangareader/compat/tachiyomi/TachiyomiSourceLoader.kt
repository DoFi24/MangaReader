package com.nilemnod.mangareader.compat.tachiyomi

import com.nilemnod.mangareader.domain.extension.service.ExtensionSourceLoader
import com.nilemnod.mangareader.domain.source.api.ContentSource
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import eu.kanade.tachiyomi.source.Source
import eu.kanade.tachiyomi.source.SourceFactory

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class TachiyomiSourceLoader : ExtensionSourceLoader {

    override fun load(
        classLoader: ClassLoader,
        pkgName: String,
        sourceClasses: String,
    ): List<ContentSource> {
        return sourceClasses
            .split(";")
            .map {
                val sourceClass = it.trim()
                if (sourceClass.startsWith(".")) pkgName + sourceClass else sourceClass
            }
            .flatMap { className ->
                when (val obj = Class.forName(className, false, classLoader).getDeclaredConstructor().newInstance()) {
                    is Source -> listOf(obj)
                    is SourceFactory -> obj.createSources()
                    else -> throw Exception("Unknown source class type: ${obj.javaClass}")
                }
            }
            .map { ExtensionContentSource(it, pkgName) }
    }
}
