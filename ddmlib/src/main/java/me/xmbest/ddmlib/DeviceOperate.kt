package me.xmbest.ddmlib

import com.android.ddmlib.FileListingService
import com.android.ddmlib.InstallReceiver
import com.android.ddmlib.MultiLineReceiver
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.awt.Image
import java.io.File
import kotlin.coroutines.resume

object DeviceOperate {
    private const val TAG = "DeviceOperate"

    private val device
        get() = DeviceManager.device.value

    private val fileListingService
        get() = FileManager.fileListingService.value

    private val coroutineScope = CoroutineScope(Dispatchers.IO + CoroutineName(TAG))

    private const val CMD_CLOSE_TIMEOUT = 3

    /**
     * top命令查询的字段
     */
    private val topColumns = listOf("pid", "user", "%cpu", "time+", "%mem", "virt", "res", "shr", "name")
    val topHeadColumns = listOf("pid", "user", "cpu", "time", "mem", "virt", "res", "shr")

    suspend fun startApp(packageName: String) {
        val activity = getLaunchActivity(packageName)
        if (activity.isBlank()) shell("monkey -p $packageName -v 1") else shell(
            "am start $activity"
        )
    }

    fun forceStop(applicationName: String) {
        Log.i(TAG, "adb shell force-stop $applicationName")
        device?.forceStop(applicationName)
    }

    fun clear(applicationName: String) {
        shell("pm clear $applicationName")
    }

    /**
     * 需要设备root后，未root的机器推荐forceStop
     */
    fun kill(pids: List<String>) {
        val pidStr = pids.joinToString(" ")
        Log.i(TAG, "adb shell kill $pidStr")
        shell("kill $pidStr")
    }

    fun rm(path: List<String>) {
        val pathStr = path.joinToString(" ")
        Log.i(TAG, "adb shell rm -rf $pathStr")
        shell("rm -rf $pathStr")
    }

    /**
     * mv 命令
     * @param start 开始路径
     * @param end 目标路径
     */
    fun mv(start: String, end: String) {
        device?.let { _ ->
            shell("mv $start $end")
        }
    }

    fun inputKey(key: Int) {
        Log.i(TAG, "adb shell input keyevent $key")
        shell("input keyevent $key")
    }

    fun reboot() {
        Log.i(TAG, "adb reboot")
        device?.reboot(null)
    }

    suspend fun path(packageName: String): String {
        return shell("pm path $packageName", 300L)
    }

    fun mkdir(path: String, auth: Int) {
        Log.i(TAG, "adb shell mkdir -m $auth $path")
        shell("mkdir -m $auth $path")
    }

    fun touch(path: String) {
        Log.i(TAG, "adb shell touch $path")
        shell("touch $path")
    }


    /**
     * 查找应用启动activity
     * @param packageName 包名
     */
    private suspend fun getLaunchActivity(packageName: String): String {
        val launchActivity = shell("dumpsys package $packageName -A 1 MAIN", 500)
        if (launchActivity.isBlank()) return ""
        val outLines = launchActivity.lines()
        if (outLines.isEmpty()) {
            return ""
        } else {
            for (value in outLines) {
                if (value.contains("$packageName/")) {
                    return value.substring(
                        value.indexOf("$packageName/"), value.indexOf(" filter")
                    )
                }
            }
            return ""
        }
    }

    /**
     * 执行文件传输操作的通用方法
     * @param operation 操作类型（push或pull）
     * @param files 文件列表
     * @param targetPath 目标路径
     */
    private fun buildAdbCommand(
        operation: String,
        files: List<String>,
        targetPath: String,
        serialNumber: String
    ): String {
        return files
            .map { localPath -> localPath to File(localPath).name }
            .joinToString("\n") {
                "${DeviceManager.adbPath.value} -s $serialNumber $operation ${it.first} \"$targetPath/${it.second}\""
            }
    }

    private fun buildWindowsCommand(
        adbCommand: String,
        autoCloseEnabled: Boolean,
        safeTimeout: Int,
        file: File
    ): String {
        val commands = mutableListOf(
            "@echo off",
            "echo Starting file transfer...",
            "chcp 65001",
            adbCommand,
            "echo.",
            "echo $adbCommand"
        )
        if (autoCloseEnabled) {
            commands.add("echo Window will close in $safeTimeout seconds...")
            commands.add("timeout /t $safeTimeout /nobreak > nul")
        }
        file.writeText(commands.joinToString("\r\n"))
        return if (autoCloseEnabled) {
            "cmd.exe /c start cmd.exe /C '${file.absolutePath}'"
        } else {
            "cmd.exe /c start cmd.exe /K '${file.absolutePath}'"
        }
    }

