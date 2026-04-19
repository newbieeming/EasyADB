package me.xmbest.screen.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Toll
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewModelScope
import com.android.ddmlib.IDevice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import me.xmbest.appStorageAbsolutePath
import me.xmbest.base.BaseViewModel
import me.xmbest.cmdAutoCloseEnabled
import me.xmbest.cmdAutoCloseTimeoutSeconds
import me.xmbest.ddmlib.DeviceManager
import me.xmbest.ddmlib.DeviceOperate
import me.xmbest.exec
import me.xmbest.screen.app.AppScreen
import me.xmbest.screen.customer.CustomerScreen
import me.xmbest.screen.file.FileScreen
import me.xmbest.screen.home.HomeScreen
import me.xmbest.screen.settings.SettingsScreen
import java.io.File

class NaviViewModule : BaseViewModel<NaviUiState>() {
    val destinations = listOf(
        NaviDestination(
            route = HomeRoute,
            name = getString("router.item.commonFeatures"),
            icon = Icons.Outlined.Category
        ) {
            HomeScreen()
        },
        NaviDestination(
            route = AppRoute,
            name = getString("router.item.appManagement"),
            icon = Icons.Outlined.GridView
        ) {
            AppScreen()
        },
        NaviDestination(
            route = FileRoute,
            name = getString("router.item.fileManagement"),
            icon = Icons.Outlined.FolderOpen
        ) {
            FileScreen()
        },
        NaviDestination(
            route = CustomerRoute,
            name = getString("router.item.quickActions"),
            icon = Icons.Outlined.Toll
        ) {
            CustomerScreen()
        },
        NaviDestination(
            route = SettingsRoute,
            name = getString("router.item.settings"),
            icon = Icons.Outlined.Settings
        ) { SettingsScreen() }
    )

    override val _uiState = MutableStateFlow(NaviUiState())

    init {
        viewModelScope.launch(Dispatchers.Default) {
            DeviceManager.device.collectLatest {
                _uiState.value = _uiState.value.copy(device = it)
            }
        }

        viewModelScope.launch(Dispatchers.Default) {
            DeviceManager.devices.collectLatest {
                _uiState.value = _uiState.value.copy(devices = it)
            }
        }
    }

    fun onEvent(event: NaviUiEvent) {
        when (event) {
            is NaviUiEvent.Navigation -> handleNavigationEvent(event)
            is NaviUiEvent.DeviceManagement -> handleDeviceManagementEvent(event)
        }
    }

    private fun handleNavigationEvent(event: NaviUiEvent.Navigation) {
        when (event) {
            is NaviUiEvent.Navigation.SelectDestination -> selectDestination(event.route)
        }
    }

    private fun handleDeviceManagementEvent(event: NaviUiEvent.DeviceManagement) {
        when (event) {
            is NaviUiEvent.DeviceManagement.SelectDevice -> selectDevice(event.device)
            is NaviUiEvent.DeviceManagement.ShowDeviceList -> showDeviceList(event.show)
            is NaviUiEvent.DeviceManagement.RefreshDevice -> refreshDevice()
            is NaviUiEvent.DeviceManagement.Install -> install(event.path)
        }
    }

    private fun install(path: String) {
        DeviceOperate.install(
            remoteFilePath = path,
            autoCloseEnabled = cmdAutoCloseEnabled,
            autoCloseTimeoutSeconds = cmdAutoCloseTimeoutSeconds,
            file = File(appStorageAbsolutePath, exec.second)
        )
    }

    private fun selectDestination(route: NaviRoute) {
        _uiState.value = _uiState.value.copy(currentRoute = route)
    }

    private fun showDeviceList(show: Boolean) {
        _uiState.value = _uiState.value.copy(devicesListShow = show)
    }

    private fun selectDevice(iDevice: IDevice) {
        DeviceManager.changeDevice(iDevice)
        showDeviceList(false)
    }

    private fun refreshDevice() {
        DeviceManager.refreshDevices()
    }

}

data class NaviDestination(
    val route: NaviRoute,
    val name: String,
    val icon: ImageVector,
    val content: @Composable () -> Unit
)
