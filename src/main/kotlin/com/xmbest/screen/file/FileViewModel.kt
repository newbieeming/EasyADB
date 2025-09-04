package com.xmbest.screen.file

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.*
import androidx.lifecycle.viewModelScope
import com.android.ddmlib.FileListingService
import com.xmbest.FILE_SPLIT
import com.xmbest.appStorageAbsolutePath
import com.xmbest.base.BaseViewModel
import com.xmbest.ddmlib.DeviceOperate
import com.xmbest.ddmlib.FileManager
import com.xmbest.ddmlib.Log
import com.xmbest.exec
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.openDirectoryPicker
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.path
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.skiko.hostOs
import java.io.File
import java.text.DecimalFormat

class FileViewModel : BaseViewModel<FileUiState>() {

    companion object {
        private const val TAG = "FileViewModel"
        private const val GB = 1024 * 1024 * 1024 //定义GB的计算常量
        private const val MB = 1024 * 1024 //定义MB的计算常量
        private const val KB = 1024 //定义KB的计算常量
    }

    override val _uiState: MutableStateFlow<FileUiState> = MutableStateFlow(FileUiState())

    init {
        viewModelScope.launch(Dispatchers.Default) {
            FileManager.fileListingService.filter { it != null }.collect {
                _uiState.value =
                    _uiState.value.copy(
                        children = DeviceOperate.ls(uiState.value.parentPath)
                    )
            }
        }
    }

    fun onEvent(event: FileUiEvent) {
        viewModelScope.launch(Dispatchers.Default) {
            when (event) {
                is FileUiEvent.NavigateToPath -> navigateToPath(event.path)
                is FileUiEvent.Refresh -> refreshCurrentDirectory()
                is FileUiEvent.StartDrag -> handleStartDrag(event.files)
                is FileUiEvent.DragEnd -> handleDragEnd()
                is FileUiEvent.Imported -> handleImported()
                is FileUiEvent.UploadFiles -> handleUploadFiles(event.files, uiState.value.parentPath)
                is FileUiEvent.DownloadFiles -> handleDownloadFiles(event.files)
                is FileUiEvent.DeleteFiles -> handleDeleteFiles(event.files)
                is FileUiEvent.CreateFolder -> handleCreateFolder(event.folderName)
            is FileUiEvent.CreateFile -> handleCreateFile(event.fileName)
            is FileUiEvent.RenameFile -> handleRenameFile(event.oldPath, event.newName)
                is FileUiEvent.Toast -> handleToast(event.message)
            }
        }

    }

    fun calculatePath(parentPath: String, fileName: String): String {
        return if (parentPath.endsWith(FILE_SPLIT)) {
            parentPath + fileName
        } else {
            "$parentPath$FILE_SPLIT$fileName"
        }
    }

    private suspend fun refreshCurrentDirectory() {
        Log.d(TAG, "refreshCurrentDirectory path = ${uiState.value.parentPath}")
        _uiState.value =
            _uiState.value.copy(
                children = DeviceOperate.ls(uiState.value.parentPath)
            )
    }

    private suspend fun navigateToPath(path: String) {
        Log.d(TAG, "navigateToPath path = $path")
        _uiState.value = _uiState.value.copy(parentPath = path)
        refreshCurrentDirectory()
    }


    fun getFileTypeInfo(type: Int): FileTypeInfo {
        return when (type) {
            FileListingService.TYPE_DIRECTORY -> FileTypeInfo(
                Icons.Default.Folder,
                getString("file.directory")
            )

            FileListingService.TYPE_DIRECTORY_LINK -> FileTypeInfo(
                Icons.Default.FolderOpen,
                getString("file.directoryLink")
            )

            FileListingService.TYPE_BLOCK -> FileTypeInfo(
                Icons.Default.Storage,
                getString("file.block")
            )

            FileListingService.TYPE_CHARACTER -> FileTypeInfo(
                Icons.Default.DeviceHub,
                getString("file.character")
            )

            FileListingService.TYPE_LINK -> FileTypeInfo(Icons.Default.Link, getString("file.link"))
            FileListingService.TYPE_SOCKET -> FileTypeInfo(
                Icons.Default.Cable,
                getString("file.socket")
            )

            FileListingService.TYPE_FIFO -> FileTypeInfo(
                Icons.Default.Timeline,
                getString("file.fifo")
            )

            FileListingService.TYPE_FILE -> FileTypeInfo(
                Icons.AutoMirrored.Filled.InsertDriveFile,
                getString("file.file")
            )

            else -> FileTypeInfo(Icons.AutoMirrored.Filled.Help, getString("file.other"))
        }
    }