    private fun buildMacCommand(
        adbCommand: String,
        autoCloseEnabled: Boolean,
        safeTimeout: Int,
        file: File
    ): String {
        file.writeText(adbCommand)
        val scriptPath = file.absolutePath
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
        val countdownSnippet = if (autoCloseEnabled) {
            if (safeTimeout > 0) {
                "for ((i=$safeTimeout;i>0;i--)); do echo \\\"Window will close in \${i}s...\\\"; sleep 1; done"
            } else {
                "echo \\\"Window will close now...\\\""
            }
        } else {
            ""
        }
        val runLine = if (autoCloseEnabled) {
            "do script \"bash \\\"$scriptPath\\\"; echo; $countdownSnippet\""
        } else {
            "do script \"bash \\\"$scriptPath\\\"\""
        }
        val appleScriptLines = buildList {
            add("tell application \"Terminal\"")
            add("activate")
            add(runLine)
            if (autoCloseEnabled) {
                add("repeat while (busy of front window)")
                add("delay 0.5")
                add("end repeat")
                add("try")
                add("close front window")
                add("end try")
            }
            add("end tell")
        }
        return "osascript ${toAppleScriptArgs(appleScriptLines)}"
    }

    private fun toAppleScriptArgs(lines: List<String>): String {
        return lines.joinToString(" ") { "-e '${it.replace("'", "'\\''")}'" }
    }

    private fun executeFileTransfer(
        operation: String,
        files: List<String>,
        targetPath: String,
        isWindows: Boolean = true,
        isMacOs: Boolean = false,
        autoCloseEnabled: Boolean = true,
        autoCloseTimeoutSeconds: Int = CMD_CLOSE_TIMEOUT,
        file: File
    ) {
        device?.let { device ->
            val adbCommand = buildAdbCommand(operation, files, targetPath, device.serialNumber)
            Log.d(TAG, "Original ADB command: $adbCommand")
            val safeTimeout = autoCloseTimeoutSeconds.coerceAtLeast(0)
            val command = when {
                isWindows -> buildWindowsCommand(adbCommand, autoCloseEnabled, safeTimeout, file)
                isMacOs -> buildMacCommand(adbCommand, autoCloseEnabled, safeTimeout, file)
                else -> adbCommand
            }
            if (isMacOs) CmdUtil.runShell(command) else CmdUtil.run(command)
        }
    }

    /**
     * push多个文件到系统
     * @param files 文件列表
     * @param remotePath 需要上传到位置
     * @param isWindows 是否windows平台
     * @param isMacOs 是否macOS平台
     * @param file 这里非windows需要传，即软件执行文件
     */
    fun push(
        files: List<String>,
        remotePath: String,
        isWindows: Boolean = true,
        isMacOs: Boolean = false,
        autoCloseEnabled: Boolean = true,
        autoCloseTimeoutSeconds: Int = CMD_CLOSE_TIMEOUT,
        file: File
    ) {
        executeFileTransfer(
            "push",
            files,
            remotePath,
            isWindows,
            isMacOs,
            autoCloseEnabled,
            autoCloseTimeoutSeconds,
            file
        )
    }

    /**
     * pull多个文件到本地
     * @param files 文件列表
     * @param localPath 本地路径
     * @param isWindows 是否windows平台
     * @param isMacOs 是否macOS平台
     * @param file 这里非windows需要传，即软件执行文件
     */
    fun pull(
        files: List<String>,
        localPath: String,
        isWindows: Boolean = true,
        isMacOs: Boolean = false,
        autoCloseEnabled: Boolean = true,
        autoCloseTimeoutSeconds: Int = CMD_CLOSE_TIMEOUT,
        file: File
    ) {
        executeFileTransfer(
            "pull",
            files,
            localPath,
            isWindows,
            isMacOs,
            autoCloseEnabled,
            autoCloseTimeoutSeconds,
            file
        )
    }

    /**
     * 安装应用
     * @param remoteFilePath 安装路径
     * @return 是否安装成功
     */
    suspend fun install(remoteFilePath: String) = suspendCancellableCoroutine {
        device?.installRemotePackage(remoteFilePath, true, object : InstallReceiver() {
            override fun done() {
                it.resume(
                    if (isSuccessfullyCompleted) InstallState.Success(successMessage)
                    else InstallState.Error(errorCode, errorMessage)
                )
            }
        }, "-t") ?: it.resume(InstallState.NotConnected)
    }

