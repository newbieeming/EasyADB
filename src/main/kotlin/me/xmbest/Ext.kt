package me.xmbest

import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.staticCompositionLocalOf
import me.xmbest.model.DialogState
import me.xmbest.util.PreferencesUtil
import me.xmbest.util.PreferencesUtil.PREFERENCES_CMD_AUTO_CLOSE_ENABLED
import me.xmbest.util.PreferencesUtil.PREFERENCES_CMD_AUTO_CLOSE_TIMEOUT
import me.xmbest.util.PreferencesUtil.PREFERENCES_CUSTOMER_ADB_PATH
import me.xmbest.util.PreferencesUtil.PREFERENCES_SCREENSHOT_SAVE_ENABLED
import me.xmbest.util.PreferencesUtil.PREFERENCES_SCREENSHOT_SAVE_PATH
import org.jetbrains.skiko.hostOs
import java.io.File

/**
 * first 当前代码resource目录
 * second 写入的文件名称
 */
val adb = Pair("adb" + (if (hostOs.isMacOS) "${File.separator}mac" else if (hostOs.isLinux) "${File.separator}linux" else ""), if (hostOs.isWindows) "adb.exe" else "adb")
val cfg = Pair("config", "config.json")
val exec = Pair("sh", if (hostOs.isWindows) "exec.bat" else "exec.sh")

const val nodeName = ".easyAdb"

/**
 * 文件分隔符
 */
const val FILE_SPLIT = "/"

/**
 * 当前程序存储目录
 */
val appStorageAbsolutePath: String =
    File(System.getProperty("user.home"), nodeName).absolutePath

/**
 * 当前程序携带adb环境
 */
val programAdbAbsolutePath: String =
    File(appStorageAbsolutePath, adb.second).absolutePath

/**
 * 用户自定义adb文件路径
 */
val customerAdbAbsolutePath: String
    get() = PreferencesUtil.get(PREFERENCES_CUSTOMER_ADB_PATH, "")

/**
 * 截图默认保存目录
 */
val defaultScreenshotSavePath: String
    get() = File(System.getProperty("user.home"), "Desktop").absolutePath

/**
 * 截图保存目录
 */
val screenshotSaveAbsolutePath: String
    get() = PreferencesUtil.get(PREFERENCES_SCREENSHOT_SAVE_PATH, defaultScreenshotSavePath)

/**
 * 截图保存开关
 */
val screenshotSaveEnabled: Boolean
    get() = PreferencesUtil.get(PREFERENCES_SCREENSHOT_SAVE_ENABLED, false)

/**
 * Windows 文件传输窗口自动关闭
 */
val cmdAutoCloseEnabled: Boolean
    get() = PreferencesUtil.get(PREFERENCES_CMD_AUTO_CLOSE_ENABLED, true)

val cmdAutoCloseTimeoutSeconds: Int
    get() = PreferencesUtil.get(PREFERENCES_CMD_AUTO_CLOSE_TIMEOUT, 3)

// 创建LocalSnackbarHostState，类似于LocalContext.current的使用方式
val LocalSnackbarHostState = staticCompositionLocalOf<SnackbarHostState> {
    error("No SnackbarHostState provided")
}

// 添加全局弹窗状态
val LocalDialogState = staticCompositionLocalOf<MutableState<DialogState>> {
    error("No DialogState provided")
}
