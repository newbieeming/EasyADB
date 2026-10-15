package me.newbieeming.screen.settings

import me.newbieeming.model.Environment
import me.newbieeming.model.Theme

sealed class SettingsUiEvent {
    // 主题设置相关事件
    sealed class ThemeSettings : SettingsUiEvent() {
        data class UpdateTheme(val theme: Theme) : ThemeSettings()
    }

    // ADB环境设置相关事件
    sealed class AdbSettings : SettingsUiEvent() {
        data class UpdateAdbEnv(val environment: Environment) : AdbSettings()
        data object UpdateCustomerAdb : AdbSettings()
    }

    // 截图设置相关事件
    sealed class ScreenshotSettings : SettingsUiEvent() {
        data class UpdateScreenshotSaveEnabled(val enabled: Boolean) : ScreenshotSettings()
        data object UpdateScreenshotSavePath : ScreenshotSettings()
    }

    // 终端设置相关事件
    sealed class TerminalSettings : SettingsUiEvent() {
        data class UpdateCmdAutoCloseEnabled(val enabled: Boolean) : TerminalSettings()
        data class UpdateCmdAutoCloseTimeout(val seconds: Int) : TerminalSettings()
    }

    // 标题栏透明度设置相关事件
    sealed class TitleBarSettings : SettingsUiEvent() {
        data class UpdateTitleBarAlpha(val alpha: Float) : TitleBarSettings()
    }

    // 启动 Loading 设置相关事件
    sealed class LoadingSettings : SettingsUiEvent() {
        data class UpdateLoadingMinDuration(val ms: Int) : LoadingSettings()
    }

    // 窗口设置相关事件
    sealed class WindowSettings : SettingsUiEvent() {
        data class UpdateKeepRunningOnClose(val enabled: Boolean) : WindowSettings()
    }

    // 数据管理相关事件
    sealed class DataManagement : SettingsUiEvent() {
        data object ClearData : DataManagement()
    }
}
