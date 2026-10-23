package com.nilemnod.mangareader.core.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun GridSkeleton(
    modifier: Modifier = Modifier,
    columns: GridCells = GridCells.Adaptive(104.dp),
    itemCount: Int = 18,
) {
    val fill = MaterialTheme.colorScheme.surfaceContainerHigh
    val shape = MaterialTheme.shapes.small
    LazyVerticalGrid(
        columns = columns,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        userScrollEnabled = false,
    ) {
        items(itemCount) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(2f / 3f)
                        .background(fill, shape),
                )
                Box(
                    Modifier
                        .fillMaxWidth(0.9f)
                        .height(10.dp)
                        .background(fill, MaterialTheme.shapes.extraSmall),
                )
                Box(
                    Modifier
                        .fillMaxWidth(0.6f)
                        .height(10.dp)
                        .background(fill, MaterialTheme.shapes.extraSmall),
                )
            }
        }
    }
}

@Composable
fun ListSkeleton(
    modifier: Modifier = Modifier,
    itemCount: Int = 10,
) {
    val fill = MaterialTheme.colorScheme.surfaceContainerHigh
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        userScrollEnabled = false,
    ) {
        items(itemCount) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Box(
                    Modifier
                        .size(width = 42.dp, height = 56.dp)
                        .background(fill, MaterialTheme.shapes.small),
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth(0.55f)
                            .height(14.dp)
                            .background(fill, MaterialTheme.shapes.extraSmall),
                    )
                    Box(
                        Modifier
                            .fillMaxWidth(0.35f)
                            .height(12.dp)
                            .background(fill, MaterialTheme.shapes.extraSmall),
                    )
                }
            }
        }
    }
}