    /**
     * 字节转gb单位
     * @param size 字节数大小
     */
    fun byte2Gb(size: String): String {
        var sizeInt: Int

        if (size.contains(",")) {
            val split = size.split(",")
            if (split.size > 1) {
                val sizeInner = split[1].trim()
                return byte2Gb(runCatching {
                    sizeInner.toInt()
                }.getOrNull()?.toString() ?: "0")
            }
        }

        try {
            sizeInt = size.toInt()
        } catch (_: Exception) {
            return "0B"
        }
        //获取到的size为：1705230
        val df = DecimalFormat("0.00") //格式化小数
        val resultSize: String = if (sizeInt / GB >= 1) {
            //如果当前Byte的值大于等于1GB
            df.format((sizeInt / GB.toFloat()).toDouble()) + "GB"
        } else if (sizeInt / MB >= 1) {
            //如果当前Byte的值大于等于1MB
            df.format((sizeInt / MB.toFloat()).toDouble()) + "MB"
        } else if (sizeInt / KB >= 1) {
            //如果当前Byte的值大于等于1KB
            df.format((sizeInt / KB.toFloat()).toDouble()) + "KB"
        } else {
            size + "B"
        }
        return resultSize
    }

    // 拖拽事件处理方法
    private fun handleStartDrag(files: List<String>) {
        val fileCount = files.size
        val tipText = if (fileCount == 1) {
            getString("file.drag.uploadSingle").format(files.first().substringAfterLast("/"))
        } else {
            getString("file.drag.uploadMultiple").format(fileCount)
        }

        _uiState.value = _uiState.value.copy(
            isDragging = true,
            uploadTipText = tipText + " " + getString("file.drag.toCurrentPath").format(uiState.value.parentPath)
        )
    }

    private fun handleDragEnd() {
        _uiState.value = _uiState.value.copy(
            isDragging = false,
            uploadTipText = "",
        )
    }

    private suspend fun handleUploadFiles(files: List<String>, remotePath: String) {
        withContext(Dispatchers.IO) {
            DeviceOperate.push(
                files = files,
                remotePath = remotePath,
                isWindows = hostOs.isWindows,
                isMacOs = hostOs.isMacOS,
                file = File(appStorageAbsolutePath, exec.second)
            )
        }
        refreshCurrentDirectory()
    }

    private suspend fun handleDownloadFiles(files: List<FileListingService.FileEntry>) {
        withContext(Dispatchers.IO) {
            val localPath = FileKit.openDirectoryPicker(getString("file.saveTo"))?.path ?: return@withContext
            DeviceOperate.pull(
                files = files.map { it.absolutePath },
                localPath = localPath,
                isWindows = hostOs.isWindows,
                isMacOs = hostOs.isMacOS,
                file = File(appStorageAbsolutePath, exec.second)
            )
        }
    }

    private suspend fun handleDeleteFiles(files: List<FileListingService.FileEntry>) {
        withContext(Dispatchers.IO) {
            DeviceOperate.rm(files.map { it.absolutePath })
        }
        refreshCurrentDirectory()
    }

    private suspend fun handleImported() {
        val files = FileKit.openFilePicker(mode = FileKitMode.Multiple())
        // 检查是否包含中文路径
        val chinesePathFiles = files?.map { it.path }?.filter { path ->
            path.any { char -> char.toString().matches(Regex("[\u4e00-\u9fa5]")) }
        }

        if (chinesePathFiles?.isNotEmpty() == true) {
            handleToast(getString("file.drag.chinesePathError"))
        } else {
            files?.map { it.path }?.let {
                handleUploadFiles(it, uiState.value.parentPath)
            }
        }
    }

    private suspend fun handleCreateFolder(folderName: String) {
        if (folderName.isBlank()) {
            handleToast(getString("file.create.folder.nameEmpty"))
            return
        }
        
        val folderPath = calculatePath(uiState.value.parentPath, folderName)
        withContext(Dispatchers.IO) {
            try {
                DeviceOperate.mkdir(folderPath, 755)
                handleToast(getString("file.create.folder.success").format(folderName))
            } catch (e: Exception) {
                handleToast(getString("file.create.folder.error").format(e.message ?: "Unknown error"))
            }
        }
        refreshCurrentDirectory()
    }
    
    private suspend fun handleCreateFile(fileName: String) {
        if (fileName.isBlank()) {
            handleToast(getString("file.create.file.nameEmpty"))
            return
        }
        
        val filePath = calculatePath(uiState.value.parentPath, fileName)
        withContext(Dispatchers.IO) {
            try {
                DeviceOperate.touch(filePath)
                handleToast(getString("file.create.file.success").format(fileName))
            } catch (e: Exception) {
                handleToast(getString("file.create.file.error").format(e.message ?: "Unknown error"))
            }
        }
        refreshCurrentDirectory()
    }
    
    private suspend fun handleRenameFile(oldPath: String, newName: String) {
        if (newName.isBlank()) {
            handleToast(getString("file.rename.name.empty"))
            return
        }
        
        val parentPath = oldPath.substringBeforeLast("/")
        val newPath = calculatePath(parentPath, newName)
        withContext(Dispatchers.IO) {
            try {
                DeviceOperate.mv(oldPath, newPath)
                handleToast(getString("file.rename.success"))
            } catch (e: Exception) {
                handleToast(getString("file.rename.failed").format(e.message ?: "Unknown error"))
            }
        }
        refreshCurrentDirectory()
    }

    private fun handleToast(message: String) {
        _uiState.value = _uiState.value.copy(toast = message)
    }
}