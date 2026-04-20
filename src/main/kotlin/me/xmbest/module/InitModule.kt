package me.xmbest.module

import io.github.vinceglb.filekit.FileKit
import me.xmbest.adb
import me.xmbest.appStorageAbsolutePath
import me.xmbest.cfg
import me.xmbest.ddmlib.AdbPathResolver
import me.xmbest.ddmlib.DeviceManager
import me.xmbest.exec
import me.xmbest.model.Environment
import me.xmbest.util.PreferencesUtil
import me.xmbest.util.PreferencesUtil.PREFERENCES_ADB_PATH
import java.io.File

object InitModule {
    private val fileList = buildList {
        addAll(listOf(adb, cfg, exec))
    }
    private val path = appStorageAbsolutePath

    fun init() {
        writeFile()
        initAdb()
        FileKit.init("EasyADB")
    }

    private fun writeFile() {
        val parentFile = File(path)
        if (!parentFile.exists()) {
            parentFile.mkdirs()
        }
        // 复制所需文件
        fileList.forEach {
            val fileName = it.second
            val file = File(parentFile, fileName)
            if (!file.exists()) {
                file.createNewFile()
                file.setExecutable(true)
                this::class.java.classLoader.getResourceAsStream("${it.first}/$fileName")?.use { input ->
                    input.copyTo(file.outputStream())
                }
            }
        }
    }

    private fun initAdb() {
        val savedAdbPath = PreferencesUtil.get(PREFERENCES_ADB_PATH, "")

        val adbPath = when {
            savedAdbPath.isNotEmpty() -> savedAdbPath
            else -> resolveAndSaveAdbPath()
        }

        DeviceManager.initialize(adbPath)
    }

    private fun resolveAndSaveAdbPath(): String {
        val resolveResult = AdbPathResolver.resolveAdbPath("adb")

        return when (resolveResult.source) {
            // 从 PATH 环境变量找到，保存到 System
            AdbPathResolver.AdbSource.PATH_ENV -> {
                Environment.System.path = resolveResult.path
                PreferencesUtil.set(PREFERENCES_ADB_PATH, Environment.System.path)
                resolveResult.path
            }
            // 从 SDK 默认路径找到，保存到 Custom
            AdbPathResolver.AdbSource.SDK_DEFAULT -> {
                Environment.Custom.path = resolveResult.path
                PreferencesUtil.set(PREFERENCES_ADB_PATH, resolveResult.path)
                resolveResult.path
            }
            // 未找到，使用程序自带的 ADB
            AdbPathResolver.AdbSource.FALLBACK -> {
                PreferencesUtil.set(PREFERENCES_ADB_PATH, Environment.Program.path)
                Environment.Program.path
            }
        }
    }
}