package com.nilemnod.mangareader.core.navigation

import androidx.compose.runtime.Composable
import com.nilemnod.mangareader.domain.manga.model.Manga
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

sealed interface SharedDialog {

    data class MangaCover(
        val mangaId: Long,
        val onDismissRequest: () -> Unit,
    ) : SharedDialog

    data class MigrateManga(
        val current: Manga,
        val target: Manga,
        val onClickTitle: () -> Unit,
        val onDismissRequest: () -> Unit,
    ) : SharedDialog
}

object DialogRegistry {

    private val factories = ConcurrentHashMap<KClass<out SharedDialog>, @Composable (SharedDialog) -> Unit>()

    fun <T : SharedDialog> register(type: KClass<T>, content: @Composable (T) -> Unit) {
        factories[type] = { dialog -> content(type.java.cast(dialog)!!) }
    }

    inline fun <reified T : SharedDialog> register(noinline content: @Composable (T) -> Unit) {
        register(T::class, content)
    }

    @Composable
    fun Content(dialog: SharedDialog) {
        val content = factories[dialog::class] ?: error("SharedDialog not registered: ${dialog::class.simpleName}")
        content(dialog)
    }
}

@Composable
fun SharedDialog.Content() = DialogRegistry.Content(this)
