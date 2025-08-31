package com.xmbest.model

import androidx.compose.material.Colors
import androidx.compose.material.lightColors
import androidx.compose.ui.graphics.Color
import com.xmbest.Config
import com.xmbest.locale.PropertiesLocalization
import com.xmbest.theme.*

private val strings =
    PropertiesLocalization.create(Config.STRINGS_NAME)

sealed class Theme(val label: String, val color: Colors) {
    object Light : Theme(strings.get("theme.light"), light)
    object Night : Theme(strings.get("theme.night"), night)
    object System : Theme(
        strings.get("theme.system"), lightColors(
            primary = Color.White,
            background = Color.White,
            onPrimary = Color.Black
        )
    )

    class Other(label: String, color: Colors) : Theme(label, color)
}