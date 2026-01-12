package me.xmbest.ddmlib

import java.io.File

object CmdUtil {

    private const val TAG = "CmdUtil"

    /**
     * 启动一个进程并返回 Process 对象。
     * @param command 要执行的命令（如 "ls -l"）。
     * @return 启动的 Process 对象。
     * @throws IllegalArgumentException 如果命令为空或空白。
     */
    fun run(command: String): Process {
        require(command.isNotBlank()) { "Command must not be blank." }
        Log.d(TAG, "run $command")
        val commands = command.split(" ").filter { it.isNotBlank() }
        val builder = ProcessBuilder(commands)
        builder.directory(File(System.getProperty("user.dir")))
        return builder.start()
    }

    fun runShell(command: String): Process {
        require(command.isNotBlank()) { "Command must not be blank." }
        Log.d(TAG, "runShell $command")
        val osName = System.getProperty("os.name").lowercase()
        val commands = if (osName.contains("win")) {
            listOf("cmd.exe", "/c", command)
        } else {
            listOf("/bin/sh", "-c", command)
        }
        val builder = ProcessBuilder(commands)
        builder.directory(File(System.getProperty("user.dir")))
        return builder.start()
    }
}
