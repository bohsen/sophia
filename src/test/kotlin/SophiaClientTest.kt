import Functions.Companion.readFile
import com.google.common.truth.Truth.assertThat
import com.russhwolf.settings.MapSettings
import org.junit.Assert.assertThrows
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class SophiaClientTest {
    private val fakeProcessHandler = FakeProcessHandler()
    private val provider = { MapSettings() }
    private val settings = KeyValueStore(provider).apply {
        tokenCard = TokenCardExtractor().extractTokenCard(readFile("TokenCard-2.txt"))
    }
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
        assertThat(pythonInstalled).isInstanceOf(CommandOutput.Success::class.java)
    }

    @Test
    fun `when processhandler throws should error in client`() {
        fakeProcessHandler.function = { throw InterruptedException("Manual throw") }
        assertThrows(InterruptedException::class.java) { service.dependencyCheck() }
    }

    @Test
    fun `when executed command fails should report failed output`() {
        val expectedCommandOutput = CommandOutput.FailedCommand("Unknown option: '-userInfo'")
        fakeProcessHandler.function = { expectedCommandOutput }
        assertThat(service.dependencyCheck()).isEqualTo(expectedCommandOutput)
    }

    @Test
    fun `when login with valid user should log in`() {
        val expectedCommandOutput = CommandOutput.Success("Log in success")
        assertThat(service.login()).isEqualTo(expectedCommandOutput)
    }

    @Test
    fun `when login with invalid user should fail`() {
        settings.tokenCard = fakeTokenCard
        val expectedCommandOutput = CommandOutput.Error("Log in failure")
        assertThat(service.login()).isEqualTo(expectedCommandOutput)
    }

    @Test
    fun `given user logged in when logging out then user is logged out`() {
        val expectedCommandOutput = CommandOutput.Success("User logged out")
        val logoutResult = service.logout()
        assertThat(logoutResult).isEqualTo(expectedCommandOutput)
    }

    @Test
    fun `given user logged in when requesting userinfo should return userinfo`() {
        service.login()
        val userInfoResult = service.getUserInfo()
        assertThat(userInfoResult).isInstanceOf(CommandOutput.Success::class.java)
    }

    @Test
    fun `given user logged in when requesting pipelines then pipelines are returned`() {
        service.login()
        val userInfoResult = service.getPipelines()
        assertThat(userInfoResult).isInstanceOf(CommandOutput.Success::class.java)
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