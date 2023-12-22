import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor
import com.itextpdf.kernel.pdf.canvas.parser.listener.SimpleTextExtractionStrategy
import java.io.File

interface TokenCardReader {
    fun readTokenCard(pdf: File): TokenCard

    companion object {
        operator fun invoke(): TokenCardReader = TokenCardReaderImpl()
    }
}

private class TokenCardReaderImpl : TokenCardReader {

    override fun readTokenCard(pdf: File): TokenCard {
        lateinit var text: String
        PdfReader(pdf).use { reader ->
            PdfDocument(reader).use { document ->
                text = PdfTextExtractor.getTextFromPage(
                    document.getPage(1),
                    SimpleTextExtractionStrategy()
                )
            }
        }

        return TokenCardExtractor().extractTokenCard(text)
    }
}