package com.nilemnod.mangareader.app.source

import android.content.Context
import com.nilemnod.mangareader.app.di.appGraph
import com.nilemnod.mangareader.app.source.compat.ExtensionContentSource
import com.nilemnod.mangareader.domain.source.api.ContentSource
import com.nilemnod.mangareader.source.local.LocalSource
import com.nilemnod.mangareader.source.local.isLocal
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

fun ContentSource.getNameForMangaInfo(): String {
    val preferences = Injekt.get<Context>().appGraph.sourcePreferences
    val enabledLanguages = preferences.enabledLanguages.get()
        .filterNot { it in listOf("all", "other") }
    val hasOneActiveLanguages = enabledLanguages.size == 1
    val isInEnabledLanguages = lang in enabledLanguages
    return when {
        // For edge cases where user disables a source they got manga of in their library.
        hasOneActiveLanguages && !isInEnabledLanguages -> displayName
        // Hide the language tag when only one language is used.
        hasOneActiveLanguages && isInEnabledLanguages -> name
        else -> displayName
    }
}

fun ContentSource.isLocalOrStub(): Boolean = isLocal() || isStub

fun ContentSource.asLocalSource(): LocalSource = (this as ExtensionContentSource).native as LocalSource
