package me.xmbest.screen.file

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.DeviceHub
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Timeline
import androidx.lifecycle.viewModelScope
import com.android.ddmlib.FileListingService
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.openDirectoryPicker
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.path
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.xmbest.FILE_SPLIT
import me.xmbest.appStorageAbsolutePath
import me.xmbest.base.BaseViewModel
import me.xmbest.cmdAutoCloseEnabled
import me.xmbest.cmdAutoCloseTimeoutSeconds
import me.xmbest.ddmlib.DeviceOperate
import me.xmbest.ddmlib.FileManager
import me.xmbest.ddmlib.Log
import me.xmbest.exec
import me.xmbest.util.PreferencesUtil
import java.io.File
import java.text.DecimalFormat

class FileViewModel : BaseViewModel<FileUiState>() {

    companion object {
        private const val TAG = "FileViewModel"
        private const val GB = 1024 * 1024 * 1024 //定义GB的计算常量
        private const val MB = 1024 * 1024 //定义MB的计算常量
        private const val KB = 1024 //定义KB的计算常量
        private const val FILTER_DEBOUNCE_DELAY = 300L // 防抖动延迟时间（毫秒）
    }

    override val _uiState: MutableStateFlow<FileUiState> =
        MutableStateFlow(
            FileUiState(
                uploadTipText = getString("file.upload.dragTip"),
                favorites = PreferencesUtil.getFavorites()
            )
        )

    // 缓存原始文件列表，避免重复请求
    private var cachedFileList: List<FileListingService.FileEntry> = emptyList()
    private var cachedPath: String = ""

