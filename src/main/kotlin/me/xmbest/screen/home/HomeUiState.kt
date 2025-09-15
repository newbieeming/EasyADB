package me.xmbest.screen.home

import androidx.compose.ui.graphics.vector.ImageVector
import com.android.ddmlib.IDevice

data class HomeUiState(
    val device: IDevice? = null,
    val wmSize: String? = null,
    val ipAddress: String? = null,
    val memory: String? = null,
    val cpuCoreSize: String? = null,
    val currentActivity: String? = null,
    val keyEventList: List<Triple<String, ImageVector, Int>> = emptyList(),
    val actionList: List<HomeActionItem> = emptyList()
)

data class HomeActionItem(
    val titleKey: String,
    val icon: ImageVector,
    val action: HomeAction,
    val needsConfirmation: Boolean = false,
    val confirmationMessageKey: String? = null
)
