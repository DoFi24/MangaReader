package com.nilemnod.mangareader.app.ui.manga

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nilemnod.mangareader.app.presentation.manga.components.EditCoverAction
import com.nilemnod.mangareader.app.presentation.manga.components.MangaCoverDialog
import com.nilemnod.mangareader.core.ui.screens.LoadingScreen
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel

@Composable
fun MangaCoverDialogHost(
    mangaId: Long,
    onDismissRequest: () -> Unit,
) {
    val context = LocalContext.current
    val vm = assistedMetroViewModel<MangaCoverViewModel, MangaCoverViewModel.Factory> {
        create(mangaId = mangaId)
    }
    val manga by vm.state.collectAsStateWithLifecycle()
    if (manga != null) {
        val getContent = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
            if (it == null) return@rememberLauncherForActivityResult
            vm.editCover(context, it)
        }
        MangaCoverDialog(
            manga = manga!!,
            snackbarHostState = vm.snackbarHostState,
            isCustomCover = remember(manga) { vm.hasCustomCover(manga!!) },
            onShareClick = { vm.shareCover(context) },
            onSaveClick = { vm.saveCover(context) },
            onEditClick = {
                when (it) {
                    EditCoverAction.EDIT -> getContent.launch("image/*")
                    EditCoverAction.DELETE -> vm.deleteCustomCover(context)
                    EditCoverAction.ADD_TO_LIBRARY -> vm.addToLibraryWarning(context)
                }
            },
            onDismissRequest = onDismissRequest,
        )
    } else {
        LoadingScreen(Modifier.systemBarsPadding())
    }
}
