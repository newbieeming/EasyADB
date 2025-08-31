import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.xmbest.Config
import com.xmbest.model.Theme
import com.xmbest.module.InitModule
import com.xmbest.screen.router.RouterScreen

@OptIn(InternalComposeUiApi::class)
@Composable
@Preview
fun App() {

    val theme = Config.theme.collectAsState().value

    MaterialTheme(
        colors =
            if (theme == Theme.System)
                if (isSystemInDarkTheme()) Theme.Night.color else Theme.Light.color
            else
                Config.theme.value.color
    ) {
        RouterScreen()
    }
}

fun main() = application {
    InitModule.init()
    val windowState = Config.windowState.collectAsState()
    Window(title = "EasyADB", onCloseRequest = ::exitApplication, state = windowState.value) {
        App()
    }
}