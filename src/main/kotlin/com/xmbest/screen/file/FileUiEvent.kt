package com.xmbest.screen.file

import com.android.ddmlib.FileListingService

sealed class FileUiEvent(val path: String) {
    object Refresh : FileUiEvent("")
    class NavigateToPath(path: String) : FileUiEvent(path)
    object StartDrag : FileUiEvent("")
    object DragEnd : FileUiEvent("")
    object Imported : FileUiEvent("")
    class UploadFiles(val files: List<String>) : FileUiEvent("")
    class DownloadFiles(val files: List<FileListingService.FileEntry>) : FileUiEvent("")
    class DeleteFiles(val files: List<FileListingService.FileEntry>) : FileUiEvent("")
    object DeleteAllFiles : FileUiEvent("")
    data class CreateFolder(val folderName: String) : FileUiEvent("")
    data class CreateFile(val fileName: String) : FileUiEvent("")
    data class RenameFile(val oldPath: String, val newName: String) : FileUiEvent("")
    class Toast(val message: String) : FileUiEvent("")
}