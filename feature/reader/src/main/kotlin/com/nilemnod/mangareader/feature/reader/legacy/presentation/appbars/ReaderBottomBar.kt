package com.nilemnod.mangareader.feature.reader.legacy.presentation.appbars

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.nilemnod.mangareader.core.reader.ReadingMode
import com.nilemnod.mangareader.core.ui.i18n.stringResource
import com.nilemnod.mangareader.i18n.MR
import com.nilemnod.mangareader.icons.custommaterialsymbols.CustomMaterialSymbols
import com.nilemnod.mangareader.icons.custommaterialsymbols.edge.CropOff
import com.nilemnod.mangareader.icons.custommaterialsymbols.edge.CropOn
import com.nilemnod.mangareader.icons.custommaterialsymbols.edge.Orientation
import com.nilemnod.mangareader.icons.custommaterialsymbols.edge.Settings

@Composable
fun ReaderBottomBar(
    readingMode: ReadingMode,
    onClickReadingMode: () -> Unit,
    onClickOrientation: () -> Unit,
    cropEnabled: Boolean,
    onClickCropBorder: () -> Unit,
    onClickSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .height(64.dp)
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .pointerInput(Unit) {}
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val tint = MaterialTheme.colorScheme.onSurfaceVariant
        IconButton(onClick = onClickReadingMode) {
            Icon(
                imageVector = readingMode.icon,
                contentDescription = stringResource(MR.strings.viewer),
                tint = tint,
            )
        }

        IconButton(onClick = onClickCropBorder) {
            Icon(
                imageVector = if (cropEnabled) {
                    CustomMaterialSymbols.Edge.CropOn
                } else {
                    CustomMaterialSymbols.Edge.CropOff
                },
                contentDescription = stringResource(MR.strings.pref_crop_borders),
                tint = if (cropEnabled) MaterialTheme.colorScheme.primary else tint,
            )
        }

        IconButton(onClick = onClickOrientation) {
            Icon(
                imageVector = CustomMaterialSymbols.Edge.Orientation,
                contentDescription = stringResource(MR.strings.rotation_type),
                tint = tint,
            )
        }

        IconButton(onClick = onClickSettings) {
            Icon(
                imageVector = CustomMaterialSymbols.Edge.Settings,
                contentDescription = stringResource(MR.strings.action_settings),
                tint = tint,
            )
        }
    }
}
