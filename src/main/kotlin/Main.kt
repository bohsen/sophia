import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

@Composable
@Preview
fun App() {
    var appbarTitle by remember { mutableStateOf("SOPHiA Plugin") }

    MaterialTheme {
        MainView(appbarTitle)
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}

@Composable
@Preview
fun MainView(appbarTitle: String) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = appbarTitle)
                }
            )
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