    /**
     * 卸载应用
     * @param packageName 应用包名
     * @return null success else error msg
     */
    fun uninstall(packageName: String): String? {
        return device?.uninstallPackage(packageName)
    }

    /**
     * 截图
     */
    fun screenshot(): Image? {
        return device?.screenshot?.asBufferedImage()
    }

    suspend fun findCurrentActivity(): String {
        val shell = shell("dumpsys window | grep mCurrentFocus", 200)
        val regex = Regex(pattern = """\s\S+/\S+}""")
        return regex.find(shell)?.value?.replace("}", "")?.trim() ?: ""
    }

    /**
     * 查找当前文件列表
     */
    suspend fun ls(parentPath: String) = suspendCancellableCoroutine {
        fileListingService?.apply {
            getChildren(
                FileListingService.FileEntry(
                    root, parentPath, FileListingService.TYPE_DIRECTORY, false
                ), false, object : FileListingService.IListingReceiver {
                    override fun setChildren(
                        entry: FileListingService.FileEntry?, children: Array<out FileListingService.FileEntry>?
                    ) {
                        it.resume(children?.asList() ?: emptyList())
                    }

                    override fun refreshEntry(entry: FileListingService.FileEntry?) {
                    }
                })
        }
    }

    /**
     * 控制状态栏
     * @param show 显示、隐藏
     */
    fun controlStatusbar(show: Boolean) {
        shell("service call statusbar ${if (show) 1 else 2}")
    }

    fun openSettings() {
        shell("am start  -n com.android.settings/com.android.settings.Settings")
    }

    fun tcpip(port: Int = 5555) {
        CmdUtil.run("${DeviceManager.adbPath.value} -s ${device?.serialNumber} tcpip $port")
    }

    /**
     * 清理logcat缓存
     */
    fun logcatC() = shell("logcat -c")

    /**
     * 获取安装应用列表
     * @param filter 过滤关键词
     * @param thirdApp 是否第三方应用
     */
    suspend fun getAppList(filter: String, thirdApp: Boolean = false): List<AppInfo> {
        val thirdPostfix = if (thirdApp) " -3" else ""
        val postfix = if (filter.isEmpty()) "" else " | grep -E '$filter'"
        val command = "pm list packages -f $thirdPostfix$postfix"
        Log.d(TAG, command)
        val result = shell(command, 500)
        return result.split("\n")
            .filter { it.isNotBlank() }
            .mapNotNull { line ->
                val lastEqualIndex = line.lastIndexOf("=")
                if (lastEqualIndex != -1) {
                    val path = line.take(lastEqualIndex).removePrefix("package:")
                    val packageName = line.substring(lastEqualIndex + 1)
                    AppInfo(
                        path = path,
                        packageName = packageName,
                    )
                } else {
                    null
                }
            }
    }

    suspend fun getAppVersion(packageName: String): Map<String, String> {
        val command = "dumpsys package $packageName | grep version"
        val result = shell(command, 300)

        val versionMap = mutableMapOf<String, String>()

        result.split("\n").forEach { line ->
            val trimmedLine = line.trim()
            if (trimmedLine.contains("versionCode=")) {
                val versionCode = trimmedLine.substringAfter("versionCode=").substringBefore(" ")
                versionMap["versionCode"] = versionCode
            }

            if (trimmedLine.contains("minSdk=")) {
                val minSdk = trimmedLine.substringAfter("minSdk=").substringBefore(" ")
                versionMap["minSdk"] = minSdk
            }

            if (trimmedLine.contains("targetSdk=")) {
                val targetSdk = trimmedLine.substringAfter("targetSdk=").substringBefore(" ")
                versionMap["targetSdk"] = targetSdk
            }

            if (trimmedLine.contains("versionName=")) {
                val versionName = trimmedLine.substringAfter("versionName=").trim()
                versionMap["versionName"] = versionName
            }
        }

        return versionMap
    }

