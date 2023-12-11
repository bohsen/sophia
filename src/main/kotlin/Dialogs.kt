import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.window.FrameWindowScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.swing.Swing
import org.apache.logging.log4j.kotlin.logger
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileSystemView

@Composable
fun FrameWindowScope.FileChooserDialog(
    title: String,
    onResult: (result: File) -> Unit,
    onCloseRequest: () -> Unit
) {
    val scope = rememberCoroutineScope()
    DisposableEffect(this) {
        val job = scope.launch(Dispatchers.Swing) {

            logger.info("Opening file-chooser...")
            JFileChooser(FileSystemView.getFileSystemView()).apply {
                currentDirectory = File(System.getProperty("user.dir"))
                dialogTitle = title
                fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
                isAcceptAllFileFilterUsed = true
                selectedFile = null
                currentDirectory = null
                when (showOpenDialog(null)) {
                    JFileChooser.APPROVE_OPTION -> {
                        logger.info { "Selected: $selectedFile" }
                        onResult(selectedFile)
                    }
                    else -> {
                        logger.info { "Cancel filechooser" }
                        onResult(File(""))
                    }
                }
                onCloseRequest()
            }
        }

        onDispose {
            job.cancel()
        }
    }

}