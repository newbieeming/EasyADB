package com.xmbest.screen.file

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.ddmlib.FileListingService
import com.xmbest.LocalDialogState
import com.xmbest.theme.CardShape
import com.xmbest.theme.ChipShape
import com.xmbest.theme.TextFieldShape
import com.xmbest.util.DialogUtil
import com.xmbest.util.InputDialogUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileContent(file: FileListingService.FileEntry, viewModel: FileViewModel) {
    val uiState = viewModel.uiState.collectAsState().value
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val dialogState = LocalDialogState.current
    
    var showRenameDialog by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier.fillMaxWidth().padding(start = 6.dp, end = 6.dp, bottom = 6.dp)
            .clip(CardShape).background(MaterialTheme.colors.surface)
            .hoverable(interactionSource)
            .combinedClickable(onDoubleClick = {
                if (file.isDirectory) {
                    viewModel.onEvent(
                        FileUiEvent.NavigateToPath(
                            viewModel.calculatePath(
                                uiState.parentPath,
                                file.name
                            )
                        )
                    )
                }
            }, onClick = {}).padding(vertical = 6.dp, horizontal = 12.dp)
    ) {
        val fileTypeInfo = viewModel.getFileTypeInfo(file.type)
        Icon(
            imageVector = fileTypeInfo.icon,
            contentDescription = "",
            tint = Color.White,
            modifier = Modifier.clip(ChipShape).background(MaterialTheme.colors.primary)
                .padding(8.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = file.name,
                    color = MaterialTheme.colors.onSurface,
                    style = TextStyle.Default.copy(fontSize = 18.sp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = viewModel.byte2Gb(file.size),
                    color = MaterialTheme.colors.onSurface.copy(0.6f),
                    style = TextStyle.Default.copy(fontSize = 12.sp),
                    modifier = Modifier
                        .padding(horizontal = 3.dp, vertical = 2.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = fileTypeInfo.text,
                    color = MaterialTheme.colors.onPrimary,
                    style = TextStyle.Default.copy(fontSize = 14.sp),
                    modifier = Modifier.clip(TextFieldShape)
                        .background(MaterialTheme.colors.primary)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = file.permissions,
                    color = MaterialTheme.colors.onSecondary,
                    style = TextStyle.Default.copy(fontSize = 14.sp),
                    modifier = Modifier.clip(TextFieldShape).background(MaterialTheme.colors.secondary)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = file.date + " " + file.time,
                    color = MaterialTheme.colors.onBackground,
                    style = TextStyle.Default.copy(fontSize = 14.sp)
                )
            }
        }

        // 悬浮时显示的操作按钮
        if (isHovered) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 重命名按钮
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                    tooltip = { Text(viewModel.getString("file.rename")) },
                    state = rememberTooltipState()
                ) {
                    IconButton(
                        onClick = {
                            showRenameDialog = true
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = viewModel.getString("file.rename"),
                            tint = MaterialTheme.colors.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // 导出按钮
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                    tooltip = { Text(viewModel.getString("file.export")) },
                    state = rememberTooltipState()
                ) {
                    IconButton(
                        onClick = {
                            viewModel.onEvent(FileUiEvent.DownloadFiles(listOf(file)))
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = viewModel.getString("file.export"),
                            tint = MaterialTheme.colors.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // 删除按钮
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                    tooltip = { Text(viewModel.getString("file.delete")) },
                    state = rememberTooltipState()
                ) {
                    IconButton(
                        onClick = {
                            DialogUtil.showWarning(
                                dialogState = dialogState,
                                message = viewModel.getString("file.delete.confirm").format(file.absolutePath),
                                onConfirm = {
                                    viewModel.onEvent(FileUiEvent.DeleteFiles(listOf(file)))
                                },
                                onCancel = {}
                            )
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = viewModel.getString("file.delete"),
                            tint = MaterialTheme.colors.error,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
    
    // 重命名对话框
    if (showRenameDialog) {
        InputDialogUtil.showRenameDialog(
            dialogState = dialogState,
            title = viewModel.getString("file.rename"),
            currentName = file.name,
            onConfirm = { newName ->
                viewModel.onEvent(FileUiEvent.RenameFile(file.absolutePath, newName))
                showRenameDialog = false
            },
            onCancel = {
                showRenameDialog = false
            }
        )
    }
}