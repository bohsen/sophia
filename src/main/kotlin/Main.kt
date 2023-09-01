import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.BottomAppBar
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.primarySurface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.MenuScope
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.isTraySupported
import androidx.compose.ui.window.rememberTrayState
import kotlinx.coroutines.launch

val appbarTitle: String
    get() = "SOPHiA Plugin"

@Composable
@Preview
fun ApplicationScope.App() {
    MaterialTheme {
        MainView()
        if (isTraySupported) {
            Tray(
                painterResource("icon_jakob.png"),
                state = rememberTrayState(),
                tooltip = "SOPHiA plugin",
                menu = { ApplicationMenu() }
            )
        }

    }
}

fun main() = application {
    val icon = painterResource("icon_jakob.png")
    Window(
        title = appbarTitle,
        onCloseRequest = ::exitApplication,
        icon = icon,
    ) {
        WindowMenuBar()
        App()
    }
}

@Composable
@Preview
private fun MainView() {
    Scaffold(
        topBar = {
            TopAppBar(
                elevation = 8.dp,
                title = {},
                backgroundColor = MaterialTheme.colors.primarySurface,
                navigationIcon = {},
                actions = {
                    IconButton(onClick = {/* Do Something*/ }) {
                        Icon(Icons.Filled.Settings, null)
                    }
                })
        },
        bottomBar = {
            BottomAppBar(elevation = 4.dp) {
                Text(text = "Her skal der fremgå hvilken mappe der lyttes på")
            }
        }
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Hovedvindue")
        }
    }
}

@Composable
private fun FrameWindowScope.WindowMenuBar() = MenuBar {
    val scope = rememberCoroutineScope()

    fun save() = scope.launch { }
    fun open() = scope.launch { }
    fun exit() = scope.launch { }

    Menu("File") {
        Item("New window", onClick = { })
        Item("Open...", onClick = { open() })
        Item("Save", onClick = { save() })
        Separator()
        Item("Exit", onClick = { exit() })
    }

    Menu("Settings") {
        Item(
            "Show tray",
            onClick = { }
        )
        Item(
            "Enter fullscreen",
            onClick = { }
        )
    }
}

@Composable
private fun MenuScope.ApplicationMenu() {
    val scope = rememberCoroutineScope()
    fun exit() = scope.launch { }

    Item("New", onClick = { })
    Separator()
    Item("Exit", onClick = { exit() })
}