package me.xmbest.screen.app

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.HdrAuto
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Workspaces
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import me.xmbest.base.BaseViewModel
import me.xmbest.ddmlib.DeviceOperate
import me.xmbest.ddmlib.DeviceOperate.forceStop
import me.xmbest.ddmlib.DeviceOperate.kill

class AppViewModel : BaseViewModel<AppUiState>() {
    override val _uiState = MutableStateFlow(
        AppUiState(
            buttonList = listOf(
                ButtonInfo(
                    icon = Icons.Filled.Search,
                    description = "Search",
                    isSelected = { false },
                    onClick = {
                        onEvent(AppUiEvent.ChangeFilter(null))
                    }
                ),
                ButtonInfo(
                    icon = Icons.Filled.Android,
                    description = "Apps",
                    isSelected = { uiState.value.mode == AppShowMode.AppMode },
                    onClick = {
                        onEvent(AppUiEvent.ChangeAppMode(AppShowMode.AppMode))
                    }
                ),
                ButtonInfo(
                    icon = Icons.Filled.Workspaces,
                    description = "Process",
                    isSelected = { uiState.value.mode == AppShowMode.ProcessMode },
                    onClick = {
                        onEvent(AppUiEvent.ChangeAppMode(AppShowMode.ProcessMode))
                    }
                ),
                ButtonInfo(
                    icon = Icons.Filled.HdrAuto,
                    description = "Auto",
                    isSelected = { uiState.value.auto },
                    onClick = {
                        onEvent(AppUiEvent.ChangeAuto)
                    }
                ),
            )
        )
    )

    private var loadProcessJob: Job? = null
    private var autoSyncJob: Job? = null

    fun onEvent(event: AppUiEvent) {
        viewModelScope.launch(Dispatchers.IO) {
            when (event) {
                is AppUiEvent.ChangeFilter -> updateFilter(event.filter)
                is AppUiEvent.ChangeAuto -> updateAuto()
                is AppUiEvent.ChangeAppMode -> updateAppMode(event.mode)
                is AppUiEvent.Show -> show()
                is AppUiEvent.Dispose -> dispose()
                is AppUiEvent.Kill -> kill(event.pids)
                is AppUiEvent.ForceStop -> forceStop(event.applicationId)
            }
        }
    }

    private fun show() {
        loadPrecessList()
        createAutoJob()
    }

    private fun dispose() {
        loadProcessJob?.cancel()
        autoSyncJob?.cancel()
    }

    private fun updateFilter(filter: String?) {
        val realFilter = filter ?: uiState.value.filter
        _uiState.value = _uiState.value.copy(filter = realFilter)
        if (uiState.value.mode == AppShowMode.ProcessMode) {
            loadPrecessList()
        }
    }

    private fun updateAppMode(appMode: AppShowMode) {
        _uiState.value = _uiState.value.copy(mode = appMode)
        if (appMode == AppShowMode.ProcessMode) {
            loadPrecessList()
        }
    }

    private fun updateAuto() {
        _uiState.value = _uiState.value.copy(auto = !uiState.value.auto)
        createAutoJob()
    }

    private fun createAutoJob() {
        autoSyncJob?.cancel()
        if (uiState.value.auto) {
            autoSyncJob = viewModelScope.launch(Dispatchers.IO) {
                while (isActive) {
                    if (uiState.value.mode == AppShowMode.ProcessMode) {
                        loadPrecessList()
                    }
                    delay(1500)
                }
            }
        }
    }

    private fun loadPrecessList() {
        loadProcessJob?.cancel()
        loadProcessJob = viewModelScope.launch(Dispatchers.IO) {
            DeviceOperate.getProcessList(uiState.value.filter).let {
                _uiState.value = _uiState.value.copy(processList = it)
            }
        }
    }
}