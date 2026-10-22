package com.nilemnod.mangareader.feature.reader.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.nilemnod.mangareader.i18n.MR
import com.nilemnod.mangareader.presentation.core.i18n.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PageActionsSheet(
    onSave: () -> Unit,
    onShare: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.navigationBarsPadding()) {
            ListItem(
                headlineContent = { Text(stringResource(MR.strings.action_save)) },
                modifier = Modifier.clickable {
                    onSave()
                    onDismiss()
                },
            )
            ListItem(
                headlineContent = { Text(stringResource(MR.strings.action_share)) },
                modifier = Modifier.clickable {
                    onShare()
                    onDismiss()
                },
            )
        }
    }
}
