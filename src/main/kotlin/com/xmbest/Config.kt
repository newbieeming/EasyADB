package com.xmbest

import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import com.xmbest.model.Theme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.*

object Config {

    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    const val STRINGS_NAME = "strings"


    private val _windowState = MutableStateFlow(
        WindowState(
            position = WindowPosition.Aligned(Alignment.Center),
            size = DpSize(1280.dp, 720.dp)
        )
    )

    private val _locale = MutableStateFlow(Locale.CHINA)
    val locale = _locale.asStateFlow()

    val windowState = _windowState.asStateFlow()

    private val _theme = MutableStateFlow<Theme>(Theme.System)

    val theme = _theme.asStateFlow()


     fun changeTheme(newTheme: Theme) {
        _theme.update { newTheme }
    }

}