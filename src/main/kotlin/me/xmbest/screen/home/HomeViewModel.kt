package me.xmbest.screen.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.VolumeDown
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.outlined.*
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import me.xmbest.base.BaseViewModel
import me.xmbest.ddmlib.*
import me.xmbest.ddmlib.DeviceOperate.findCurrentActivity

class HomeViewModel() : BaseViewModel<HomeUiState>() {

    companion object {
        private const val TAG = "HomeViewModel"
    }

    override val _uiState = MutableStateFlow(
        HomeUiState(
            keyEventList = listOf(
                Triple("任务列表", Icons.Default.ClearAll, 187),
                Triple("返回桌面", Icons.Outlined.Home, 3),
                Triple("返回上级", Icons.AutoMirrored.Outlined.ArrowBack, 4),
                Triple("锁定屏幕", Icons.Outlined.Lock, 26),
                Triple("增加音量", Icons.AutoMirrored.Outlined.VolumeUp, 24),
                Triple("减少音量", Icons.AutoMirrored.Outlined.VolumeDown, 25),
                Triple("增加亮度", Icons.Outlined.Add, 221),
                Triple("减少亮度", Icons.Outlined.Minimize, 220)
            )
        )
    )

    init {
        viewModelScope.launch(Dispatchers.IO) {
            DeviceManager.device.collectLatest { device ->
                runCatching {
                    _uiState.value = _uiState.value.copy(
                        device = device,
                        wmSize = wmSize(),
                        ipAddress = ipAddress(),
                        memory = memorySize(),
                        cpuCoreSize = cpuCoreSize(),
                    )
                }.onFailure {
                    Log.e(TAG, "onFailure!", it)
                    _uiState.value = _uiState.value.copy(device = device)
                }
            }
        }
    }


    fun onEvent(event: HomeUiEvent) {
        viewModelScope.launch(Dispatchers.Default) {
            when (event) {
                is HomeUiEvent.InputKey -> DeviceOperate.inputKey(event.key)
                is HomeUiEvent.ShowStatusbar -> DeviceOperate.controlStatusbar(true)
                is HomeUiEvent.HideStatusbar -> DeviceOperate.controlStatusbar(false)
                is HomeUiEvent.ClearLogcat -> DeviceOperate.logcatC()
                is HomeUiEvent.Reboot -> DeviceOperate.reboot()
                is HomeUiEvent.OpenSettings -> DeviceOperate.openSettings()
                is HomeUiEvent.OpenWifiAdb -> DeviceOperate.tcpip()
                is HomeUiEvent.ScreenShot -> handleScreenShot()
                is HomeUiEvent.FindCurrentActivity -> handleFindCurrentActivity()
                is HomeUiEvent.ClearCurrentActivity -> handleClearCurrentActivity()
            }
        }
    }

    private fun handleClearCurrentActivity(){
        _uiState.value = _uiState.value.copy(currentActivity = null)
    }


    private suspend fun handleFindCurrentActivity() {
        val activity = findCurrentActivity()
        ClipboardUtil.setSysClipboardText(activity)
        _uiState.value = _uiState.value.copy(currentActivity = activity)
    }

    private fun handleScreenShot() {
        DeviceOperate.screenshot()?.let {
            ClipboardUtil.setClipboardImage(it)
        }
    }
}
