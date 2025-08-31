package com.xmbest.screen.file

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.awtTransferable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.xmbest.theme.CardShape
import java.awt.datatransfer.DataFlavor

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun FileScreen(viewModel: FileViewModel = viewModel()) {
    val scrollState = rememberScrollState()
    val uiState = viewModel.uiState.collectAsState().value

    LaunchedEffect(UInt) {
        viewModel.onEvent(FileUiEvent.Refresh)
    }

    val dragAndDropTarget = remember {
        object : DragAndDropTarget {
            override fun onStarted(event: DragAndDropEvent) {
                val files = extractFilesFromEvent(event)
                if (files.isNotEmpty()) {
                    viewModel.onEvent(FileUiEvent.StartDrag(files))
                }
            }

            override fun onEnded(event: DragAndDropEvent) {
                viewModel.onEvent(FileUiEvent.DragEnd)
            }

            override fun onDrop(event: DragAndDropEvent): Boolean {
                val files = extractFilesFromEvent(event)
                if (files.isNotEmpty()) {
                    viewModel.onEvent(FileUiEvent.UploadFiles(files, uiState.parentPath))
                    return true
                }
                return false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .dragAndDropTarget(
                shouldStartDragAndDrop = { event ->
                    try {
                        val transferable = event.awtTransferable
                        transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor) ||
                                transferable.isDataFlavorSupported(DataFlavor.stringFlavor)
                    } catch (_: Exception) {
                        false
                    }
                },
                target = dragAndDropTarget
            )
    ) {
        LazyColumn(
            modifier = Modifier
                .padding(bottom = 6.dp)
                .scrollable(scrollState, Orientation.Vertical)
        ) {
            stickyHeader {
                FileHeader(viewModel)
            }
            items(uiState.children) {
                FileContent(it, viewModel)
            }
        }

        // 拖拽提示UI
        if (uiState.isDragging) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colors.primary.copy(alpha = 0.1f))
                    .padding(3.dp) // 添加内边距确保边框完全显示
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colors.primary,
                            shape = CardShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.uploadTipText,
                        color = MaterialTheme.colors.primary,
                        fontSize = 18.sp,
                        modifier = Modifier
                            .clip(CardShape)
                            .background(MaterialTheme.colors.surface.copy(alpha = 0.9f))
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}