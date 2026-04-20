package me.xmbest.ddmlib

import org.jetbrains.skiko.hostOs
import java.io.File

/**
 * ADB 路径解析工具
 */
object AdbPathResolver {
    private const val TAG = "AdbPathResolver"

    /**
     * 解析结果类型
     */
    enum class AdbSource {
        /** 从 PATH 环境变量找到 */
        PATH_ENV,
        /** 从 SDK 默认路径找到 */
        SDK_DEFAULT,
        /** 未找到，使用原始命令 */
        FALLBACK
    }

    /**
     * 解析结果
     * @param path 解析后的 ADB 路径
     * @param source ADB 来源
     */
    data class ResolveResult(
        val path: String,
        val source: AdbSource
    )

    /**
     * 解析 ADB 可执行文件路径
     * @param configuredPath 配置的路径（可能是 "adb" 命令或具体路径）
     * @return 解析结果，包含实际路径和来源
     */
    fun resolveAdbPath(configuredPath: String): ResolveResult {
        val safePath = configuredPath.trim().removeSurrounding("\"")

        if (!isSystemAdbCommand(safePath)) {
            return ResolveResult(safePath, AdbSource.FALLBACK)
        }

        val pathEnvResult = findAdbFromPathEnv()
        if (pathEnvResult != null) {
            Log.d(TAG, "Resolved adb from PATH: $pathEnvResult")
            return ResolveResult(pathEnvResult, AdbSource.PATH_ENV)
        }

        val sdkResult = findAdbFromSdkEnv()
        if (sdkResult != null) {
            Log.d(TAG, "Resolved adb from SDK default path: $sdkResult")
            return ResolveResult(sdkResult, AdbSource.SDK_DEFAULT)
        }

        Log.w(TAG, "Could not resolve adb from PATH/SDK, fallback to command: $safePath")
        return ResolveResult(safePath, AdbSource.FALLBACK)
    }

    /**
     * 判断是否为系统 ADB 命令
     */
    private fun isSystemAdbCommand(path: String): Boolean {
        val adbName = adbExecutableName()
        return path.equals("adb", ignoreCase = true) || path.equals(adbName, ignoreCase = true)
    }

    /**
     * 从 PATH 环境变量中查找 ADB
     */
    private fun findAdbFromPathEnv(): String? {
        val pathEnv = System.getenv("PATH") ?: return null
        val adbName = adbExecutableName()
        return pathEnv.split(File.pathSeparator)
            .asSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .map { File(it, adbName) }
            .firstOrNull(::isUsableExecutable)
            ?.absolutePath
    }

    /**
     * 从 Android SDK 环境变量中查找 ADB
     */
    private fun findAdbFromSdkEnv(): String? {
        val adbName = adbExecutableName()
        val sdkRoots = buildList {
            addIfNotBlank(System.getenv("ANDROID_SDK_ROOT"))
            addIfNotBlank(System.getenv("ANDROID_HOME"))
            val homePath = System.getProperty("user.home").orEmpty()
            if (homePath.isNotBlank()) {
                when {
                    hostOs.isWindows -> add("$homePath\\AppData\\Local\\Android\\Sdk")
                    hostOs.isMacOS -> add("$homePath/Library/Android/sdk")
                    hostOs.isLinux -> add("$homePath/Android/Sdk")
                }
            }
        }
        return sdkRoots
            .asSequence()
            .map { File(it, "platform-tools${File.separator}$adbName") }
            .firstOrNull(::isUsableExecutable)
            ?.absolutePath
    }

    private fun MutableList<String>.addIfNotBlank(value: String?) {
        value?.trim()?.takeIf { it.isNotEmpty() }?.let { add(it) }
    }

    private fun isUsableExecutable(file: File): Boolean {
        return file.exists() && file.isFile && (hostOs.isWindows || file.canExecute())
    }

    private fun adbExecutableName(): String {
        return if (hostOs.isWindows) "adb.exe" else "adb"
    }
}