package me.xmbest.screen.navigation

import com.android.ddmlib.IDevice

data class NaviUiState(
    /**
     * 当前页面
     */
    val currentRoute: NaviRoute = HomeRoute,
    /**
     * 是否展示设备列表
     */
    val devicesListShow: Boolean = false,
    val device: IDevice? = null,
    val devices: Set<IDevice> = emptySet()
)
