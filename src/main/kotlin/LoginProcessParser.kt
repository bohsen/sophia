import ProcessHandler.ProcessParser.Companion.pwFromFile
import ProcessHandler.ProcessParser.Companion.python
import ProcessHandler.ProcessParser.Companion.resolvePythonScriptPath
import ProcessHandler.ProcessParser.Companion.user
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.apache.logging.log4j.kotlin.logger
import java.io.ByteArrayInputStream
import java.util.concurrent.TimeUnit
import kotlin.jvm.optionals.toList
import kotlin.math.min


class LoginProcessParser(private val tokenCard: TokenCard) : ProcessHandler.ProcessParser {

    override val command: List<String>
        get() = listOf(
            python,
            resolvePythonScriptPath,
            "login",
            "-u",
            user,
            "-pf",
            pwFromFile
        )

    override fun parse(): CommandOutput {

        val builder = ProcessBuilder()
        val process = builder
            .command(command)
            .start()

        var coordinate = ""
        with(process) {
            val buffer = ByteArray(4096)
            while (isAlive) {

                var tokenText: String? = null
                val no: Int = inputStream.available()
                if (no > 0) {
                    val n: Int = inputStream.read(buffer, 0, min(no, buffer.size))
                    tokenText = String(buffer, 0, n)
                }

                if (tokenText != null) {
                    if (tokenText.startsWith("Provide token for coordinate")) {
                        coordinate = extractCoordinate(tokenText)
                        break
                    }
                }
            }
        }

        val token = tokenCard.getToken(coordinate)

        val writer = process.outputStream.bufferedWriter()
        writer.write(token, 0, token.length)
        writer.newLine()
        writer.flush()

        val logs = process.inputStream.bufferedReader().lines().toList()
        val error = process.errorStream.bufferedReader().lines().toList()

        val exitCode = process.waitFor(5L, TimeUnit.SECONDS)

        return if (error.isEmpty() && exitCode) {
            CommandOutput.Success(logs.first())
        } else if (error.isNotEmpty() && exitCode) {
            CommandOutput.Error(error.toString())
        } else {
            CommandOutput.FailedCommand(logs.toString())
        }
    }

    /**
     * Extracts the coordinate from the prompt-text that is presented during login using Sophia CLI
     *
     * Example input: "Provide token for coordinate [7, G]:"
     * Will return the String "7G"
     */
    internal fun extractCoordinate(tokenText: String): String {
        val first = tokenText.substringAfter("[").substringBefore(",")
        val second = tokenText.substringAfter(", ").substringBefore("]")
        return first + second
    }
}