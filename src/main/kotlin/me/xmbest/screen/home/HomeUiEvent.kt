package me.xmbest.screen.home

sealed class HomeUiEvent {
    class InputKey(val key: Int) : HomeUiEvent()
    class ExecuteAction(val action: HomeAction) : HomeUiEvent()
    object ShowStatusbar : HomeUiEvent()
    object HideStatusbar : HomeUiEvent()
    object Reboot : HomeUiEvent()
    object OpenSettings : HomeUiEvent()
    object OpenWifiAdb : HomeUiEvent()
    object ScreenShot : HomeUiEvent()
    object FindCurrentActivity : HomeUiEvent()
    object ClearCurrentActivity : HomeUiEvent()
}

enum class HomeAction {
    CURRENT_ACTIVITY,
    REBOOT,
    SCREENSHOT,
    WIFI_ADB,
    NATIVE_SETTINGS,
    CLEAR_LOGCAT,
    SHOW_STATUSBAR,
    HIDE_STATUSBAR
}