import Functions.Companion.readFile
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.io.File

class LoginProcessParserTest {

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

    private val loginProcessParser = LoginProcessParser(fakeTokenCard)

    @Test
    fun `when creating LoginProcessParser command should be correct`() {
        val tokenCard = TokenCardExtractor().extractTokenCard(readFile("TokenCard.txt"))
        val parser = LoginProcessParser(tokenCard)
        assertThat(parser.command).isEqualTo(readFile("LoginCommand.txt").split(" "))
    }

    @Test
    fun `when using valid tokencard should successfully login`() {
        val card = File(this.javaClass.getResource("TokenCardValid.pdf").file)

        val tokenCard = TokenCardReader().readTokenCard(card)
        val parser = LoginProcessParser(tokenCard)
        assertThat(parser.parse()).isEqualTo(CommandOutput.Success("Log in success"))
    }

    @Test
    fun `when using invalid tokencard should fail login`() {
        val parser = LoginProcessParser(fakeTokenCard)
        assertThat(parser.parse()).isEqualTo(CommandOutput.Error("Unauthorized"))
    }

    @Test
    fun `should return correct coordinate from text`() {
        val text1 = "Provide token for coordinate [7, G]:"
        val text2 = "Provide token for coordinate [3, E]:"
        val text3 = "Provide token for coordinate [1, A]:"

        assertThat(loginProcessParser.extractCoordinate(text1)).isEqualTo("7G")
        assertThat(loginProcessParser.extractCoordinate(text2)).isEqualTo("3E")
        assertThat(loginProcessParser.extractCoordinate(text3)).isEqualTo("1A")
    }
}