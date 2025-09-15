package me.xmbest.ddmlib

import com.android.ddmlib.FileListingService
import com.android.ddmlib.InstallReceiver
import com.android.ddmlib.MultiLineReceiver
import kotlinx.coroutines.*
import java.awt.Image
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object DeviceOperate {
    private const val TAG = "DeviceOperate"

    private val device
        get() = DeviceManager.device.value

    private val fileListingService
        get() = FileManager.fileListingService.value

    private val coroutineScope = CoroutineScope(Dispatchers.IO + CoroutineName(TAG))

    private const val CMD_CLOSE_TIMEOUT = 3

    fun root(): Boolean {
        Log.i(TAG, "adb root")
        return device?.root() ?: false
    }

    fun forceStop(applicationName: String) {
        Log.i(TAG, "adb shell force-stop $applicationName")
        device?.forceStop(applicationName)
    }

    /**
     * 需要设备root后，未root的机器推荐forceStop
     */
    fun kill(pids: List<Int>) {
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

    fun chmod(path: String, auth: Int) {
        Log.i(TAG, "adb shell chmod $auth $path")
        shell("chmod $auth $path")
    }

    /**
     * 执行文件传输操作的通用方法
     * @param operation 操作类型（push或pull）
     * @param files 文件列表
     * @param targetPath 目标路径
     * @param isWindows 是否windows平台
     * @param isMacOs 是否macOS平台
     * @param file 脚本文件
     */
    private fun executeFileTransfer(
        operation: String,
        files: List<String>,
        targetPath: String,
        isWindows: Boolean = true,
        isMacOs: Boolean = false,
        file: File
    ) {
        device?.let { device ->
            val adbCommand = files.map { file -> file to File(file).name }
                .joinToString("\n") { "${DeviceManager.adbPath.value} -s ${device.serialNumber} $operation ${it.first} \"$targetPath/${it.second}\"" }

            Log.d(TAG, "Original ADB command: $adbCommand")
            var command = adbCommand

            if (isWindows) {
                val commands = mutableListOf<String>()
                commands.add("@echo off")
                commands.add("echo Starting file transfer...")
                // utf-8 编码
                commands.add("chcp 65001")
                commands.add(adbCommand)
                commands.add("echo.")
                commands.add("echo Executing: $adbCommand")
                commands.add("echo Window will close in $CMD_CLOSE_TIMEOUT seconds...")
                commands.add("timeout /t $CMD_CLOSE_TIMEOUT /nobreak > nul")

                file.writeText(commands.joinToString("\r\n"))
                command = "cmd.exe /c start cmd.exe /C ${file.absolutePath}"
            } else if (isMacOs) {
                file.writeText(adbCommand)
                command = "open -b com.apple.terminal ${file.absolutePath}"
            }
            CmdUtil.run(command)
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
        files: List<String>, remotePath: String, isWindows: Boolean = true, isMacOs: Boolean = false, file: File
    ) {
        executeFileTransfer("push", files, remotePath, isWindows, isMacOs, file)
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
        files: List<String>, localPath: String, isWindows: Boolean = true, isMacOs: Boolean = false, file: File
    ) {
        executeFileTransfer("pull", files, localPath, isWindows, isMacOs, file)
    }

    /**
     * 安装应用
     * @param remoteFilePath 安装路径
     * @return 是否安装成功
     */
    suspend fun install(remoteFilePath: String) = suspendCoroutine {
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
        val serialNumber = device?.serialNumber
        Log.i(TAG, "adb shell screenshot $serialNumber")
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
    suspend fun ls(parentPath: String) = suspendCoroutine {
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

    fun shell(command: String) = device?.executeShellCommand(command, EmptyReceiver())

    suspend fun shell(command: String, timeMillis: Long) = suspendCoroutine {
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
    