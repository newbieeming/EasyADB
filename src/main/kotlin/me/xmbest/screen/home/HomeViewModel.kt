package me.xmbest.screen.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.VolumeDown
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.filled.*
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
                Triple(getString("key.taskList"), Icons.Default.ClearAll, 187),
                Triple(getString("key.home"), Icons.Outlined.Home, 3),
                Triple(getString("key.back"), Icons.AutoMirrored.Outlined.ArrowBack, 4),
                Triple(getString("key.lockScreen"), Icons.Outlined.Lock, 26),
                Triple(getString("key.volumeUp"), Icons.AutoMirrored.Outlined.VolumeUp, 24),
                Triple(getString("key.volumeDown"), Icons.AutoMirrored.Outlined.VolumeDown, 25),
                Triple(getString("key.brightnessUp"), Icons.Outlined.Add, 221),
                Triple(getString("key.brightnessDown"), Icons.Outlined.Minimize, 220)
            ),
            actionList = listOf(
                HomeActionItem(
                    titleKey = "home.currentActivity",
                    icon = Icons.Default.Search,
                    action = HomeAction.CURRENT_ACTIVITY
                ),
                HomeActionItem(
                    titleKey = "home.reboot",
                    icon = Icons.Default.Replay,
                    action = HomeAction.REBOOT,
                    needsConfirmation = true,
                    confirmationMessageKey = "home.reboot.confirm"
                ),
                HomeActionItem(
                    titleKey = "home.screenshot",
                    icon = Icons.Default.FilterCenterFocus,
                    action = HomeAction.SCREENSHOT
                ),
                HomeActionItem(
                    titleKey = "home.wifiAdb",
                    icon = Icons.Outlined.BugReport,
                    action = HomeAction.WIFI_ADB
                ),
                HomeActionItem(
                    titleKey = "home.nativeSettings",
                    icon = Icons.Outlined.Settings,
                    action = HomeAction.NATIVE_SETTINGS
                ),
                HomeActionItem(
                    titleKey = "home.clearLogcat",
                    icon = Icons.Outlined.DeleteSweep,
                    action = HomeAction.CLEAR_LOGCAT
                ),
                HomeActionItem(
                    titleKey = "home.showStatusbar",
                    icon = Icons.Default.KeyboardDoubleArrowDown,
                    action = HomeAction.SHOW_STATUSBAR
                ),
                HomeActionItem(
                    titleKey = "home.hideStatusbar",
                    icon = Icons.Default.KeyboardDoubleArrowUp,
                    action = HomeAction.HIDE_STATUSBAR
                )
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
                is HomeUiEvent.ExecuteAction -> handleAction(event.action)
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

    private suspend fun handleAction(action: HomeAction) {
        when (action) {
            HomeAction.CURRENT_ACTIVITY -> handleFindCurrentActivity()
            HomeAction.REBOOT -> DeviceOperate.reboot()
            HomeAction.SCREENSHOT -> handleScreenShot()
            HomeAction.WIFI_ADB -> DeviceOperate.tcpip()
            HomeAction.NATIVE_SETTINGS -> DeviceOperate.openSettings()
            HomeAction.CLEAR_LOGCAT -> DeviceOperate.logcatC()
            HomeAction.SHOW_STATUSBAR -> DeviceOperate.controlStatusbar(true)
            HomeAction.HIDE_STATUSBAR -> DeviceOperate.controlStatusbar(false)
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
