import okio.buffer
import okio.source
import org.apache.logging.log4j.kotlin.logger
import java.util.*


/**
 * Interface for classes that execute shell commands
 */
interface ProcessHandler {
    /**
     * Execute a shell command.
     * Calls to this function is likely to block the active thread. Callers
     * should be aware of this and take appropriate precautions.
     */
    fun execute(vararg command: String): CommandOutput

    /**
     * Execute a shell command.
     * Calls to this function is likely to block the active thread. Callers
     * should be aware of this and take appropriate precautions.
     */
    fun execute(processParser: ProcessParser): CommandOutput

    companion object {
        operator fun invoke(): ProcessHandler = ProcessHandlerImpl()
    }

    interface ProcessParser {
        val command: List<String>
        fun parse(): CommandOutput

        companion object {
            operator fun invoke(vararg command: String): ProcessParser = DefaultProcessParser(*command)

            private val isWindows = System.getProperty("os.name")
                .lowercase(Locale.getDefault()).startsWith("windows")

            internal val python = if (isWindows) {
                "python.exe"
            } else {
                "python3"
            }

            internal val resolvePythonScriptPath = getFilePath("sg-upload-v2-wrapper.py")
            internal val user = readFile("user.txt")
            internal val pwFromFile: String = getFilePath("pw.txt")

            private fun getFilePath(filename: String): String {
                val classLoader = this::class.java.classLoader
                val path = classLoader.getResource(filename)?.path
                return path.orEmpty()
            }

            private fun readFile(filename: String): String {
                val classLoader = this::class.java
                val file = classLoader.getResourceAsStream(filename).source().buffer()
                return file.readUtf8()
            }
        }
    }
}

private class DefaultProcessParser(vararg command: String) : ProcessHandler.ProcessParser {

    override val command: List<String> = listOf(*command)

    override fun parse(): CommandOutput {

        val builder = ProcessBuilder()

        val process = builder.command(command).start()

        val logs = process.inputStream.bufferedReader().lines().toList()
        val error = process.errorStream.bufferedReader().lines().toList()

        val exitCode = process.onExit().get().exitValue()

        return if (error.isEmpty() && exitCode == 0) {
            logger.info { logs }
            CommandOutput.Success(logs.first())
        } else if (error.isNotEmpty() && exitCode == 0) {
            logger.error { error }
            CommandOutput.Error(error.first())
        } else {
            logger.error { logs }
            CommandOutput.FailedCommand(logs.toString())
        }
    }
}

/**
 * Wrapper class for exposing the result of a shell command
 */
sealed interface CommandOutput {
    data class Success(val logMessage: String?) : CommandOutput
    data class Error(val error: String) : CommandOutput
    data class FailedCommand(val error: String) : CommandOutput
}

/**
 * Default implementation for executing shell commands. Uses ProcessBuilder internally.
 */
private class ProcessHandlerImpl : ProcessHandler {
    override fun execute(vararg command: String): CommandOutput {
        return DefaultProcessParser(*command).parse()
    }

    override fun execute(processParser: ProcessHandler.ProcessParser): CommandOutput {
        return processParser.parse()
    }
}