    // 防抖动Job
    private var filterJob: Job? = null

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
                is FileUiEvent.Navigation -> handleNavigationEvent(event)
                is FileUiEvent.FileOperation -> handleFileOperationEvent(event)
                is FileUiEvent.Drag -> handleDragEvent(event)
                is FileUiEvent.Favorites -> handleFavoritesEvent(event)
                is FileUiEvent.UI -> handleUIEvent(event)
            }
        }
    }

    private suspend fun handleNavigationEvent(event: FileUiEvent.Navigation) {
        when (event) {
            is FileUiEvent.Navigation.Refresh -> refreshCurrentDirectory()
            is FileUiEvent.Navigation.NavigateToPath -> navigateToPath(event.path)
            is FileUiEvent.Navigation.JumpToClipboardPath -> handleJumpToClipboardPath()
            is FileUiEvent.Navigation.NavigateToFavorite -> handleNavigateToFavorite(event.favoritePath)
        }
    }

    private suspend fun handleFileOperationEvent(event: FileUiEvent.FileOperation) {
        when (event) {
            is FileUiEvent.FileOperation.Imported -> handleImported()
            is FileUiEvent.FileOperation.UploadFiles -> handleUploadFiles(event.files, uiState.value.parentPath)
            is FileUiEvent.FileOperation.DownloadFiles -> handleDownloadFiles(event.files)
            is FileUiEvent.FileOperation.DeleteFiles -> handleDeleteFiles(event.files)
            is FileUiEvent.FileOperation.DeleteAllFiles -> handleDeleteAllFiles()
            is FileUiEvent.FileOperation.CreateFolder -> handleCreateFolder(event.folderName)
            is FileUiEvent.FileOperation.CreateFile -> handleCreateFile(event.fileName)
            is FileUiEvent.FileOperation.RenameFile -> handleRenameFile(event.oldPath, event.newName)
        }
    }

    private fun handleDragEvent(event: FileUiEvent.Drag) {
        when (event) {
            is FileUiEvent.Drag.StartDrag -> handleStartDrag()
            is FileUiEvent.Drag.DragEnd -> handleDragEnd()
        }
    }

    private fun handleFavoritesEvent(event: FileUiEvent.Favorites) {
        when (event) {
            is FileUiEvent.Favorites.ToggleFavorite -> handleToggleFavorite(event.filePath)
            is FileUiEvent.Favorites.RefreshFavorites -> handleRefreshFavorites()
        }
    }

    private fun handleUIEvent(event: FileUiEvent.UI) {
        when (event) {
            is FileUiEvent.UI.Toast -> handleToast(event.message)
            is FileUiEvent.UI.UpdateFilter -> handleUpdateFilterWithDebounce(event.filter)
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
        Log.d(
            TAG,
            "refreshCurrentDirectory path = ${uiState.value.parentPath}, filter = ${uiState.value.filterStr}"
        )

        // 从设备获取最新文件列表并缓存
        val fileList = DeviceOperate.ls(uiState.value.parentPath)
        cachedFileList = fileList
        cachedPath = uiState.value.parentPath

        // 应用过滤
        val filteredList = if (uiState.value.filterStr.isBlank()) {
            fileList
        } else {
            fileList.filter { it.name.contains(uiState.value.filterStr, true) }
        }

        _uiState.value = _uiState.value.copy(children = filteredList)
    }

    private suspend fun navigateToPath(path: String) {
        Log.d(TAG, "navigateToPath path = $path")
        _uiState.value = _uiState.value.copy(parentPath = path, filterStr = "")
        // 清除缓存，因为路径改变了
        cachedFileList = emptyList()
        cachedPath = ""
        refreshCurrentDirectory()
    }

    /**
     * 带防抖动的过滤处理
     */
    private fun handleUpdateFilterWithDebounce(filter: String) {
        // 立即更新UI状态中的过滤字符串，提供即时反馈
        _uiState.value = _uiState.value.copy(filterStr = filter)

        // 取消之前的防抖动任务
        filterJob?.cancel()

        // 启动新的防抖动任务
        filterJob = viewModelScope.launch(Dispatchers.Default) {
            delay(FILTER_DEBOUNCE_DELAY)
            applyFilter(filter)
        }
    }

    /**
     * 应用过滤逻辑，优先使用缓存
     */
    private suspend fun applyFilter(filter: String) {
        Log.d(
            TAG,
            "applyFilter filter = $filter, cachedPath = $cachedPath, currentPath = ${uiState.value.parentPath}"
        )

        // 检查是否可以使用缓存
        val fileList = if (cachedPath == uiState.value.parentPath && cachedFileList.isNotEmpty()) {
            // 使用缓存的文件列表
            Log.d(TAG, "Using cached file list for filtering")
            cachedFileList
        } else {
            // 缓存不可用，重新获取文件列表
            Log.d(TAG, "Cache miss, fetching fresh file list")
            val freshList = DeviceOperate.ls(uiState.value.parentPath)
            cachedFileList = freshList
            cachedPath = uiState.value.parentPath
            freshList
        }

        // 应用过滤
        val filteredList = if (filter.isBlank()) {
            fileList
        } else {
            fileList.filter { it.name.contains(filter, true) }
        }

        // 更新UI状态
        _uiState.value = _uiState.value.copy(children = filteredList)
    }

    /**
     * 原有的handleUpdateFilter方法保持兼容性，但现在使用防抖动版本
     */
//    private suspend fun handleUpdateFilter(filter: String) {
//        handleUpdateFilterWithDebounce(filter)
//    }

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

    /**
     * 清除文件列表缓存
     */
    private fun clearCache() {
        cachedFileList = emptyList()
        cachedPath = ""
    }

    // 拖拽事件处理方法
    private fun handleStartDrag() {
        _uiState.value = _uiState.value.copy(
            isDragging = true
        )
    }

    private fun handleDragEnd() {
        _uiState.value = _uiState.value.copy(
            isDragging = false
        )
    }

    private suspend fun handleUploadFiles(files: List<String>, remotePath: String) {
        withContext(Dispatchers.IO) {
            DeviceOperate.push(
                files = files,
                remotePath = remotePath,
                autoCloseEnabled = cmdAutoCloseEnabled,
                autoCloseTimeoutSeconds = cmdAutoCloseTimeoutSeconds,
                file = File(appStorageAbsolutePath, exec.second)
            )
        }
        // 清除缓存，因为文件列表已改变
        clearCache()
        refreshCurrentDirectory()
    }

    private suspend fun handleDownloadFiles(files: List<FileListingService.FileEntry>) {
        withContext(Dispatchers.IO) {
            val localPath =
                FileKit.openDirectoryPicker(getString("file.saveTo"))?.path ?: return@withContext
            DeviceOperate.pull(
                files = files.map { it.absolutePath },
                localPath = localPath,
                autoCloseEnabled = cmdAutoCloseEnabled,
                autoCloseTimeoutSeconds = cmdAutoCloseTimeoutSeconds,
                file = File(appStorageAbsolutePath, exec.second)
            )
        }
    }

    private suspend fun handleDeleteFiles(files: List<FileListingService.FileEntry>) {
        withContext(Dispatchers.IO) {
            DeviceOperate.rm(files.map { it.absolutePath })
        }
        // 清除缓存，因为文件列表已改变
        clearCache()
        refreshCurrentDirectory()
    }

    private suspend fun handleDeleteAllFiles() {
        val currentFiles = uiState.value.children
        if (currentFiles.isNotEmpty()) {
            withContext(Dispatchers.IO) {
                DeviceOperate.rm(currentFiles.map { it.absolutePath })
            }
            // 清除缓存，因为文件列表已改变
            clearCache()
            refreshCurrentDirectory()
        }
    }

    private suspend fun handleImported() {
        val files = FileKit.openFilePicker(mode = FileKitMode.Multiple())
        files?.map { it.path }?.let {
            handleUploadFiles(it, uiState.value.parentPath)
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
                handleToast(
                    getString("file.create.folder.error").format(
                        e.message ?: "Unknown error"
                    )
                )
            }
        }
        // 清除缓存，因为文件列表已改变
        clearCache()
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
                handleToast(
                    getString("file.create.file.error").format(
                        e.message ?: "Unknown error"
                    )
                )
            }
        }
        // 清除缓存，因为文件列表已改变
        clearCache()
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
        // 清除缓存，因为文件列表已改变
        clearCache()
        refreshCurrentDirectory()
    }

    private fun handleToast(message: String) {
        _uiState.value = _uiState.value.copy(toast = message)
    }

    private suspend fun handleUpdateFilter(filter: String) {
        _uiState.value = _uiState.value.copy(filterStr = filter)
        refreshCurrentDirectory()
    }

    private suspend fun handleJumpToClipboardPath() {
        withContext(Dispatchers.IO) {
            try {
                // 读取剪切板内容
                val clipboardText = me.xmbest.ddmlib.ClipboardUtil.getSysClipboardText()

                if (clipboardText.isNullOrBlank()) {
                    handleToast(getString("file.jumpToClipboard.emptyClipboard"))
                    return@withContext
                }

                // 验证路径格式（简单验证是否以/开头）
                val path = clipboardText.trim()
                if (!path.startsWith(FILE_SPLIT)) {
                    handleToast(getString("file.jumpToClipboard.invalidPath"))
                    return@withContext
                }

                // 检查路径是否存在并获取文件信息
                val fileEntry = getFileEntry(path)
                if (fileEntry == null) {
                    handleToast(getString("file.jumpToClipboard.pathNotExist"))
                    return@withContext
                }

                // 判断是文件还是目录，并执行相应的跳转逻辑
                if (fileEntry.isDirectory) {
                    // 如果是目录，直接跳转到该目录
                    navigateToPath(path)
                } else {
                    // 如果是文件，跳转到文件的上级目录
                    val parentPath = path.substringBeforeLast(FILE_SPLIT).ifEmpty { FILE_SPLIT }
                    navigateToPath(parentPath)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error jumping to clipboard path", e)
                handleToast(getString("file.jumpToClipboard.invalidPath"))
            }
        }
    }

    private suspend fun getFileEntry(path: String): FileListingService.FileEntry? {
        return try {
            val parentPath = path.substringBeforeLast(FILE_SPLIT).ifEmpty { FILE_SPLIT }
            val fileName = path.substringAfterLast(FILE_SPLIT)
            val children = DeviceOperate.ls(parentPath)
            children.find { it.name == fileName }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting file entry: $path", e)
            null
        }
    }

    // 收藏夹相关处理方法
    private fun handleToggleFavorite(filePath: String) {
        val currentFavorites = _uiState.value.favorites
        val updatedFavorites = if (PreferencesUtil.isFavorite(filePath)) {
            PreferencesUtil.removeFavorite(filePath)
            currentFavorites - filePath
        } else {
            PreferencesUtil.addFavorite(filePath)
            currentFavorites + filePath
        }

        _uiState.value = _uiState.value.copy(favorites = updatedFavorites)
    }

    private fun handleRefreshFavorites() {
        val favorites = PreferencesUtil.getFavorites()
        _uiState.value = _uiState.value.copy(favorites = favorites)
    }

    private suspend fun handleNavigateToFavorite(favoritePath: String) {
        try {
            // 检查路径是否存在
            val fileEntry = getFileEntry(favoritePath)
            if (fileEntry == null) {
                handleToast(getString("favorites.pathNotExist").format(favoritePath))
                // 从收藏夹中移除不存在的路径
                PreferencesUtil.removeFavorite(favoritePath)
                handleRefreshFavorites()
                return
            }

            // 如果是目录，直接跳转
            if (fileEntry.isDirectory) {
                navigateToPath(favoritePath)
            } else {
                // 如果是文件，跳转到文件的上级目录
                val parentPath = favoritePath.substringBeforeLast(FILE_SPLIT).ifEmpty { FILE_SPLIT }
                navigateToPath(parentPath)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to favorite: $favoritePath", e)
            handleToast(getString("favorites.navigationError").format(e.message ?: "Unknown error"))
        }
    }
}
