package com.nilemnod.mangareader.feature.settings

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.nilemnod.mangareader.core.ui.i18n.stringResource
import com.nilemnod.mangareader.core.ui.util.Screen
import com.nilemnod.mangareader.core.ui.util.collectAsState
import com.nilemnod.mangareader.feature.settings.SettingsScreen
import com.nilemnod.mangareader.feature.settings.onboarding.OnboardingScreen
import com.nilemnod.mangareader.feature.settings.screen.SearchableSettings
import com.nilemnod.mangareader.feature.settings.settingsGraph
import com.nilemnod.mangareader.i18n.MR
class OnboardingScreen : Screen() {

    @Composable
    override fun Content() {
        val context = LocalContext.current
        val navigator = LocalNavigator.currentOrThrow

        val appStatePreferences = remember { context.settingsGraph.appStatePreferences }
        val shownOnboardingFlow by appStatePreferences.shownOnboardingFlow.collectAsState()

        val finishOnboarding: () -> Unit = {
            appStatePreferences.shownOnboardingFlow.set(true)
            navigator.pop()
        }

        val restoreSettingKey = stringResource(MR.strings.label_backup)

        BackHandler(enabled = !shownOnboardingFlow) {
            // Prevent exiting if onboarding hasn't been completed
        }

        OnboardingScreen(
            onComplete = finishOnboarding,
            onRestoreBackup = {
                finishOnboarding()
                SearchableSettings.highlightKey = restoreSettingKey
                navigator.push(SettingsScreen(SettingsScreen.Destination.DataAndStorage))
            },
        )
    }
}
