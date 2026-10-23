package com.nilemnod.mangareader.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.nilemnod.mangareader.core.ui.R

@OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)
private fun golosFont(weight: FontWeight) = Font(
    resId = R.font.golos_text,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
)

val GolosText = FontFamily(
    golosFont(FontWeight.Normal),
    golosFont(FontWeight.Medium),
    golosFont(FontWeight.SemiBold),
    golosFont(FontWeight.Bold),
)

private fun style(size: Int, line: Int, weight: FontWeight, tracking: Float) = TextStyle(
    fontFamily = GolosText,
    fontSize = size.sp,
    lineHeight = line.sp,
    fontWeight = weight,
    letterSpacing = tracking.sp,
)

val AppTypography = Typography(
    displayLarge = style(57, 64, FontWeight.Normal, -0.25f),
    displayMedium = style(45, 52, FontWeight.Normal, 0f),
    displaySmall = style(36, 44, FontWeight.Medium, 0f),
    headlineLarge = style(32, 40, FontWeight.Medium, 0f),
    headlineMedium = style(28, 36, FontWeight.Medium, 0f),
    headlineSmall = style(24, 32, FontWeight.Medium, 0f),
    titleLarge = style(22, 28, FontWeight.Medium, 0f),
    titleMedium = style(16, 24, FontWeight.SemiBold, 0.15f),
    titleSmall = style(14, 20, FontWeight.SemiBold, 0.1f),
    bodyLarge = style(16, 24, FontWeight.Normal, 0.2f),
    bodyMedium = style(14, 20, FontWeight.Normal, 0.2f),
    bodySmall = style(12, 16, FontWeight.Normal, 0.3f),
    labelLarge = style(14, 20, FontWeight.SemiBold, 0.1f),
    labelMedium = style(12, 16, FontWeight.SemiBold, 0.4f),
    labelSmall = style(11, 16, FontWeight.SemiBold, 0.4f),
)

val TabularNumbers = TextStyle(fontFeatureSettings = "tnum")

val Typography.header: TextStyle
    @Composable
    get() = titleSmall.copy(
        color = MaterialTheme.colorScheme.primary,
    )
