package me.xmbest.screen.app

sealed class AppUiEvent {
    class ChangeFilter(val filter: String?) : AppUiEvent()
    class ChangeAppMode(val mode: AppShowMode) : AppUiEvent()
    object ChangeAuto : AppUiEvent()
    object Show : AppUiEvent()
    object Dispose : AppUiEvent()
    class Kill(val pids: List<String>) : AppUiEvent()
    class ForceStop(val applicationId: String) : AppUiEvent()
}