package com.nilemnod.mangareader.feature.settings.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.nilemnod.mangareader.core.ui.components.material.padding
import com.nilemnod.mangareader.core.ui.i18n.stringResource
import com.nilemnod.mangareader.core.ui.screens.InfoScreen
import com.nilemnod.mangareader.i18n.MR
import com.nilemnod.mangareader.icons.custommaterialsymbols.CustomMaterialSymbols
import com.nilemnod.mangareader.icons.custommaterialsymbols.edgefilled.Library
import soup.compose.material.motion.animation.materialSharedAxisX
import soup.compose.material.motion.animation.rememberSlideDistance

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    onRestoreBackup: () -> Unit,
) {
    val slideDistance = rememberSlideDistance()

    var currentStep by rememberSaveable { mutableIntStateOf(0) }
    val steps = remember {
        listOf(
            ThemeStep(),
            StorageStep(),
            PermissionStep(),
            GuidesStep(onRestoreBackup = onRestoreBackup),
        )
    }
    val isLastStep = currentStep == steps.lastIndex

    BackHandler(enabled = currentStep != 0) {
        currentStep--
    }

    InfoScreen(
        icon = CustomMaterialSymbols.EdgeFilled.Library,
        headingText = stringResource(MR.strings.onboarding_heading),
        subtitleText = stringResource(MR.strings.onboarding_description),
        acceptText = stringResource(
            if (isLastStep) {
                MR.strings.onboarding_action_finish
            } else {
                MR.strings.onboarding_action_next
            },
        ),
        canAccept = steps[currentStep].isComplete,
        onAcceptClick = {
            if (isLastStep) {
                onComplete()
            } else {
                currentStep++
            }
        },
        rejectText = stringResource(MR.strings.onboarding_action_back).takeIf { currentStep > 0 },
        onRejectClick = { currentStep-- },
    ) {
        Row(
            modifier = Modifier.padding(vertical = MaterialTheme.padding.small),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            steps.indices.forEach { index ->
                val active = index == currentStep
                Box(
                    modifier = Modifier
                        .size(width = if (active) 24.dp else 8.dp, height = 8.dp)
                        .clip(CircleShape)
                        .background(
                            if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                        ),
                )
            }
        }
        Box(
            modifier = Modifier
                .padding(vertical = MaterialTheme.padding.small)
                .clip(MaterialTheme.shapes.medium)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceContainerLow),
        ) {
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    materialSharedAxisX(
                        forward = targetState > initialState,
                        slideDistance = slideDistance,
                    )
                },
                label = "stepContent",
            ) {
                steps[it].Content()
            }
        }
    }
}
