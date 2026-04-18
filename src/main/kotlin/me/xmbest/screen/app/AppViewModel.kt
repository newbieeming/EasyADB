package me.xmbest.screen.app

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import me.xmbest.base.BaseViewModel
import me.xmbest.ddmlib.DeviceManager
import me.xmbest.ddmlib.DeviceOperate
import me.xmbest.ddmlib.DeviceOperate.clear
import me.xmbest.ddmlib.DeviceOperate.forceStop
import me.xmbest.ddmlib.DeviceOperate.kill
import me.xmbest.ddmlib.DeviceOperate.startApp
import me.xmbest.ddmlib.DeviceOperate.uninstall
import me.xmbest.ddmlib.Log
import me.xmbest.util.PreferencesUtil
import kotlin.system.measureTimeMillis

class AppViewModel : BaseViewModel<AppUiState>() {

    companion object {
        private const val TAG = "AppViewModel"
    }

    override val _uiState =
        MutableStateFlow(
            AppUiState(
                filter = PreferencesUtil.get(PreferencesUtil.PREFERENCES_APP_FILTER, ""),
                auto = PreferencesUtil.get(PreferencesUtil.PREFERENCES_APP_AUTO, true),
                third = PreferencesUtil.get(PreferencesUtil.PREFERENCES_APP_THIRD, false),
                mode = when (PreferencesUtil.get(PreferencesUtil.PREFERENCES_APP_MODE, "ProcessMode")) {
                    "AppMode" -> AppShowMode.AppMode
                    else -> AppShowMode.ProcessMode
                },
                buttonList = listOf(
                    ButtonInfo(
                        icon = Icons.Filled.Search,
                        description = getString("app.find"),
                        isSelected = { false },
                        isShow = { true },
                        onClick = {
                            onEvent(AppUiEvent.Settings.ChangeFilter(null))
                        }), ButtonInfo(
                        icon = Icons.Filled.HdrAuto,
                        description = getString("app.autoRefresh"),
                        isSelected = { uiState.value.auto },
                        isShow = { uiState.value.mode == AppShowMode.ProcessMode },
                        onClick = {
                            onEvent(AppUiEvent.Settings.ChangeAuto)
                        }), ButtonInfo(
                        icon = Icons.Filled.Looks3,
                        description = getString("app.thirdParty"),
                        isSelected = { uiState.value.third },
                        isShow = { uiState.value.mode == AppShowMode.AppMode },
                        onClick = {
                            onEvent(AppUiEvent.Settings.ChangeThird)
                        }), ButtonInfo(
                        icon = Icons.Filled.Android,
                        description = getString("app.appList"),
                        isSelected = { uiState.value.mode == AppShowMode.AppMode },
                        isShow = { true },
                        onClick = {
                            onEvent(AppUiEvent.Settings.ChangeAppMode(AppShowMode.AppMode))
                        }), ButtonInfo(
                        icon = Icons.Filled.Workspaces,
                        description = getString("app.processList"),
                        isSelected = { uiState.value.mode == AppShowMode.ProcessMode },
                        isShow = { true },
                        onClick = {
                            onEvent(AppUiEvent.Settings.ChangeAppMode(AppShowMode.ProcessMode))
                        })
                )
            )
        )

    private var loadProcessJob: Job? = null
    private var loadAppsJob: Job? = null
    private var autoSyncJob: Job? = null

    fun onEvent(event: AppUiEvent) {
        viewModelScope.launch(Dispatchers.IO) {
            when (event) {
                is AppUiEvent.Settings -> handleSettingsEvent(event)
                is AppUiEvent.Lifecycle -> handleLifecycleEvent(event)
                is AppUiEvent.AppOperation -> handleAppOperationEvent(event)
            }
        }
    }

    private fun handleSettingsEvent(event: AppUiEvent.Settings) {
        when (event) {
            is AppUiEvent.Settings.ChangeFilter -> updateFilter(event.filter)
            is AppUiEvent.Settings.ChangeAuto -> updateAuto()
            is AppUiEvent.Settings.ChangeThird -> updateThird()
            is AppUiEvent.Settings.ChangeAppMode -> updateAppMode(event.mode)
        }
    }

