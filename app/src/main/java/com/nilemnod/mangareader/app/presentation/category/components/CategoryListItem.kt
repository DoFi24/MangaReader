package com.nilemnod.mangareader.app.presentation.category.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.nilemnod.mangareader.domain.category.model.Category
import com.nilemnod.mangareader.i18n.MR
import com.nilemnod.mangareader.icons.materialsymbols.MaterialSymbols
import com.nilemnod.mangareader.icons.materialsymbols.rounded.Delete
import com.nilemnod.mangareader.icons.materialsymbols.rounded.DragHandle
import com.nilemnod.mangareader.icons.materialsymbols.rounded.Edit
import com.nilemnod.mangareader.presentation.core.components.material.padding
import com.nilemnod.mangareader.presentation.core.i18n.stringResource
import sh.calvin.reorderable.ReorderableCollectionItemScope

@Composable
fun ReorderableCollectionItemScope.CategoryListItem(
    category: Category,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onRename)
                .padding(vertical = MaterialTheme.padding.small)
                .padding(
                    start = MaterialTheme.padding.small,
                    end = MaterialTheme.padding.medium,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = MaterialSymbols.Rounded.DragHandle,
                contentDescription = null,
                modifier = Modifier
                    .padding(MaterialTheme.padding.medium)
                    .draggableHandle(),
            )
            Text(
                text = category.name,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onRename) {
                Icon(
                    imageVector = MaterialSymbols.Rounded.Edit,
                    contentDescription = stringResource(MR.strings.action_rename_category),
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = MaterialSymbols.Rounded.Delete,
                    contentDescription = stringResource(MR.strings.action_delete),
                )
            }
        }
    }
}
