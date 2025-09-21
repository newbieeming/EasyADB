package me.xmbest.screen.app

import androidx.compose.ui.graphics.vector.ImageVector
import me.xmbest.ddmlib.ProcessInfo

data class AppUiState(
    val filter: String = "",
    val auto: Boolean = true,
    val mode: AppShowMode = AppShowMode.ProcessMode,
    val buttonList: List<ButtonInfo> = emptyList(),
    val processList: List<ProcessInfo> = emptyList(),
    val appList: List<ProcessInfo> = emptyList(),
)

sealed class AppShowMode {
    object AppMode : AppShowMode()
    object ProcessMode : AppShowMode()
}

data class ButtonInfo(
    val description: String,
    val icon: ImageVector,
    val isSelected: () -> Boolean,
    val onClick: () -> Unit
)