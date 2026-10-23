package com.nilemnod.mangareader.core.navigation

import android.content.Context
import android.content.Intent
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

sealed interface SharedIntent {

    data class Reader(
        val mangaId: Long?,
        val chapterId: Long?,
    ) : SharedIntent

    data class WebView(
        val url: String,
        val sourceId: Long? = null,
        val title: String? = null,
    ) : SharedIntent

    data class Manga(
        val mangaId: Long,
    ) : SharedIntent
}

object IntentRegistry {

    @PublishedApi
    internal val factories = ConcurrentHashMap<KClass<out SharedIntent>, (Context, SharedIntent) -> Intent>()

    inline fun <reified T : SharedIntent> register(noinline factory: (Context, T) -> Intent) {
        @Suppress("UNCHECKED_CAST")
        factories[T::class] = factory as (Context, SharedIntent) -> Intent
    }

    fun get(context: Context, intent: SharedIntent): Intent {
        val factory = factories[intent::class] ?: error("SharedIntent not registered: ${intent::class.simpleName}")
        return factory(context, intent)
    }
}

fun SharedIntent.intent(context: Context): Intent = IntentRegistry.get(context, this)
