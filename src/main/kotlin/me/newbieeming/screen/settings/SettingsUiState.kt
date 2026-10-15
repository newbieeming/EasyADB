package me.newbieeming.screen.settings

import me.newbieeming.model.Theme

data class SettingsUiState(
    val adbPath: String,
    val customerAdbPath: String,
    val theme: Theme,
    val screenshotSaveEnabled: Boolean,
    val screenshotSavePath: String,
    val cmdAutoCloseEnabled: Boolean,
    val cmdAutoCloseTimeoutSeconds: Int,
    val titleBarAlpha: Float,
    val loadingMinDurationMs: Int,
    val keepRunningOnClose: Boolean,
)
