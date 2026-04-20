package me.xmbest.ddmlib

import com.android.ddmlib.AndroidDebugBridge
import com.android.ddmlib.IDevice
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

object DeviceManager {
    private const val TAG = "DeviceManager"
    private val _adbPath = MutableStateFlow("adb")
    val adbPath = _adbPath.asStateFlow()
    private val _adbExecutablePath = MutableStateFlow("adb")
    val adbExecutablePath = _adbExecutablePath.asStateFlow()
    private val coroutineScope =
        CoroutineScope(Dispatchers.IO + SupervisorJob() + CoroutineName(TAG))

    private var refreshDevicesJob: Job? = null
    private val _devices = MutableStateFlow<Set<IDevice>>(emptySet())

    private val _device = MutableStateFlow<IDevice?>(null)
    val device = _device.asStateFlow()

    /**
     * 当前链接的设备列表
     */
    val devices = _devices.asStateFlow()

    private val listener = object :
        AndroidDebugBridge.IDeviceChangeListener {
        override fun deviceConnected(device: IDevice?) {
            Log.d(TAG, "deviceConnected device.name: ${device?.name}")
            refreshDevices()
        }

        override fun deviceDisconnected(device: IDevice?) {
            Log.d(TAG, "deviceDisconnected device.name: ${device?.name}")
            refreshDevices()
        }

        override fun deviceChanged(
            device: IDevice?,
            changeMask: Int
        ) {
            Log.d(TAG, "deviceChanged device.name: ${device?.name},changeMask: $changeMask")
            refreshDevices()
        }
    }

    init {
        coroutineScope.launch {
            devices.collectLatest {
                if (it.isEmpty()) {
                    _device.update { null }
                } else if (device.value == null || device.value !in devices.value) {
                    _device.update { devices.value.first() }
                }
            }
        }
    }

    /**
     * 初始化/切换 adb执行环境
     * @param path adb 路径
     */
    fun initialize(path: String) {
        val safePath = path.trim().removeSurrounding("\"")
        val resolveResult = AdbPathResolver.resolveAdbPath(safePath)
        val executablePath = resolveResult.path
        _adbPath.update { safePath }
        _adbExecutablePath.update { executablePath }
        AndroidDebugBridge.terminate()
        AndroidDebugBridge.addDeviceChangeListener(listener)
        AndroidDebugBridge.init(false)
        try {
            AndroidDebugBridge.createBridge(
                executablePath,
                true,
                5000L,
                TimeUnit.MILLISECONDS
            )
            refreshDevices()
        } catch (e: IllegalArgumentException) {
            Log.e(
                TAG,
                "Failed to initialize adb bridge. configuredPath=$safePath, executablePath=$executablePath",
                e
            )
            _devices.update { emptySet() }
            _device.update { null }
        }
    }

    /**
     * 刷新设备
     */
    fun refreshDevices() {
        refreshDevicesJob?.cancel()
        refreshDevicesJob = coroutineScope.launch {
            val bridge = AndroidDebugBridge.getBridge()
            Log.d(TAG, "isConnected = ${bridge?.isConnected},size = ${bridge?.devices?.size}")
            if (bridge?.isConnected == true) {
                _devices.update { bridge.devices.filter { it.state == IDevice.DeviceState.ONLINE }.toSet() }
            } else {
                _devices.update { emptySet() }
            }
        }
    }

    /**
     * 更新选中设备，即需要执行对应命令的设备
     * @param iDevice 设备
     */
    fun changeDevice(iDevice: IDevice) {
        if (devices.value.contains(iDevice)) {
            _device.update { iDevice }
        }
    }
}
