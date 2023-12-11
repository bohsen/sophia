import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.*
import org.apache.logging.log4j.kotlin.logger
import theme.BackgroundColor
import theme.md_theme_light_onPrimaryContainer

fun main() = application {
    App()
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
@Preview
fun ApplicationScope.App() {
    // val watchService = WatchService()
    val presenter = AppPresenter()
    val scope = rememberCoroutineScope()

    val showSettings = remember { mutableStateOf(false) }
    val openLogs = remember { mutableStateOf(false) }


    Tray(onOpenSettings = { showSettings.value = true },
        onOpenLogs = { openLogs.value = true })

    if (showSettings.value) {
        logger.info("Opening settings-dialog")
        val windowState = rememberWindowState(width = 400.dp, height = 600.dp)
        Window(
            title = "",
            state = windowState,
            onCloseRequest = { showSettings.value = false },
            onPreviewKeyEvent = {
                if (it.key == Key.Escape && it.type == KeyEventType.KeyDown) {
                    showSettings.value = false
                    true
                } else {
                    false
                }
            }
        ) {
            SettingsScreen(onCloseRequest = { showSettings.value = false })
        }
    }
}


@Composable
fun ApplicationScope.Tray(
    onOpenSettings: () -> Unit,
    onOpenLogs: () -> Unit,
    onCloseRequest: () -> Unit = ::exitApplication
) {
    if (isTraySupported) {
        Tray(
            painterResource("icon_jakob.png"),
            state = rememberTrayState(),
            tooltip = "SOPHiA plugin",
            menu = {
                ApplicationMenu(
                    onOpenSettings = onOpenSettings,
                    onOpenLogs = onOpenLogs,
                    onCloseRequest = onCloseRequest,
                )
            }
        )
    }
}

@Composable
@Preview
private fun LogsListView(modifier: Modifier = Modifier, logs: List<CommandOutput>) {
    LazyColumn(modifier) {
        items(logs) {
            Box(
                Modifier.padding(bottom = 5.dp)
                    .background(
                        BackgroundColor, shape = RoundedCornerShape(100.dp)
                    )
            ) {
                Text(
                    text = it.logs.toString(),
                    color = md_theme_light_onPrimaryContainer,
                    modifier = Modifier.padding(5.dp),
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun MenuScope.ApplicationMenu(
    onOpenSettings: () -> Unit,
    onOpenLogs: () -> Unit,
    onCloseRequest: () -> Unit
) {
    Item("Indstillinger", onClick = onOpenSettings)
    Item("Logfiler", onClick = onOpenLogs)
    Separator()
    Item("Afslut", onClick = onCloseRequest)
}

sealed interface Model {
    data object Initializing : Model
    data class Processing(val logs: List<String>, val uploads: List<String>)
}