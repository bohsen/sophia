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
    private val tokenMap: Map<String, List<String>> = mapOf(
        "A" to "iuh7 9iik lkmd ssuu sbna wyyh mkki aayh".split(" "),
        "B" to "nnhs yyhh aamj wpod mcjy aakl qldo siah".split(" "),
        "C" to "amaj weks 99jd ala9 smn6 aqq8 ssm8 xxuu".split(" "),
        "D" to "8a9a aakk 9w8d vmvu xxn7 65ee ddl9 xnh8".split(" "),
        "E" to "xjs7 aak9 vv87 z765 c765 xx92 117h sslk".split(" "),
        "F" to "mvnu xxb3 84fw ww66 aa83 sw78 11jr 725e".split(" "),
        "G" to "a9au a7ue ve84 slxo x763 d7d7 l93w u756".split(" "),
        "H" to "am12 lw92 ie7x m7tg 9hg6 7gbc cu72 si82".split(" "),
    )
    private val fakeTokenCard = TokenCard(14567, "Test Peter Testesen", "Feb 28, 2025", tokenMap)

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
        val expectedCommandOutput = CommandOutput(
            true,
            """Script is up-to-date (checksum 3ec45b3f8b3d5e0a691ade413e2e8f8a)
                Provide token for coordinate [3, E]:""".trimIndent()
        )
        val loginResult = service.login(getStringFromFile("sophia.txt", true), fakeTokenCard)
        assertEquals(expectedCommandOutput, loginResult)
    }

    @Test
    fun `when requesting userinfo should return userinfo`() {
        val expectedCommandOutput = CommandOutput(true, "")
        val userInfoResult = service.getUserInfo()
        assertEquals(expectedCommandOutput, userInfoResult)
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

    private fun getStringFromFile(filePath: String, debug: Boolean = false): String {
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
        var parser: ProcessHandler.ProcessParser? = null

        override fun execute(vararg command: String): CommandOutput {
            return function?.invoke(command) ?: delegate.execute(*command)
        }

        override fun execute(processParser: ProcessHandler.ProcessParser): CommandOutput {
            return parser?.parse() ?: processParser.parse()
        }
    }
}