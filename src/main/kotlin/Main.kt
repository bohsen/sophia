import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.*
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import kotlinx.coroutines.CoroutineScope
import org.apache.logging.log4j.kotlin.logger
import kotlin.io.path.Path

@Preview
@Composable
fun MyScreenPreview() {
    application { App() }
}

fun main() {
    //System.setProperty("java.awt.headless", "true");
    application {
        App()
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ApplicationScope.App() {
    val keyValueStore = remember { KeyValueStore() }
    val watchService = remember { WatchService() }
    val scope = rememberCoroutineScope()

    val showSettings = remember { mutableStateOf(false) }
    val openLogs = remember { mutableStateOf(false) }

    observeFileChanges(scope, keyValueStore, showSettings, watchService)

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
private fun observeFileChanges(
    scope: CoroutineScope,
    keyValueStore: KeyValueStore,
    showSettings: MutableState<Boolean>,
    watchService: WatchService
) {
    LaunchedEffect(scope) {
        keyValueStore.observablePath.collect { path ->
            when {
                path.isEmpty() -> showSettings.value = true
                else -> with(watchService) {
                    unRegisterAll()
                    registerAll(Path(path))
                    processEvents().collect { path ->
                        if (path.endsWith("CopyComplete.txt")) {
                            TODO("Extract directory path and start upload")
                        }
                    }
                }
            }
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