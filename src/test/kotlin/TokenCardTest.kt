import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test

class TokenCardTest {
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
    private val tokenCard = TokenCard(14567, "Test Peter Testesen", "Feb 28, 2025", tokenMap)

    @Test
    fun `should get token 1A`() {
        val expectedToken = "iuh7"
        assertThat(tokenCard.getToken("1A")).isEqualTo(expectedToken)
    }

    @Test
    fun `should get token 8H`() {
        val expectedToken = "si82"
        assertThat(tokenCard.getToken("8H")).isEqualTo(expectedToken)
    }

    @Test
    fun `when coordinate lengt greater than 2 should throw`() {
        val e = assertThrows(IllegalStateException::class.java) { tokenCard.getToken("11A") }
        assertThat(e).hasMessageThat().isEqualTo("coordinate length should be equal to 2 but was: 3")
    }
    @Test
    fun `when first character in coordinate out of range should throw`() {
        val e = assertThrows(IllegalStateException::class.java) { tokenCard.getToken("9A") }
        assertThat(e).hasMessageThat().isEqualTo("coordinate not in range 1 to 8. Was 9")
    }

    @Test
    fun `when second character in coordinate out of range should throw`() {
        val e = assertThrows(IllegalStateException::class.java) { tokenCard.getToken("1I") }
        assertThat(e).hasMessageThat().isEqualTo("coordinate not in range A to H. Was I")
    }
}