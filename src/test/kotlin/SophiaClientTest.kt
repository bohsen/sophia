import okio.buffer
import okio.source
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class SophiaClientTest {
    private val fakeProcessHandler = FakeProcessHandler()
    private val service = SophiaClient(fakeProcessHandler)

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
        val pythonInstalled = service.login(getResourceAsStream("sophia.txt"))
    }

    private fun getResourceAsStream(file: String): String {
        return this.javaClass.classLoader.getResourceAsStream(file)!!.source().buffer().use { source ->
            source.readUtf8()
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