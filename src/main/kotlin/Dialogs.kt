import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.window.AwtWindow
import androidx.compose.ui.window.FrameWindowScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.swing.Swing
import org.apache.logging.log4j.kotlin.logger
import java.awt.FileDialog
import java.io.File
import java.nio.file.Path
import javax.swing.JFileChooser
import javax.swing.filechooser.FileSystemView

@Composable
fun FrameWindowScope.FileDialog(
    title: String,
    isLoad: Boolean,
    onResult: (result: Path?) -> Unit
) = AwtWindow(
    create = {
        object : FileDialog(window, title, if (isLoad) LOAD else SAVE) {
            override fun setVisible(value: Boolean) {
                super.setVisible(value)
                if (value) {
                    if (file != null) {
                        onResult(File(directory).resolve(file).toPath())
                    } else {
                        onResult(null)
                    }
                }
            }
        }.apply {
            this.title = title
        }
    },
    dispose = FileDialog::dispose
)

@Composable
fun FrameWindowScope.FileChooserDialog(
    title: String,
    onResult: (result: File) -> Unit
) {
    val scope = rememberCoroutineScope()
    DisposableEffect(this) {
        val job = scope.launch(Dispatchers.Swing) {

            logger.info("Opening filechooser...")
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
            }
        }

        onDispose {
            job.cancel()
        }
    }

}