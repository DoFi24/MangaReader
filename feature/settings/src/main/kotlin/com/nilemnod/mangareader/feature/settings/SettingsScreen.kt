package com.nilemnod.mangareader.feature.settings

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.nilemnod.mangareader.core.ui.components.TwoPanelBox
import com.nilemnod.mangareader.core.ui.util.DefaultNavigatorScreenTransition
import com.nilemnod.mangareader.core.ui.util.LocalBackPress
import com.nilemnod.mangareader.core.ui.util.Screen
import com.nilemnod.mangareader.core.ui.util.isTabletUi
import com.nilemnod.mangareader.feature.settings.screen.SettingsAppearanceScreen
import com.nilemnod.mangareader.feature.settings.screen.SettingsMainScreen
class SettingsScreen(
    private val destination: Int? = null,
) : Screen() {

    constructor(destination: Destination) : this(destination.id)

    @Composable
    override fun Content() {
        val parentNavigator = LocalNavigator.currentOrThrow
        if (!isTabletUi()) {
            Navigator(
                screen = when (destination) {
                    null -> SettingsMainScreen
                    else -> SettingsEntries.destination(destination) ?: SettingsMainScreen
                },
                onBackPressed = null,
            ) {
                val pop: () -> Unit = {
                    if (it.canPop) {
                        it.pop()
                    } else {
                        parentNavigator.pop()
                    }
                }
                CompositionLocalProvider(LocalBackPress provides pop) {
                    DefaultNavigatorScreenTransition(navigator = it)
                }
            }
        } else {
            Navigator(
                screen = when (destination) {
                    null -> SettingsAppearanceScreen
                    else -> SettingsEntries.destination(destination) ?: SettingsAppearanceScreen
                },
                onBackPressed = null,
            ) {
                val insets = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal)
                TwoPanelBox(
                    modifier = Modifier
                        .windowInsetsPadding(insets)
                        .consumeWindowInsets(insets),
                    startContent = {
                        CompositionLocalProvider(LocalBackPress provides parentNavigator::pop) {
                            SettingsMainScreen.Content(twoPane = true)
                        }
                    },
                    endContent = { DefaultNavigatorScreenTransition(navigator = it) },
                )
            }
        }
    }

    sealed class Destination(val id: Int) {
        data object About : Destination(0)
        data object DataAndStorage : Destination(1)
    }
}
