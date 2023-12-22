import com.google.common.truth.Truth.assertThat
import okio.buffer
import okio.source
import org.junit.Test
import java.io.File

class TokenCardReaderTest {

    private val tokenCardReader = TokenCardReader()

    @Test
    fun `Reading pdf should output content`() {
        val card = File(this.javaClass.getResource("card-83329.pdf").file)
        val expectedTokenCard = TokenCardExtractor().extractTokenCard(readFile("TokenCard.txt"))
        assertThat(tokenCardReader.readTokenCard(card)).isEqualTo(expectedTokenCard)
    }

    private fun readFile(filename: String): String {
        val classLoader = this.javaClass.classLoader
        val file = classLoader.getResourceAsStream(filename).source().buffer()
        return file.readUtf8()
    }
}