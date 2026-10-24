package com.nilemnod.mangareader.core.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import com.nilemnod.mangareader.core.ui.i18n.stringResource
import dev.icerock.moko.resources.StringResource

data class EmptyScreenAction(
    val stringRes: StringResource,
    val icon: ImageVector,
    val onClick: () -> Unit,
)

enum class EmptyIllustration {
    NoResults,
    NoUpdates,
    Offline,
}

@Composable
fun EmptyScreen(
    stringRes: StringResource,
    modifier: Modifier = Modifier,
    title: String? = null,
    illustration: EmptyIllustration = EmptyIllustration.NoResults,
    actions: List<EmptyScreenAction>? = null,
) {
    EmptyScreen(
        message = stringResource(stringRes),
        modifier = modifier,
        title = title,
        illustration = illustration,
        actions = actions,
    )
}

@Composable
fun EmptyScreen(
    message: String,
    modifier: Modifier = Modifier,
    title: String? = null,
    illustration: EmptyIllustration = EmptyIllustration.NoResults,
    actions: List<EmptyScreenAction>? = null,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        EmptyIllustrationImage(
            illustration = illustration,
            modifier = Modifier.size(160.dp),
        )
        Spacer(Modifier.size(16.dp))
        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.size(8.dp))
        }
        Text(
            text = message,
            modifier = Modifier.widthIn(max = 320.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        if (!actions.isNullOrEmpty()) {
            Row(
                modifier = Modifier.padding(top = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                actions.fastForEachIndexed { index, action ->
                    if (index == 0) {
                        FilledTonalButton(onClick = action.onClick) {
                            Icon(
                                imageVector = action.icon,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(Modifier.size(8.dp))
                            Text(text = stringResource(action.stringRes))
                        }
                    } else {
                        TextButton(onClick = action.onClick) {
                            Text(text = stringResource(action.stringRes))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyIllustrationImage(
    illustration: EmptyIllustration,
    modifier: Modifier = Modifier,
) {
    val base = MaterialTheme.colorScheme.outline
    val accent = MaterialTheme.colorScheme.primary
    Canvas(modifier = modifier) {
        scale(size.minDimension / ILLUSTRATION_SIZE, pivot = Offset.Zero) {
            drawBook(base)
            when (illustration) {
                EmptyIllustration.NoResults -> {
                    drawCircle(accent, radius = 16f, center = Offset(86f, 84f), style = lineStroke)
                    drawLine(accent, Offset(98f, 96f), Offset(112f, 110f), STROKE, StrokeCap.Round)
                }
                EmptyIllustration.NoUpdates -> {
                    drawPolyline(accent, listOf(Offset(70f, 78f), Offset(80f, 88f), Offset(96f, 66f)))
                }
                EmptyIllustration.Offline -> {
                    drawArc(base, 221.5f, 97f, false, Offset(62f, 82.6f), Size(32f, 32f), style = lineStroke)
                    drawArc(base, 221.4f, 97.2f, false, Offset(70f, 93.3f), Size(16f, 16f), style = lineStroke)
                    drawCircle(accent, radius = 3f, center = Offset(78f, 104f))
                    drawLine(accent, Offset(66f, 62f), Offset(98f, 108f), STROKE, StrokeCap.Round)
                }
            }
        }
    }
}

private fun DrawScope.drawBook(color: Color) {
    drawRoundRect(
        color = color,
        topLeft = Offset(40f, 32f),
        size = Size(64f, 88f),
        cornerRadius = CornerRadius(8f),
        style = lineStroke,
    )
    val spine = Path().apply {
        moveTo(56f, 132f)
        lineTo(112f, 132f)
        arcTo(Rect(104f, 116f, 120f, 132f), 90f, -90f, false)
        lineTo(120f, 52f)
    }
    drawPath(spine, color, style = lineStroke)
    drawLine(color, Offset(54f, 32f), Offset(54f, 120f), STROKE, StrokeCap.Round)
}

private fun DrawScope.drawPolyline(color: Color, points: List<Offset>) {
    val path = Path().apply {
        moveTo(points.first().x, points.first().y)
        points.drop(1).forEach { lineTo(it.x, it.y) }
    }
    drawPath(path, color, style = lineStroke)
}

private const val ILLUSTRATION_SIZE = 160f
private const val STROKE = 4f
private val lineStroke = Stroke(width = STROKE, cap = StrokeCap.Round, join = StrokeJoin.Round)
