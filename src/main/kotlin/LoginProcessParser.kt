import ProcessHandler.ProcessParser.Companion.pwFromFile
import ProcessHandler.ProcessParser.Companion.python
import ProcessHandler.ProcessParser.Companion.resolvePythonScriptPath
import kotlinx.coroutines.future.await
import kotlinx.coroutines.runBlocking
import org.apache.logging.log4j.kotlin.logger
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*
import java.util.stream.Stream


class LoginProcessParser(private val tokenCard: TokenCard) : ProcessHandler.ProcessParser {
    private val logger = logger()
    override val command: List<String>
        get() = listOf(
            python,
            resolvePythonScriptPath,
            "login",
            "-u",
            tokenCard.username,
            "-pf",
            pwFromFile
        )

    override fun parse(): CommandOutput {

        logger.debug {
            "Execute command: ${command.reduce { a, b -> "$a $b" }}"
        }

        val builder = ProcessBuilder()
        builder.redirectErrorStream(true)

        val builders = listOf(
            ProcessBuilder(command).redirectErrorStream(true),
            ProcessBuilder("xargs")
        )
        val processes = ProcessBuilder.startPipeline(
            builders
        )
        val liveProcesses: Stream<ProcessHandle> = ProcessHandle.allProcesses()
        liveProcesses.filter { obj: ProcessHandle -> obj.isAlive }
            .forEach { ph ->
                logger.info("PID: " + ph.pid())
                logger.info("Instance: " + ph.info().startInstant())
                logger.info("User: " + ph.info().user())
            }
        val last = processes.last()
        last.inputStream.use { stream ->
            InputStreamReader(stream).use { isr ->
                BufferedReader(isr).use { r ->
                    logger.debug { r.lines().count() }
                }
            }
        }

        val exitCode = runBlocking {
            last.onExit().await().exitValue()
        }


        return CommandOutput(exitCode == 0, "logs")
    }
}