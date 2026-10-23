package com.nilemnod.mangareader.core.ui.theme

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically

object Motion {
    const val FAST = 150
    const val MEDIUM = 200
    const val SLOW = 300

    val Standard: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    val EmphasizedDecelerate: Easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
    val EmphasizedAccelerate: Easing = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)
}

object Transitions {

    fun fadeThroughIn(): EnterTransition = fadeIn(
        tween(Motion.MEDIUM, delayMillis = 50, easing = Motion.Standard),
    )

    fun fadeThroughOut(): ExitTransition = fadeOut(tween(Motion.FAST, easing = Motion.Standard))

    fun chromeTopIn(offsetPx: Int): EnterTransition =
        fadeIn(tween(Motion.MEDIUM, easing = Motion.EmphasizedDecelerate)) +
            slideInVertically(tween(Motion.MEDIUM, easing = Motion.EmphasizedDecelerate)) { -offsetPx }

    fun chromeTopOut(offsetPx: Int): ExitTransition =
        fadeOut(tween(Motion.FAST, easing = Motion.EmphasizedAccelerate)) +
            slideOutVertically(tween(Motion.FAST, easing = Motion.EmphasizedAccelerate)) { -offsetPx }

    fun chromeBottomIn(offsetPx: Int): EnterTransition =
        fadeIn(tween(Motion.MEDIUM, easing = Motion.EmphasizedDecelerate)) +
            slideInVertically(tween(Motion.MEDIUM, easing = Motion.EmphasizedDecelerate)) { offsetPx }

    fun chromeBottomOut(offsetPx: Int): ExitTransition =
        fadeOut(tween(Motion.FAST, easing = Motion.EmphasizedAccelerate)) +
            slideOutVertically(tween(Motion.FAST, easing = Motion.EmphasizedAccelerate)) { offsetPx }
}
