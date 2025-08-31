package com.xmbest.screen.file

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xmbest.FILE_SPLIT
import com.xmbest.ddmlib.ClipboardUtil
import com.xmbest.theme.ButtonShape

/**
 * 构建路径面包屑导航数据
 */
private fun buildPathParts(parentPath: String, rootLabel: String): List<Pair<String, String>> {
    val rootPath = listOf(Pair(rootLabel, FILE_SPLIT))
    
    if (parentPath == FILE_SPLIT) {
        return rootPath
    }
    
    val cleanPath = parentPath.removePrefix(FILE_SPLIT)
    val parts = cleanPath.split(FILE_SPLIT).filter { it.isNotEmpty() }
    val pathPairs = mutableListOf<Pair<String, String>>()
    
    var currentPath = ""
    parts.forEachIndexed { index, part ->
        currentPath = if (index == 0) "$FILE_SPLIT$part" else "$currentPath$FILE_SPLIT$part"
        pathPairs.add(Pair(part, currentPath))
    }
    
    return rootPath + pathPairs
}

/**
 * 计算父级路径
 */
private fun getParentPath(currentPath: String): String {
    return if (currentPath.contains(FILE_SPLIT) && currentPath.lastIndexOf(FILE_SPLIT) > 0) {
        currentPath.substringBeforeLast(FILE_SPLIT)
    } else {
        FILE_SPLIT
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileHeader(viewModel: FileViewModel) {
    val uiState = viewModel.uiState.collectAsState().value
    val pathParts = buildPathParts(uiState.parentPath, viewModel.getString("file.root"))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colors.background)
            .padding(12.dp)
    ) {
        BreadcrumbNavigation(
            pathParts = pathParts,
            onNavigate = { path -> viewModel.onEvent(FileUiEvent.NavigateToPath(path)) },
            onCopyPath = { path -> ClipboardUtil.setSysClipboardText(path.ifEmpty { FILE_SPLIT }) },
            copyPathLabel = viewModel.getString("file.copyPath")
        )

        FunctionButtonsRow(
            showBackButton = uiState.parentPath != FILE_SPLIT,
            onBackClick = { viewModel.onEvent(FileUiEvent.NavigateToPath(getParentPath(uiState.parentPath))) },
            onNewFolderClick = { /* TODO: 实现创建文件夹功能 */ },
            onNewFileClick = { /* TODO: 实现创建文件功能 */ },
            onImportClick = { viewModel.onEvent(FileUiEvent.Imported) },
            backLabel = viewModel.getString("file.back"),
            newFolderLabel = viewModel.getString("file.newFolder"),
            newFileLabel = viewModel.getString("file.newFile"),
            importLabel = viewModel.getString("file.importFile")
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BreadcrumbNavigation(
    pathParts: List<Pair<String, String>>,
    onNavigate: (String) -> Unit,
    onCopyPath: (String) -> Unit,
    copyPathLabel: String
) {
    val scrollState = rememberScrollState()
    
    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
    ) {
        pathParts.forEachIndexed { index, part ->
            val isLast = index == pathParts.size - 1
            val clickPath = part.second
            
            if (index > 0) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = "",
                    tint = MaterialTheme.colors.onBackground
                )
            }

            ContextMenuArea(
                items = {
                    listOf(
                        ContextMenuItem(copyPathLabel) {
                            onCopyPath(clickPath)
                        }
                    )
                }
            ) {
                PathBreadcrumb(
                    text = part.first,
                    isRoot = index == 0,
                    isLast = isLast,
                    onClick = { onNavigate(clickPath) }
                )
            }
        }
    }
}

@Composable
private fun PathBreadcrumb(
    text: String,
    isRoot: Boolean,
    isLast: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .clip(ButtonShape)
            .background(
                if (isLast) MaterialTheme.colors.primary
                else MaterialTheme.colors.surface
            )
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        if (isRoot) {
            Icon(
                Icons.Default.PhoneAndroid,
                contentDescription = text,
                tint = if (isLast) MaterialTheme.colors.onPrimary else MaterialTheme.colors.primary,
                modifier = Modifier.size(14.dp)
            )
        }
        Text(
            text = text,
            color = if (isLast) MaterialTheme.colors.onPrimary else MaterialTheme.colors.onSurface,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun FunctionButtonsRow(
    showBackButton: Boolean,
    onBackClick: () -> Unit,
    onNewFolderClick: () -> Unit,
    onNewFileClick: () -> Unit,
    onImportClick: () -> Unit,
    backLabel: String,
    newFolderLabel: String,
    newFileLabel: String,
    importLabel: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        AnimatedVisibility(visible = showBackButton) {
            FunctionButton(
                icon = Icons.Default.ArrowBack,
                text = backLabel,
                onClick = onBackClick
            )
        }

        FunctionButton(
            icon = Icons.Default.CreateNewFolder,
            text = newFolderLabel,
            onClick = onNewFolderClick
        )

        FunctionButton(
            icon = Icons.Default.NoteAdd,
            text = newFileLabel,
            onClick = onNewFileClick
        )

        FunctionButton(
            icon = Icons.Default.Upload,
            text = importLabel,
            onClick = onImportClick
        )
    }
}

@Composable
private fun FunctionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .clip(ButtonShape)
            .background(MaterialTheme.colors.surface)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = MaterialTheme.colors.primary,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = text,
            color = MaterialTheme.colors.onSurface,
            fontSize = 12.sp
        )
    }
}