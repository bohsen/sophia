import com.russhwolf.settings.MapSettings
import okio.buffer
import okio.source
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.FileNotFoundException

class SophiaClientTest {
    private val fakeProcessHandler = FakeProcessHandler()
    private val provider = { MapSettings() }
    private val settings = KeyValueStore(provider)
    private val service = SophiaClient(fakeProcessHandler, settings)

    @JvmField
    @Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun `when python installed should not report an error`() {
        val pythonInstalled = service.dependencyCheck()
        assertTrue(pythonInstalled.exitCode)
    }

    @Test
    fun `when processhandler throws should error in client`() {
        fakeProcessHandler.function = { throw InterruptedException("Manual throw") }
        assertThrows(InterruptedException::class.java) { service.dependencyCheck() }
    }

    @Test
    fun `when executed command fails should report failed output`() {
        val expectedCommandOutput = CommandOutput(false, "Unknown option: '-userInfo'")
        fakeProcessHandler.function = { expectedCommandOutput }
        assertEquals(expectedCommandOutput, service.dependencyCheck())
    }

    @Test
    fun `when login with correct user should ask for token`() {
        val expectedCommandOutput = CommandOutput(true, "")
        val loginResult = service.login(getStringFromFile("sophia.txt", true))
        assertEquals(expectedCommandOutput, loginResult)
    }

    private fun getResourceAsStream(file: String, debug: Boolean = false): String {
        val classLoader = this.javaClass.classLoader
        if (classLoader != null) {
            try {
                val inputString =
                    this.javaClass.classLoader.getResourceAsStream(file)!!.source().buffer().use { source ->
                        source.readUtf8()
                    }
                if (debug) println("Output from inputfile is: $inputString")
                return inputString
            } catch (e: FileNotFoundException) {
                println("Could not find the specified file: $file")
                throw e
            }
        } else {
            throw IllegalStateException(
                """Classloader is null. Can't open an inputstream for the specified file: $file without it."""
            )
        }
    }

    internal fun getStringFromFile(filePath: String, debug: Boolean = false): String {
        val classLoader = this.javaClass.classLoader
        if (classLoader != null) {
            try {
                val inputString = classLoader.getResourceAsStream(filePath).bufferedReader().use { it.readText() }
                if (debug) println("Output from inputfile is: $inputString")
                return inputString
            } catch (e: FileNotFoundException) {
                println("Could not find the specified file: $filePath")
                throw e
            }
        } else {
            throw IllegalStateException(
                """Classloader is null. Can't open an inputstream for the specified file: $filePath without it."""
            )
        }
    }

    class FakeProcessHandler : ProcessHandler {
        private val delegate = ProcessHandler()
        var function: ((Any) -> CommandOutput)? = null
        override fun execute(vararg command: String): CommandOutput {
            return function?.invoke(command) ?: delegate.execute(*command)
        }
    }
}