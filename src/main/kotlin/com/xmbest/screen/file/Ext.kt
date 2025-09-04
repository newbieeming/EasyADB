package com.xmbest.screen.file

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.awtTransferable
import com.android.ddmlib.FileListingService
import java.awt.datatransfer.DataFlavor
import java.io.File

@OptIn(ExperimentalComposeUiApi::class)
internal fun extractFilesFromEvent(event: DragAndDropEvent): List<String> {
    return runCatching {
        val transferable = event.awtTransferable
        when {
            transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor) -> {
                @Suppress("UNCHECKED_CAST")
                val fileList = transferable.getTransferData(DataFlavor.javaFileListFlavor) as List<File>
                fileList.map { it.absolutePath }
            }

            transferable.isDataFlavorSupported(DataFlavor.stringFlavor) -> {
                val uriString = transferable.getTransferData(DataFlavor.stringFlavor) as String
                // 处理 URI 列表格式
                uriString.lines()
                    .filter { it.isNotBlank() && it.startsWith("file://") }
                    .map { it.removePrefix("file://").replace("%20", " ") }
            }

            else -> emptyList()
        }
    }.getOrNull() ?: emptyList()
}

/**
 * full是已多个/// 开头的最终的目的只需要一个/开头
 */
internal val FileListingService.FileEntry.absolutePath
    get() = fullPath.replace(Regex("^/+"), "/")