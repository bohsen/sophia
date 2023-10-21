import okio.buffer
import okio.source
import org.apache.logging.log4j.kotlin.logger
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

    companion object {
        operator fun invoke(): ProcessHandler = ProcessHandlerImpl()
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
    private val logger = logger()

    override fun execute(vararg command: String): CommandOutput {
        val builder = ProcessBuilder()
        builder.redirectErrorStream(true)
        val process = builder.command(*command).start()

        val exitCode = process.waitFor(3L, TimeUnit.SECONDS)
        var logs: String? = null
        logger.info {
            process.inputStream.source().buffer().use { source ->
                logs = source.readUtf8()
            }
            "Command result: $logs"
        }
        return CommandOutput(exitCode, logs)
    }
}