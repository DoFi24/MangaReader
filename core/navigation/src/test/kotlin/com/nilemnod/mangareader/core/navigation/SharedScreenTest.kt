package com.nilemnod.mangareader.core.navigation

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.registry.ScreenRegistry
import cafe.adriel.voyager.core.screen.Screen
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class SharedScreenTest {

    private class FakeScreen(val label: String) : Screen {
        @Composable
        override fun Content() = Unit
    }

    @Test
    fun `builds the registered screen with the provider arguments`() {
        ScreenRegistry {
            register<SharedScreen.Manga> { FakeScreen("manga ${it.mangaId} ${it.fromSource}") }
        }

        (SharedScreen.Manga(7, true).screen() as FakeScreen).label shouldBe "manga 7 true"
        (SharedScreen.Manga(8).screen() as FakeScreen).label shouldBe "manga 8 false"
    }

    @Test
    fun `fails clearly when a screen is not registered`() {
        shouldThrow<IllegalStateException> { SharedScreen.GlobalSearch("query").screen() }
    }
}
