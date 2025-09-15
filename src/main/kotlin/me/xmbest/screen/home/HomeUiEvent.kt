package me.xmbest.screen.home

sealed class HomeUiEvent {
    class InputKey(val key: Int) : HomeUiEvent()
    object ShowStatusbar : HomeUiEvent()
    object HideStatusbar : HomeUiEvent()
    object ClearLogcat : HomeUiEvent()
    object Reboot : HomeUiEvent()
    object OpenSettings : HomeUiEvent()
    object OpenWifiAdb : HomeUiEvent()
    object ScreenShot : HomeUiEvent()
    object FindCurrentActivity : HomeUiEvent()
    object ClearCurrentActivity : HomeUiEvent()
}