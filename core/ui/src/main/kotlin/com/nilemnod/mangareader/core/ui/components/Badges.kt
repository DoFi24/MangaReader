package com.nilemnod.mangareader.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun BadgeGroup(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        content()
    }
}

@Composable
fun Badge(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    textColor: Color = MaterialTheme.colorScheme.onPrimary,
    shape: Shape = MaterialTheme.shapes.extraSmall,
) {
    Text(
        text = text,
        modifier = modifier
            .clip(shape)
            .background(color)
            .heightIn(min = BadgeHeight)
            .widthIn(min = BadgeHeight)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        color = textColor,
        maxLines = 1,
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.labelSmall,
    )
}

@Composable
fun Badge(
    imageVector: ImageVector,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    iconColor: Color = MaterialTheme.colorScheme.onPrimary,
    shape: Shape = MaterialTheme.shapes.extraSmall,
) {
    val iconContentPlaceholder = "[icon]"
    val text = buildAnnotatedString {
        appendInlineContent(iconContentPlaceholder)
    }
    val inlineContent = mapOf(
        Pair(
            iconContentPlaceholder,
            InlineTextContent(
                Placeholder(
                    width = MaterialTheme.typography.labelSmall.fontSize,
                    height = MaterialTheme.typography.labelSmall.fontSize,
                    placeholderVerticalAlign = PlaceholderVerticalAlign.Center,
                ),
            ) {
                Icon(
                    imageVector = imageVector,
                    tint = iconColor,
                    contentDescription = null,
                )
            },
        ),
    )

    Text(
        text = text,
        inlineContent = inlineContent,
        modifier = modifier
            .clip(shape)
            .background(color)
            .heightIn(min = BadgeHeight)
            .padding(horizontal = 4.dp, vertical = 2.dp),
        color = iconColor,
        maxLines = 1,
        style = MaterialTheme.typography.labelSmall,
    )
}

private val BadgeHeight = 20.dp