    /**
     * 获取文件大小并格式化为合适的单位
     * @param packageName 文件包名
     * @return 格式化后的文件大小字符串（GB、MB或KB）
     */
    suspend fun getFileSize(packageName: String): String {
        return try {
            val command = "pm path $packageName | cut -d: -f2 | xargs stat -c %s"
            val result = shell(command, 300)
            val sizeInBytes = result.trim().toLongOrNull() ?: 0L

            when {
                sizeInBytes >= 1024 * 1024 * 1024 -> {
                    val sizeInGB = sizeInBytes / (1024.0 * 1024.0 * 1024.0)
                    String.format("%.2f GB", sizeInGB)
                }

                sizeInBytes >= 1024 * 1024 -> {
                    val sizeInMB = sizeInBytes / (1024.0 * 1024.0)
                    String.format("%.2f MB", sizeInMB)
                }

                sizeInBytes >= 1024 -> {
                    val sizeInKB = sizeInBytes / 1024.0
                    String.format("%.2f KB", sizeInKB)
                }

                else -> {
                    "$sizeInBytes B"
                }
            }
        } catch (_: Exception) {
            "Unknown"
        }
    }

    /**
     * 获取当前进程信息
     */
    suspend fun getProcessList(filter: String): List<ProcessInfo> {
        // 第一次尝试：使用 `top -b -n 1 -o <column>` 格式
        val postfix = if (filter.isEmpty()) "" else " | grep -E '$filter'"
        val command = topColumns.joinToString(" -o ", "top -b -n 1 -o ", postfix)
        Log.d(TAG, "command = $command")
        val result = shell(command, 1000)
        var processes = parseTopOutput(result, topColumns)

        // 如果第一次解析失败（可能是旧版 top），尝试通用 `top -n 1` 格式
        if (processes.isEmpty()) {
            val cmd = "top -n 1$postfix"
            val fallbackResult = shell(cmd, 500)
            val fallbackColumns = fallbackResult.split("\n")
                .firstOrNull { it.trim().startsWith("PID") }
                ?.trim()
                ?.split("\\s+".toRegex())
                ?.map { column ->
                    when (column.lowercase()) {
                        "cpu%" -> "%cpu"
                        "uid" -> "user"
                        "rss" -> "res"
                        else -> column.lowercase()
                    }
                } ?: topColumns // 如果仍然无法解析，使用默认列名
            processes = parseTopOutput(fallbackResult, fallbackColumns)
        }
        return processes
    }

    private fun parseTopOutput(output: String, columns: List<String>): List<ProcessInfo> {
        val lines = output.split("\n")
        var startIndex = 0

        // 查找表头行（以 "PID" 开头）
        for (i in lines.indices) {
            if (lines[i].trim().startsWith("PID") || lines[i].trim().endsWith("%host")) {
                startIndex = i + 1
                break
            }
        }

        if (startIndex < lines.size && lines[startIndex].trim().startsWith("PID")) {
            startIndex += 1
        }

        return lines.drop(startIndex)
            .filter { it.trim().isNotEmpty() }
            .map { line ->
                val parts = line.trim().split("\\s+".toRegex())
                val process = mutableMapOf<String, String>()

                columns.forEachIndexed { index, column ->
                    process[column] = if (column == "args") {
                        parts.drop(index).joinToString(" ")
                    } else {
                        parts.getOrNull(index) ?: ""
                    }
                }

                ProcessInfo(
                    pid = process["pid"] ?: "",
                    user = process["user"] ?: "",
                    cpu = process["%cpu"] ?: "",
                    time = process["time+"] ?: "",
                    mem = process["%mem"] ?: "",
                    virt = process["virt"] ?: "",
                    res = process["res"] ?: "",
                    shr = process["shr"] ?: "",
                    name = process["name"] ?: "",
                    args = process["args"] ?: "",
                )
            }
            .filter { it.args != "top -b -n 1" } // 过滤掉 top 命令自身的进程
    }

    fun shell(command: String) = device?.executeShellCommand(command, EmptyReceiver())

    suspend fun shell(command: String, timeMillis: Long) = suspendCancellableCoroutine {
        coroutineScope.launch {
            var resume = false
            device?.executeShellCommand(command, object : MultiLineReceiver() {
                override fun processNewLines(lines: Array<out String>?) {
                    if (lines?.isNotEmpty() == true && isActive && !resume) {
                        val str = lines.filter { line -> line.isNotEmpty() }.joinToString("\n")
                        resume = true
                        it.resume(str)
                    }
                }

                override fun isCancelled() = false
            })
            delay(timeMillis)
            if (isActive && !resume) {
                resume = true
                it.resume("")
            }
        }
    }
}
    
