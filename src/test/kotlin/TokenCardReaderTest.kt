import Functions.Companion.readFile
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.io.File

class TokenCardReaderTest {

    private val tokenCardReader = TokenCardReader()

    @Test
    fun `Reading pdf should output content`() {
        val card = File(this.javaClass.getResource("TokenCardInvalid.pdf").file)
        val expectedTokenCard = TokenCardExtractor().extractTokenCard(readFile("TokenCard.txt"))
        assertThat(tokenCardReader.readTokenCard(card)).isEqualTo(expectedTokenCard)
    }
}