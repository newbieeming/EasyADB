package me.xmbest.ddmlib

data class ProcessInfo(
    val pid: String,
    val user: String,
    val cpu: String,
    val time: String,
    val virt: String,
    val res: String,
    val shr: String,
    val mem: String,
    val name: String,
    val args: String,
)