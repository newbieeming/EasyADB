package me.xmbest.screen.navigation

sealed interface NaviRoute

data object HomeRoute : NaviRoute
data object AppRoute : NaviRoute
data object FileRoute : NaviRoute
data object CustomerRoute : NaviRoute
data object SettingsRoute : NaviRoute