    private fun handleLifecycleEvent(event: AppUiEvent.Lifecycle) {
        when (event) {
            is AppUiEvent.Lifecycle.Show -> show()
            is AppUiEvent.Lifecycle.Dispose -> dispose()
        }
    }

    private suspend fun handleAppOperationEvent(event: AppUiEvent.AppOperation) {
        when (event) {
            is AppUiEvent.AppOperation.Kill -> kill(event.pids)
            is AppUiEvent.AppOperation.ForceStop -> forceStop(event.applicationId)
            is AppUiEvent.AppOperation.StartApp -> startApp(event.packageName)
            is AppUiEvent.AppOperation.ClearData -> clear(event.packageName)
            is AppUiEvent.AppOperation.Uninstall -> uninstall(event.packageName)
        }
    }

    private fun show() {
        loadPrecessList()
        loadAppList()
        createAutoJob()
    }

    private fun dispose() {
        loadProcessJob?.cancel()
        loadAppsJob?.cancel()
        autoSyncJob?.cancel()
    }

    private fun updateFilter(filter: String?) {
        val realFilter = filter ?: uiState.value.filter
        _uiState.value = _uiState.value.copy(filter = realFilter)
        // 保存过滤器到存储
        PreferencesUtil.set(PreferencesUtil.PREFERENCES_APP_FILTER, realFilter)
        if (uiState.value.mode == AppShowMode.ProcessMode) {
            loadPrecessList()
        } else {
            loadAppList()
        }
    }

    private fun updateAppMode(appMode: AppShowMode) {
        _uiState.value = _uiState.value.copy(mode = appMode)
        // 保存应用模式到存储
        val modeString = when (appMode) {
            is AppShowMode.AppMode -> "AppMode"
            is AppShowMode.ProcessMode -> "ProcessMode"
        }
        PreferencesUtil.set(PreferencesUtil.PREFERENCES_APP_MODE, modeString)
        if (appMode == AppShowMode.ProcessMode) {
            loadPrecessList()
            createAutoJob()
        } else {
            loadAppList()
        }
    }

    private fun updateAuto() {
        val newAutoValue = !uiState.value.auto
        _uiState.value = _uiState.value.copy(auto = newAutoValue)
        // 保存自动刷新开关到存储
        PreferencesUtil.set(PreferencesUtil.PREFERENCES_APP_AUTO, newAutoValue)
        createAutoJob()
    }

    private fun updateThird() {
        val newValue = !uiState.value.third
        _uiState.value = _uiState.value.copy(third = newValue)
        // 保存自动刷新开关到存储
        PreferencesUtil.set(PreferencesUtil.PREFERENCES_APP_THIRD, newValue)
        loadAppList()
    }

    private fun createAutoJob() {
        autoSyncJob?.cancel()
        if (uiState.value.auto) {
            autoSyncJob = viewModelScope.launch(Dispatchers.IO) {
                while (isActive && uiState.value.mode == AppShowMode.ProcessMode) {
                    loadPrecessList()
                    delay(1500)
                }
            }
        }
    }

    private fun loadPrecessList() {
        loadProcessJob?.cancel()
        if (DeviceManager.device.value == null) return
        loadProcessJob = viewModelScope.launch(Dispatchers.IO) {
            val timeMillis = measureTimeMillis {
                DeviceOperate.getProcessList(uiState.value.filter).let {
                    _uiState.value = _uiState.value.copy(processList = it)
                }
            }
            Log.d(TAG, "Process list loaded in $timeMillis ms")
        }
    }

    private fun loadAppList() {
        loadAppsJob?.cancel()
        loadAppsJob = viewModelScope.launch(Dispatchers.IO) {
            val timeMillis = measureTimeMillis {
                DeviceOperate.getAppList(uiState.value.filter, uiState.value.third).let {
                    _uiState.value = _uiState.value.copy(appList = it)
                }
            }
            Log.d(TAG, "App list loaded in $timeMillis ms")
        }
    }
}