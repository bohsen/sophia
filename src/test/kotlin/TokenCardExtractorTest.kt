import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TokenCardExtractorTest {

    private val tokenCardExtractor = TokenCardExtractor()
    private val fakePdfOutput = """
        Test Peter Testesen (14567) | Expires: Feb 28, 2025
        1 2 3 4 5 6 7 8
        A iuh7 9iik lkmd ssuu sbna wyyh mkki aayh
        B nnhs yyhh aamj wpod mcjy aakl qldo siah
        C amaj weks 99jd ala9 smn6 aqq8 ssm8 xxuu
        D 8a9a aakk 9w8d vmvu xxn7 65ee ddl9 xnh8
        E xjs7 aak9 vv87 z765 c765 xx92 117h sslk
        F mvnu xxb3 84fw ww66 aa83 sw78 11jr 725e
        G a9au a7ue ve84 slxo x763 d7d7 l93w u756
        H am12 lw92 ie7x m7tg 9hg6 7gbc cu72 si82
    """.trimIndent()

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

    @Test
    fun `should extract TokenCard information`() {
        val expectedTokenCard = TokenCard(14567, "Test Peter Testesen", "Feb 28, 2025", tokenMap)
        assertThat(tokenCardExtractor.extractTokenCard(fakePdfOutput)).isEqualTo(expectedTokenCard)
    }
}