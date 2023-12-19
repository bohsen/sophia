import org.apache.logging.log4j.kotlin.logger
import java.util.*
import java.util.concurrent.TimeUnit


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

            internal val isWindows = System.getProperty("os.name")
                .lowercase(Locale.getDefault()).startsWith("windows")

            internal val python = if (isWindows) {
                "python.exe"
            } else {
                "python3"
            }

            internal val resolvePythonScriptPath = getFilePath("sg-upload-v2-wrapper.py")

            internal val pwFromFile: String = getFilePath("pw.txt")

            internal fun getFilePath(filename: String): String {
                val classLoader = this::class.java.classLoader
                val path = classLoader.getResource(filename)?.path
                return path.orEmpty()
            }
        }
    }
}

private class DefaultProcessParser(vararg command: String) : ProcessHandler.ProcessParser {

    override val command: List<String> = listOf(*command)

    override fun parse(): CommandOutput {
        logger.debug {
            "Execute command: ${command.reduce { a, b -> "$a $b" }}"
        }

        val builder = ProcessBuilder()
        builder.redirectErrorStream(true)

        val process = builder.command(command).start()

        val exitCode = process.waitFor(3L, TimeUnit.SECONDS)

        return CommandOutput(exitCode, "Test")
    }
}

/**
 * Wrapper class for exposing the result of a shell command
 */
data class CommandOutput(val exitCode: Boolean, val logs: String?)

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